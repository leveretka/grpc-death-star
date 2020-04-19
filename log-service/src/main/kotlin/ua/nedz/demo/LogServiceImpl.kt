package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.util.RoundRobinLoadBalancerFactory
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ua.nedz.grpc.*

class LogServiceImpl: LogServiceGrpcKt.LogServiceCoroutineImplBase() {
    private val listeners = mutableListOf<FlowCollector<LogServiceProto.Log>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"

    private val planetChannel = ManagedChannelBuilder
            .forTarget(planetTarget)
            .nameResolverFactory(DnsNameResolverProvider())
            .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
            .usePlaintext()
            .build()

    private val planetStub = PlanetServiceGrpcKt.PlanetServiceCoroutineStub(planetChannel)

    override suspend fun newPlanet(request: PlanetProto.Planet): Empty {
        notifyUsers("Planet ${request.name} was born.")
        return Empty.getDefaultInstance()
    }

    override suspend fun destroyedPlanet(request: PlanetProto.DestroyPlanetRequest): Empty {
        val planet = planetStub.getPlanetById(PlanetServiceProto.GetPlanetRequest.newBuilder()
                .setPlanetId(request.planetId)
                .build())
        notifyUsers("User ${request.userName} destroyed planet ${planet.name}!")
        return Empty.getDefaultInstance()
    }

    override fun newUser(request: LogServiceProto.User): Flow<LogServiceProto.Log> = flow {
        listeners.add(this)
        GlobalScope.launch { notifyUsers("User ${request.name} joined.") }
    }

    private suspend fun notifyUsers(message: String) =
        listeners.forEach {
            GlobalScope.launch {
                it.emit(LogServiceProto.Log.newBuilder()
                        .setMessage(message)
                        .build())
            }
        }

}