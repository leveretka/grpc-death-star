package ua.nedz.demo

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import ua.nedz.grpc.*
import ua.nedz.grpc.PlanetProto.DestroyPlanetRequest
import ua.nedz.grpc.PlanetProto.Planets
import java.util.concurrent.Executors

class DeathStarServiceImpl : DeathStarServiceImplBase(coroutineContext = Executors.newFixedThreadPool(16).asCoroutineDispatcher()) {

    private val listeners = mutableListOf<Channel<Planets>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceGrpc.newStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpc.newStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpc.newStub(logChannel)

    override suspend fun destroy(requests: ReceiveChannel<DestroyPlanetRequest>): ReceiveChannel<Planets> {
        val channel = Channel<Planets>()
        listeners.add(channel)
        channel.send(populateWithCoordinates(planetStub.getAllPlanets()))
        for (request in requests) {
            val wasRemoved = planetStub.removePlanet(RemovePlanetRequest { planetId = request.planetId })
            if (wasRemoved.result) {
                scoreStub.addScore(AddScoreRequest {
                    userName = request.userName
                    toAdd = request.weight
                })
                logStub.destroyedPlanet(request)
                val newPlanet = planetStub.generateNewPlanet()
                logStub.newPlanet(newPlanet)
                listeners.forEach {
                    it.send(Planets {
                        addPlanets(populateWithCoordinates(newPlanet, request.coordinates.x, request.coordinates.y))
                    })
                }
            }
        }
        return channel
    }

}