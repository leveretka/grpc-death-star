package com.example.deathstarclient

import com.google.protobuf.Empty
import io.grpc.ClientInterceptors
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.internal.DnsNameResolverProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import ua.nedz.grpc.*
import kotlin.random.Random
import kotlin.random.nextInt

class DeathStarClient {

    private var deathStarTarget: String = System.getenv("DEATH_STAR_SERVICE_TARGET") ?: "localhost:50051"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val ch = channelForTarget(deathStarTarget)
    private val deathStarChannel = ClientInterceptors.intercept(ch, HackTheSystemInterceptor())
    private val deathStarStub = DeathStarServiceGrpcKt.DeathStarServiceCoroutineStub(deathStarChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceGrpcKt.ScoreServiceCoroutineStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceGrpcKt.LogServiceCoroutineStub(logChannel)

    fun join(userName: String, destroyFlow: Flow<PlanetProto.DestroyPlanetRequest>): JoinResult {
        println("Inside Join")
        val planetsStream = deathStarStub.destroy(destroyFlow)
        val logStream = logStub.newUser(LogServiceProto.User.newBuilder()
                .setName(userName)
                .build())
        val scoresStream = scoreStub.scores(Empty.getDefaultInstance())
        println("Received all streams")
        return JoinResult(planetsStream, logStream, scoresStream)
    }

    data class JoinResult (
            val planetsStream: Flow<PlanetProto.Planets>,
            val logStream: Flow<LogServiceProto.Log>,
            val scoresStream: Flow<ScoreServiceProto.ScoresResponse>
    )

    fun succesfulDestroyAttempt(p: PlanetProto.Planet): Boolean {
        val prob = 0.1 * Math.sqrt(260412.5 - (p.weight * p.weight))
        return Random.nextInt(0..100) < prob
    }

    fun DestroyPlanetRequest(init: PlanetProto.DestroyPlanetRequest.Builder.() -> Unit) =
            PlanetProto.DestroyPlanetRequest.newBuilder()
                    .apply(init)
                    .build()

    fun Coordinates(init: PlanetProto.Coordinates.Builder.() -> Unit) =
            PlanetProto.Coordinates.newBuilder()
                    .apply(init)
                    .build()


    private fun channelForTarget(target: String): ManagedChannel {
        return ManagedChannelBuilder
                .forTarget(target)
                .nameResolverFactory(DnsNameResolverProvider())
                .usePlaintext()
                .build()
    }
}