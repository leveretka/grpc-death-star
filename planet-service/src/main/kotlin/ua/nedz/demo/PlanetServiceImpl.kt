package ua.nedz.demo

import com.google.protobuf.Empty
import ua.nedz.grpc.PlaneServiceProto
import ua.nedz.grpc.PlanetProto
import ua.nedz.grpc.PlanetServiceGrpcKt
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import kotlin.random.nextInt

class PlanetServiceImpl : PlanetServiceGrpcKt.PlanetServiceImplBase() {
    private val counter = AtomicLong(1000L)

    override suspend fun generateNewPlanet(request: Empty): PlanetProto.Planet {
        val weight: Long = randomWeight()
        val name = randomName()

        val planet = Planet(counter.incrementAndGet(), name, weight, true)
        PlanetRepo.insertPlanet(planet)
        return PlanetProto.Planet
                .newBuilder()
                .setPlanetId(planet.id)
                .setName(planet.name)
                .setWeight(planet.weight)
                .build()
    }

    private fun randomName() = PlanetRepo.names[Random.nextInt(1..PlanetRepo.names.size) - 1]

    private fun randomWeight(): Long =
            when (Random.nextInt(0..100)) {
                in 0..4 -> 500
                in 5..14 -> 200
                in 15..29 -> 100
                in 30..49 -> 50
                in 50..74 -> 20
                in 75..100 -> 10
                else -> 0
            }

    override suspend fun getAllPlanets(request: Empty) =
            PlanetProto.Planets.newBuilder().addAllPlanets(
                    PlanetRepo.getAllPlanets().map {
                        PlanetProto.Planet
                                .newBuilder()
                                .setPlanetId(it.id)
                                .setName(it.name)
                                .setWeight(it.weight)
                                .build()
                    })
                    .build()

    override suspend fun removePlanet(request: PlaneServiceProto.RemovePlanetRequest): PlaneServiceProto.RemovePlanetResponse {
        PlanetRepo.deletePlanet(request.planetId)
        return PlaneServiceProto.RemovePlanetResponse.newBuilder().setResult(true).build()
    }

}
