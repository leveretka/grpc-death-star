package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.util.RoundRobinLoadBalancerFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import ua.nedz.grpc.*

class LogServiceImpl: LogServiceGrpcKt.LogServiceImplBase() {
    private val listeners = mutableListOf<Channel<LogServiceProto.Log>>()

    private var planetTarget: String? = System.getenv("PLANET_SERVICE_TARGET")

    init {
        if (planetTarget.isNullOrEmpty()) planetTarget = "localhost:50061"
    }

    private val planetChannel = ManagedChannelBuilder
            .forTarget(planetTarget)
            .nameResolverFactory(DnsNameResolverProvider())
            .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
            .usePlaintext(true)
            .build()
    private val planetStub = PlanetServiceGrpcKt.newStub(planetChannel)

    override suspend fun newPlanet(request: PlanetProto.Planet): Empty {
        notifyUsers("Planet ${request.name} was born.")
        return Empty.getDefaultInstance()
    }

    override suspend fun destroyedPlanet(request: PlanetProto.DestroyPlanetRequest): Empty {
        val planet = planetStub.getPlanetById(PlanetServiceProto.GetPlanetRequest.newBuilder()
                .setPlanetId(request.planetId)
                .build())
        notifyUsers("User ${request.userName} destroted planet ${planet.name}!")
        return Empty.getDefaultInstance()
    }

    override suspend fun newUser(request: LogServiceProto.User): ReceiveChannel<LogServiceProto.Log> {
        val channel = Channel<LogServiceProto.Log>()
        listeners.add(channel)
        notifyUsers("User ${request.name} joined.")
        return channel
    }

    private suspend fun notifyUsers(message: String) =
        listeners.forEach {
            GlobalScope.launch {
                it.send(LogServiceProto.Log.newBuilder()
                        .setMessage(message)
                        .build())
            }
        }

}