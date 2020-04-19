package ua.nedz.demo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ua.nedz.grpc.DeathStarServiceGrpcKt
import ua.nedz.grpc.LogServiceGrpcKt
import ua.nedz.grpc.PlanetProto.DestroyPlanetRequest
import ua.nedz.grpc.PlanetProto.Planets
import ua.nedz.grpc.PlanetServiceGrpcKt
import ua.nedz.grpc.ScoreServiceGrpcKt

@ExperimentalCoroutinesApi
class DeathStarServiceImpl : DeathStarServiceGrpcKt.DeathStarServiceCoroutineImplBase() {

    private val listeners = mutableListOf<ProducerScope<Planets>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceGrpcKt.PlanetServiceCoroutineStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpcKt.ScoreServiceCoroutineStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpcKt.LogServiceCoroutineStub(logChannel)

    override fun destroy(requests: Flow<DestroyPlanetRequest>): Flow<Planets> = channelFlow {
        listeners.add(this)
        GlobalScope.launch {
            send(populateWithCoordinates(planetStub.getAllPlanets()))
            requests.collect { request ->
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
        }
        awaitClose {  }
    }
}