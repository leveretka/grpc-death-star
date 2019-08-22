package ua.nedz.demo

import com.google.protobuf.Empty
import kotlinx.coroutines.asCoroutineDispatcher
import ua.nedz.grpc.PlanetProto
import ua.nedz.grpc.PlanetProtoBuilders
import ua.nedz.grpc.PlanetProtoBuilders.Planet
import ua.nedz.grpc.PlanetProtoBuilders.Planets
import ua.nedz.grpc.PlanetServiceCoroutineGrpc
import ua.nedz.grpc.PlanetServiceProto
import ua.nedz.grpc.PlanetServiceProtoBuilders.RemovePlanetResponse
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong
import kotlin.coroutines.CoroutineContext

class PlanetServiceKrotoImpl : PlanetServiceCoroutineGrpc.PlanetServiceImplBase() {

    init {
        PlanetRepo.initialPlanets()
    }

    override val initialContext: CoroutineContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    private val counter = AtomicLong(1000L)

    override suspend fun generateNewPlanet(request: Empty): PlanetProto.Planet {
        val planetModel = Planet(counter.incrementAndGet(), randomName(), randomWeight(), randomImg())
        PlanetRepo.insertPlanet(planetModel)
        return planetFrom(planetModel)
    }

    override suspend fun getAllPlanets(request: Empty): PlanetProto.Planets {
        println("Inside get all planets")

        return Planets{
            for( planetModel in PlanetRepo.getAllPlanets())
                addPlanets(planetFrom(planetModel))
        }
    }

    override suspend fun removePlanet(request: PlanetServiceProto.RemovePlanetRequest): PlanetServiceProto.RemovePlanetResponse {
        println("Before calling repo")
        return RemovePlanetResponse {
            result = PlanetRepo.deletePlanet(request.planetId)
        }
    }

    override suspend fun getPlanetById(request: PlanetServiceProto.GetPlanetRequest): PlanetProto.Planet =
        PlanetRepo.getPlanetById(request.planetId)
                ?.let(::planetFrom)
                ?: throw PlanetNotFoundException(request.planetId)

    private fun planetFrom(p: Planet): PlanetProto.Planet =
        Planet {
            planetId = p.id
            name = p.name
            weight = p.weight
            img = p.img
        }
}
