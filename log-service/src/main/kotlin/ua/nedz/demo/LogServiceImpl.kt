package ua.nedz.demo

import com.google.protobuf.Empty
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import ua.nedz.grpc.LogServiceGrpcKt
import ua.nedz.grpc.LogServiceProto
import ua.nedz.grpc.PlanetProto

class LogServiceImpl: LogServiceGrpcKt.LogServiceImplBase() {
    private val listeners = mutableListOf<Channel<LogServiceProto.Log>>()
    private val userNames = mutableMapOf<Long, String>()
    private val planetNames = mutableMapOf<Long, String>()

    override suspend fun newPlanet(request: PlanetProto.Planet): Empty {
        notifyUsers("{Planet ${request.name} was born.")
        planetNames[request.planetId] = request.name
        return Empty.getDefaultInstance()
    }

    override suspend fun destroyedPlanet(request: PlanetProto.DestroyPlanetRequest): Empty {
        notifyUsers("User ${userNames[request.userId]} destroted planet ${planetNames[request.planetId]}!")
        return Empty.getDefaultInstance()
    }

    override suspend fun newUser(request: LogServiceProto.User): ReceiveChannel<LogServiceProto.Log> {
        val channel = Channel<LogServiceProto.Log>()
        listeners.add(channel)
        notifyUsers("User ${request.name} joined.")
        userNames[request.userId] = request.name
        return channel
    }

    private suspend fun notifyUsers(message: String) =
            listeners.forEach {
                it.send(LogServiceProto.Log.newBuilder()
                        .setMessage(message)
                        .build())
            }
}