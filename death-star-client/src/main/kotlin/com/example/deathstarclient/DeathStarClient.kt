package com.example.deathstarclient

import com.google.protobuf.Empty
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import io.grpc.util.RoundRobinLoadBalancerFactory
import kotlinx.coroutines.channels.ReceiveChannel
import ua.nedz.grpc.*
import kotlin.random.Random
import kotlin.random.nextInt

class DeathStarClient {

    private var deathStarTarget: String? = System.getenv("DEATH_STAR_SERVICE_TARGET")
    private var scoreTarget: String? = System.getenv("SCORE_SERVICE_TARGET")
    private var logTarget: String? = System.getenv("LOG_SERVICE_TARGET")

    init {
        if (deathStarTarget.isNullOrEmpty()) deathStarTarget = "localhost:50051"
        if (scoreTarget.isNullOrEmpty()) scoreTarget = "localhost:50071"
        if (logTarget.isNullOrEmpty()) logTarget = "localhost:50081"
    }

    private val deathStarChannel = ManagedChannelBuilder
            .forTarget(deathStarTarget)
            .nameResolverFactory(DnsNameResolverProvider())
            .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
            .usePlaintext(true)
            .build()
    private val deathStarStub = DeathStarServiceGrpcKt.newStub(deathStarChannel)

    private val scoreChannel = ManagedChannelBuilder
            .forTarget(scoreTarget)
            .nameResolverFactory(DnsNameResolverProvider())
            .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
            .usePlaintext(true)
            .build()
    private val scoreStub = ScoreServiceGrpcKt.newStub(scoreChannel)

    private val logChannel = ManagedChannelBuilder
            .forTarget(logTarget)
            .nameResolverFactory(DnsNameResolverProvider())
            .loadBalancerFactory(RoundRobinLoadBalancerFactory.getInstance())
            .usePlaintext(true)
            .build()
    private val logStub = LogServiceGrpcKt.newStub(logChannel)


    fun join(userName: String): JoinResult {
        println("Inside Join")
        val planetsStream = deathStarStub.destroy()
        val logStream = logStub.newUser(LogServiceProto.User.newBuilder()
                .setName(userName)
                .build())
        val scoresStream = scoreStub.scores(Empty.getDefaultInstance())
        println("Received all streams")
        return JoinResult(planetsStream, logStream, scoresStream)
    }

    data class JoinResult (
            val planetsStream: DeathStarServiceGrpcKt.ManyToManyCall<PlanetProto.DestroyPlanetRequest, PlanetProto.Planets>,
            val logStream: ReceiveChannel<LogServiceProto.Log>,
            val scoresStream: ReceiveChannel<ScoreServiceProto.ScoresResponse>
    )

    fun succesfulDestroyAttempt(p: PlanetProto.Planet): Boolean {
        val probabilty = Math.min(50, 5000 / p.weight)
        return Random.nextInt(0..100) < probabilty
    }

}