package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.stub.StreamObserver
import ua.nedz.grpc.*
import ua.nedz.grpc.DeathStarServiceGrpc.DeathStarServiceImplBase
import ua.nedz.grpc.PlanetProto.DestroyPlanetRequest
import ua.nedz.grpc.PlanetProto.Planets

class OldDeathStarServiceImpl : DeathStarServiceImplBase() {

    private val listeners = mutableListOf<StreamObserver<Planets>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceGrpc.newStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpc.newStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpc.newStub(logChannel)

    override fun destroy(responseObserver: StreamObserver<Planets>): StreamObserver<DestroyPlanetRequest> {
        listeners.add(responseObserver)
        planetStub.getAllPlanets(Empty.getDefaultInstance(),
                object : StreamObserver<Planets> by DefaultStreamObserver() {
                    override fun onNext(planets: Planets) {
                        responseObserver.onNext(populateWithCoordinnates(planets))
                    }
                })
        return object : StreamObserver<DestroyPlanetRequest> by DefaultStreamObserver() {
            override fun onNext(destroyPlanetRequest: DestroyPlanetRequest) {
                planetStub.removePlanet(RemovePlanetRequest { planetId = destroyPlanetRequest.planetId },
                        object : StreamObserver<PlanetServiceProto.RemovePlanetResponse> by DefaultStreamObserver() {
                            override fun onNext(removePlanetResponse: PlanetServiceProto.RemovePlanetResponse) {
                               if (removePlanetResponse.result) {
                                   scoreStub.addScore(AddScoreRequest {
                                       userName = destroyPlanetRequest.userName
                                       toAdd = destroyPlanetRequest.weight
                                   }, object : StreamObserver<Empty> by DefaultStreamObserver() {})
                                   logStub.destroyedPlanet(destroyPlanetRequest, object : StreamObserver<Empty> by DefaultStreamObserver() {})
                                   planetStub.generateNewPlanet(Empty.getDefaultInstance(),
                                           object : StreamObserver<PlanetProto.Planet> by DefaultStreamObserver() {
                                               override fun onNext(planet: PlanetProto.Planet) {
                                                   logStub.newPlanet(planet, object : StreamObserver<Empty> by DefaultStreamObserver() {})
                                                   listeners.forEach {
                                                       it.onNext(Planets {
                                                           addPlanets(populateWithCoordinates(planet,
                                                                   destroyPlanetRequest.coordinates.x,
                                                                   destroyPlanetRequest.coordinates.y))
                                                       })
                                                   }
                                               }
                                           })
                               }
                            }
                        })
            }
        }
    }
}
