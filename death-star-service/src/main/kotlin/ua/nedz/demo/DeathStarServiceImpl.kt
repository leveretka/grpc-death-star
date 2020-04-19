package ua.nedz.demo

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ua.nedz.grpc.DeathStarServiceGrpcKt
import ua.nedz.grpc.LogServiceGrpcKt
import ua.nedz.grpc.PlanetProto.DestroyPlanetRequest
import ua.nedz.grpc.PlanetProto.Planets
import ua.nedz.grpc.PlanetServiceGrpcKt
import ua.nedz.grpc.ScoreServiceGrpcKt

class DeathStarServiceImpl : DeathStarServiceGrpcKt.DeathStarServiceCoroutineImplBase() {

    private val listeners = mutableListOf<FlowCollector<Planets>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceGrpcKt.PlanetServiceCoroutineStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpcKt.ScoreServiceCoroutineStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpcKt.LogServiceCoroutineStub(logChannel)

    override fun destroy(requests: Flow<DestroyPlanetRequest>): Flow<Planets> = flow {
        listeners.add(this)
        GlobalScope.launch {
            emit(populateWithCoordinates(planetStub.getAllPlanets()))
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
                        it.emit(Planets {
                            addPlanets(populateWithCoordinates(newPlanet, request.coordinates.x, request.coordinates.y))
                        })
                    }
                }
            }
        }
    }
}