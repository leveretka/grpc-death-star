package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.util.RoundRobinLoadBalancerFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import ua.nedz.grpc.*
import java.util.concurrent.Executors

class DeathStarServiceImpl : DeathStarServiceImplBase(coroutineContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()) {

    private val listeners = mutableListOf<Channel<PlanetProto.Planets>>()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceGrpc.newStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpc.newStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpc.newStub(logChannel)

    @ExperimentalCoroutinesApi
    override suspend fun destroy(requests: ReceiveChannel<PlanetProto.DestroyPlanetRequest>): ReceiveChannel<PlanetProto.Planets> {
        val channel = Channel<PlanetProto.Planets>()
        listeners.add(channel)
        println("Sending all planets")
        GlobalScope.launch {
            channel.send(planetStub.getAllPlanets(Empty.getDefaultInstance()))
            println("Sent all planets")
        }

        GlobalScope.launch {
            for (request in requests) {
                planetStub.removePlanet(RemovePlanetRequest {planetId = request.planetId})
                scoreStub.addScore(AddScoreRequest {
                    userName = request.userName
                    toAdd = request.weight
                })
                logStub.destroyedPlanet(request)
                val newPlanet = planetStub.generateNewPlanet(Empty.getDefaultInstance())
                logStub.newPlanet(newPlanet)
                listeners.forEach {
                    it.send(planetStub.getAllPlanets(Empty.getDefaultInstance()))
                    println("Sent all planets")
                }
            }
        }
        return channel
    }

    private fun channelForTarget(target: String): ManagedChannel {
        return ManagedChannelBuilder
                .forTarget(target)
                .nameResolverFactory(DnsNameResolverProvider())
                .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
                .usePlaintext(true)
                .build()
    }

    private fun RemovePlanetRequest(init: PlanetServiceProto.RemovePlanetRequest.Builder.() -> Unit) =
            PlanetServiceProto.RemovePlanetRequest.newBuilder()
                    .apply(init)
                    .build()

    private fun AddScoreRequest(init: ScoreServiceProto.AddScoreRequest.Builder.() -> Unit) =
            ScoreServiceProto.AddScoreRequest.newBuilder()
                    .apply(init)
                    .build()


}