package ua.nedz.demo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import ua.nedz.grpc.DeathStarProto
import ua.nedz.grpc.DeathStarServiceGrpcKt
import ua.nedz.grpc.PlanetProto

class DeathStarServiceImpl : DeathStarServiceGrpcKt.DeathStarServiceImplBase() {

    @ExperimentalCoroutinesApi
    override suspend fun destroy(requests: ReceiveChannel<DeathStarProto.DestroyPlanetRequest>) = produce<PlanetProto.Planets> {
        //getAllPlanets
        val planetsList = listOf<PlanetProto.Planet>()
        send(PlanetProto.Planets.newBuilder().addAllPlanets(planetsList).build())
            for (request in requests) {
                //remove planet
                //write score
                //send event
                //generate new planet
                //get all planets
                send(PlanetProto.Planets.newBuilder().addAllPlanets(planetsList).build())
            }
    }

}