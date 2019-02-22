package ua.nedz.demo

import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.testing.GrpcServerRule
import io.grpc.util.RoundRobinLoadBalancerFactory
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import org.junit.Rule
import ua.nedz.grpc.*
import kotlin.test.BeforeTest

class DeathStarServiceImplTest {

    @[Rule JvmField]
    var grpcServerRule = GrpcServerRule().directExecutor()

    @BeforeTest
    fun bindService() {
        grpcServerRule.serviceRegistry?.addService(DeathStarServiceImpl())
    }
}