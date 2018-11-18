package ua.nedz.demo

import com.google.protobuf.Empty
import ua.nedz.grpc.PlanetProto
import ua.nedz.grpc.PlanetServiceGrpcKt
import ua.nedz.grpc.PlanetServiceProto
import java.util.concurrent.atomic.AtomicLong

class PlanetServiceImpl : PlanetServiceGrpcKt.PlanetServiceImplBase() {
    private val counter = AtomicLong(1000L)
    init {
        PlanetRepo.initialPlanets()
    }

    override suspend fun generateNewPlanet(request: Empty): PlanetProto.Planet {
        val planet = Planet(counter.incrementAndGet(), randomName(), randomWeight())
        PlanetRepo.insertPlanet(planet)
        return PlanetProto.Planet
                .newBuilder()
                .setPlanetId(planet.id)
                .setName(planet.name)
                .setWeight(planet.weight)
                .build()
    }

    override suspend fun getAllPlanets(request: Empty): PlanetProto.Planets {
        println("Inside get all planets")

        return PlanetProto.Planets.newBuilder().addAllPlanets(
                PlanetRepo.getAllPlanets().map {
                    PlanetProto.Planet
                            .newBuilder()
                            .setPlanetId(it.id)
                            .setName(it.name)
                            .setWeight(it.weight)
                            .build()
                })
                .build()
    }

    override suspend fun removePlanet(request: PlanetServiceProto.RemovePlanetRequest): PlanetServiceProto.RemovePlanetResponse {
        PlanetRepo.deletePlanet(request.planetId)
        return PlanetServiceProto.RemovePlanetResponse.newBuilder().setResult(true).build()
    }

}
