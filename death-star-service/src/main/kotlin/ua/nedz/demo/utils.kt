package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.stub.StreamObserver
import ua.nedz.grpc.*

fun channelForTarget(target: String): ManagedChannel {
    return ManagedChannelBuilder
            .forTarget(target)
            .nameResolverFactory(DnsNameResolverProvider())
            .usePlaintext()
            .build()
}

fun RemovePlanetRequest(init: PlanetServiceProto.RemovePlanetRequest.Builder.() -> Unit) =
        PlanetServiceProto.RemovePlanetRequest.newBuilder()
                .apply(init)
                .build()

fun AddScoreRequest(init: ScoreServiceProto.AddScoreRequest.Builder.() -> Unit) =
        ScoreServiceProto.AddScoreRequest.newBuilder()
                .apply(init)
                .build()

fun Coordinates(init: PlanetProto.Coordinates.Builder.() -> Unit) =
        PlanetProto.Coordinates.newBuilder()
                .apply(init)
                .build()

fun Planet(init: PlanetProto.Planet.Builder.() -> Unit) =
        PlanetProto.Planet.newBuilder()
                .apply(init)
                .build()

fun Planets(init: PlanetProto.Planets.Builder.() -> Unit) =
        PlanetProto.Planets.newBuilder()
                .apply(init)
                .build()

class DefaultStreamObserver<T> : StreamObserver<T> {
    override fun onNext(value: T?) {}

    override fun onError(t: Throwable?) {}

    override fun onCompleted() {}
}

fun populateWithCoordinates(allPlanets: PlanetProto.Planets): PlanetProto.Planets {
    val populatedPlanets = Planets {
        (0 until allPlanets.planetsCount).forEach {
            val p = allPlanets.getPlanets(it)
            val newPlanet = populateWithCoordinates(p, it % 10, it / 10)
            addPlanets(newPlanet)
        }
    }
    return populatedPlanets
}

fun populateWithCoordinates(p: PlanetProto.Planet, x: Int, y: Int): PlanetProto.Planet? {
    val newPlanet = Planet {
        planetId = p.planetId
        name = p.name
        weight = p.weight
        img = p.img
        coordinates = Coordinates {
            this.x = x
            this.y = y
        }
    }
    return newPlanet
}

suspend fun PlanetServiceGrpcKt.PlanetServiceCoroutineStub.getAllPlanets() =
        getAllPlanets(Empty.getDefaultInstance())

suspend fun PlanetServiceGrpcKt.PlanetServiceCoroutineStub.generateNewPlanet() =
        generateNewPlanet(Empty.getDefaultInstance())

