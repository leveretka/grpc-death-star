package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.util.RoundRobinLoadBalancerFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import ua.nedz.grpc.*

class DeathStarServiceImpl : DeathStarServiceGrpcKt.DeathStarServiceImplBase() {

    private var planetTarget: String? = System.getenv("PLANET_SERVICE_TARGET")

    init {
        if (planetTarget.isNullOrEmpty()) {
            planetTarget = "192.168.0.102:50061"
        }
    }

    private val planetChannel = ManagedChannelBuilder
            .forTarget(planetTarget)
            .nameResolverFactory(DnsNameResolverProvider())
            .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
            .usePlaintext(true)
            .build()

    private val planetStub = PlanetServiceGrpcKt.newStub(planetChannel)


    @ExperimentalCoroutinesApi
    override suspend fun destroy(requests: ReceiveChannel<DeathStarProto.DestroyPlanetRequest>) = produce<PlanetProto.Planets> {
        send(planetStub.getAllPlanets(Empty.getDefaultInstance()))
            for (request in requests) {
                planetStub.removePlanet(PlaneServiceProto.RemovePlanetRequest
                        .newBuilder()
                        .setPlanetId(request.planetId)
                        .build())
                //write score
                //send event
                planetStub.generateNewPlanet(Empty.getDefaultInstance())
                send(planetStub.getAllPlanets(Empty.getDefaultInstance())
                )
            }
    }

}