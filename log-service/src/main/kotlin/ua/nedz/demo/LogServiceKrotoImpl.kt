package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import ua.nedz.grpc.*
import ua.nedz.grpc.LogServiceProtoBuilders.Log
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class LogServiceKrotoImpl: LogServiceCoroutineGrpc.LogServiceImplBase() {

    override val initialContext: CoroutineContext =
        Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"

    private val planetChannel = ManagedChannelBuilder
            .forTarget(planetTarget)
            .nameResolverFactory(DnsNameResolverProvider())
            .defaultLoadBalancingPolicy("round_robin")
            .usePlaintext()
            .build()

    private val planetStub = PlanetServiceCoroutineGrpc.newStub(planetChannel)

    private val logBroadcast = BroadcastChannel<LogServiceProto.Log>(Channel.BUFFERED)

    override suspend fun newPlanet(request: PlanetProto.Planet): Empty {
        notifyUsers("Planet ${request.name} was born.")
        return Empty.getDefaultInstance()
    }

    override suspend fun destroyedPlanet(request: PlanetProto.DestroyPlanetRequest): Empty {
        val planet = planetStub.getPlanetById{ planetId = request.planetId }
        notifyUsers("User ${request.userName} destroyed planet ${planet.name}!")
        return Empty.getDefaultInstance()
    }

    override suspend fun newUser(request: LogServiceProto.User, responseChannel: SendChannel<LogServiceProto.Log>) {
        val subscription = logBroadcast.openSubscription()

        notifyUsers("User ${request.name} joined.")


        subscription.consumeEach { event ->
            responseChannel.send(event)
        }
    }

    private suspend fun notifyUsers(message: String) =
            logBroadcast.send(Log {
                this.message = message
            })

}