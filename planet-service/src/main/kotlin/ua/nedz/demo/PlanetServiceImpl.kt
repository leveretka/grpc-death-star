package ua.nedz.demo

import com.google.protobuf.Empty
import ua.nedz.grpc.PlanetProto
import ua.nedz.grpc.PlanetServiceGrpcKt
import ua.nedz.grpc.PlanetServiceProto
import java.util.concurrent.atomic.AtomicLong

class PlanetServiceImpl : PlanetServiceGrpcKt.PlanetServiceCoroutineImplBase() {
    private val counter = AtomicLong(1000L)
    init {
        PlanetRepo.initialPlanets()
    }

    override suspend fun generateNewPlanet(request: Empty): PlanetProto.Planet {
        val planet = Planet(counter.incrementAndGet(), randomName(), randomWeight(), randomImg())
        PlanetRepo.insertPlanet(planet)
        return Planet(planet)
    }

    override suspend fun getAllPlanets(request: Empty): PlanetProto.Planets {
        println("Inside get all planets")

        return PlanetProto.Planets.newBuilder().addAllPlanets(
                PlanetRepo.getAllPlanets().map {
                    Planet(it)
                })
                .build()
    }

    override suspend fun removePlanet(request: PlanetServiceProto.RemovePlanetRequest): PlanetServiceProto.RemovePlanetResponse {
        println("Before calling repo")
        val result = PlanetRepo.deletePlanet(request.planetId)
        return PlanetServiceProto.RemovePlanetResponse.newBuilder().setResult(result).build()
    }

    override suspend fun getPlanetById(request: PlanetServiceProto.GetPlanetRequest): PlanetProto.Planet {
        PlanetRepo.getPlanetById(request.planetId)?.let {
            return Planet(it)
        }
        throw PlanetNotFoundException(request.planetId)
    }

    private fun Planet(p: Planet): PlanetProto.Planet {
        return Planet {
            planetId = p.id
            name = p.name
            weight = p.weight
            img = p.img
        }
    }

    private fun Planet(init: PlanetProto.Planet.Builder.() -> Unit) =
            PlanetProto.Planet.newBuilder()
                    .apply(init)
                    .build()

}
