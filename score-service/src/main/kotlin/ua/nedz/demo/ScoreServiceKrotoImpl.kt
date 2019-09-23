package ua.nedz.demo

import com.google.protobuf.Empty
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.*
import ua.nedz.grpc.ScoreServiceCoroutineGrpc
import ua.nedz.grpc.ScoreServiceProto
import ua.nedz.grpc.ScoreServiceProtoBuilders.Score
import ua.nedz.grpc.ScoreServiceProtoBuilders.ScoresResponse
import ua.nedz.grpc.addScores
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class ScoreServiceKrotoImpl: ScoreServiceCoroutineGrpc.ScoreServiceImplBase() {

    override val initialContext: CoroutineContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()

    private val scoresMap = mutableMapOf<String, Long>()

    private val scoreBroadcast = BroadcastChannel<ScoreServiceProto.ScoresResponse>(Channel.BUFFERED)

    override suspend fun addScore(request: ScoreServiceProto.AddScoreRequest): Empty {
        scoresMap.putIfAbsent(request.userName, 0)
        scoresMap[request.userName]?.let {
            scoresMap[request.userName] = it + request.toAdd
        }

        notifyListeners()
        return Empty.getDefaultInstance()
    }

    private suspend fun notifyListeners() {
        val allScores = ScoresResponse {
            scoresMap.entries.sortedByDescending { it.value }.forEach { (id, value) ->
                addScores {
                    userName = id
                    score = value
                }
            }
        }

        scoreBroadcast.send(allScores)
    }

    override suspend fun scores(request: Empty, responseChannel: SendChannel<ScoreServiceProto.ScoresResponse>) {
        val subscription = scoreBroadcast.openSubscription()

        subscription.consumeEach { responseChannel.send(it) }
    }

}