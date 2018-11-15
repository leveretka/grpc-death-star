package ua.nedz.demo

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import ua.nedz.grpc.DeathStarProto
import ua.nedz.grpc.DeathStarServiceGrpcKt

class DeathStarServiceImpl : DeathStarServiceGrpcKt.DeathStarServiceImplBase() {

    @ExperimentalCoroutinesApi
    override suspend fun destroy(requests: ReceiveChannel<DeathStarProto.DestroyPlanetRequest>) = produce<DeathStarProto.Planets> {
        //getAllPlanets
        val planetsList = listOf<DeathStarProto.Planet>()
        send(DeathStarProto.Planets.newBuilder().addAllPlanets(planetsList).build())
            for (request in requests) {
                //remove planet
                //write score
                //send event
                //generate new planet
                //get all planets
                send(DeathStarProto.Planets.newBuilder().addAllPlanets(planetsList).build())
            }
    }

}