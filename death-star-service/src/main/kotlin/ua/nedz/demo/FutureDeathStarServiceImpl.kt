package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import ua.nedz.grpc.*

class FutureDeathStarServiceImpl : DeathStarServiceGrpc.DeathStarServiceImplBase() {

    private val listeners = mutableListOf<StreamObserver<PlanetProto.Planets>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceGrpc.newFutureStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpc.newFutureStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpc.newFutureStub(logChannel)

    override fun destroy(responseObserver: StreamObserver<PlanetProto.Planets>): StreamObserver<PlanetProto.DestroyPlanetRequest> {
        listeners.add(responseObserver)
        GlobalScope.launch {
            val allPlanets = planetStub.getAllPlanets(Empty.getDefaultInstance()).await()
            responseObserver.onNext(populateWithCoordinnates(allPlanets))
        }

        return object : StreamObserver<PlanetProto.DestroyPlanetRequest> by DefaultStreamObserver() {
            override fun onNext(destroyPlanetRequest: PlanetProto.DestroyPlanetRequest) {
                GlobalScope.launch {
                    val removePlanet = planetStub.removePlanet(
                            RemovePlanetRequest { planetId = destroyPlanetRequest.planetId }).await()
                    if (removePlanet.result) {
                        scoreStub.addScore(AddScoreRequest {
                            userName = destroyPlanetRequest.userName
                            toAdd = destroyPlanetRequest.weight
                        })
                        logStub.destroyedPlanet(destroyPlanetRequest)

                        val newPlanet = planetStub.generateNewPlanet(Empty.getDefaultInstance()).await()

                        logStub.newPlanet(newPlanet)

                        listeners.forEach {
                            it.onNext(Planets {
                                addPlanets(populateWithCoordinates(newPlanet,
                                        destroyPlanetRequest.coordinates.x,
                                        destroyPlanetRequest.coordinates.y))
                            })
                        }
                    }
                }

            }
        }
    }
}
