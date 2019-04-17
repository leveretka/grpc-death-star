package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.stub.ServerCallStreamObserver
import io.grpc.stub.StreamObserver
import ua.nedz.grpc.*
import java.util.concurrent.atomic.AtomicBoolean

class ManualFlowDeathStarService : DeathStarServiceGrpc.DeathStarServiceImplBase() {

    private val listeners = mutableListOf<StreamObserver<PlanetProto.Planets>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceGrpc.newStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpc.newStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpc.newStub(logChannel)


    override fun destroy(responseObserver: StreamObserver<PlanetProto.Planets>?): StreamObserver<PlanetProto.DestroyPlanetRequest> {
        val serverCallStreamObserver = responseObserver as ServerCallStreamObserver<PlanetProto.Planets>
        serverCallStreamObserver.disableAutoInboundFlowControl()
        val wasReady = AtomicBoolean(false)
        serverCallStreamObserver.setOnReadyHandler {
            if (serverCallStreamObserver.isReady && wasReady.compareAndSet(false, true)) {
                serverCallStreamObserver.request(1)
            }
        }
        listeners.add(responseObserver)
        planetStub.getAllPlanets(Empty.getDefaultInstance(),
                object : StreamObserver<PlanetProto.Planets> by DefaultStreamObserver() {
                    override fun onNext(value: PlanetProto.Planets) {
                        responseObserver.onNext(populateWithCoordinates(value))
                    }
                })
        return object : StreamObserver<PlanetProto.DestroyPlanetRequest> by DefaultStreamObserver() {
            override fun onNext(destroyPlanetRequest: PlanetProto.DestroyPlanetRequest) {
                planetStub.removePlanet(RemovePlanetRequest { planetId = destroyPlanetRequest.planetId },
                        object : StreamObserver<PlanetServiceProto.RemovePlanetResponse> by DefaultStreamObserver() {
                            override fun onNext(removePlanetResponse: PlanetServiceProto.RemovePlanetResponse) {
                                if (removePlanetResponse.result) {
                                    scoreStub.addScore(AddScoreRequest {
                                        userName = destroyPlanetRequest.userName
                                        toAdd = destroyPlanetRequest.weight
                                    }, object : StreamObserver<Empty> by DefaultStreamObserver() {})
                                    logStub.destroyedPlanet(destroyPlanetRequest, object : StreamObserver<Empty>
                                    by DefaultStreamObserver() {})
                                    planetStub.generateNewPlanet(Empty.getDefaultInstance(),
                                            object : StreamObserver<PlanetProto.Planet> by DefaultStreamObserver() {
                                                override fun onNext(planet: PlanetProto.Planet) {
                                                    logStub.newPlanet(planet, object : StreamObserver<Empty>
                                                    by DefaultStreamObserver() {})
                                                    listeners.forEach {
                                                        it.onNext(Planets {
                                                            addPlanets(populateWithCoordinates(planet,
                                                                    destroyPlanetRequest.coordinates.x,
                                                                    destroyPlanetRequest.coordinates.y))
                                                        })
                                                    }

                                                }
                                            })
                                    if (serverCallStreamObserver.isReady) {
                                        serverCallStreamObserver.request(1)
                                    } else {
                                        wasReady.set(false)
                                    }
                                }
                            }
                        })
            }
        }

    }

    private fun RemovePlanetRequest(init: PlanetServiceProto.RemovePlanetRequest.Builder.() -> Unit) =
            PlanetServiceProto.RemovePlanetRequest.newBuilder()
                    .apply(init)
                    .build()

    private fun AddScoreRequest(init: ScoreServiceProto.AddScoreRequest.Builder.() -> Unit) =
            ScoreServiceProto.AddScoreRequest.newBuilder()
                    .apply(init)
                    .build()
}