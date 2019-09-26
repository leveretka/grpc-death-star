package ua.nedz.demo

import com.google.protobuf.Empty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import ua.nedz.grpc.ScoreServiceImplBase
import ua.nedz.grpc.ScoreServiceProto
import java.util.concurrent.Executors

class ScoreServiceImpl: ScoreServiceImplBase(coroutineContext = Executors.newFixedThreadPool(4).asCoroutineDispatcher()) {

    private val scoresMap = mutableMapOf<String, Long>()
    private val listeners = mutableListOf<Channel<ScoreServiceProto.ScoresResponse>>()

    override suspend fun addScore(request: ScoreServiceProto.AddScoreRequest): Empty {
        scoresMap.putIfAbsent(request.userName, 0)
        scoresMap[request.userName]?.let {
            scoresMap[request.userName] = it + request.toAdd
        }
        notifyListeners(*listeners.toTypedArray())
        return Empty.getDefaultInstance()
    }

    private fun notifyListeners(vararg listeners: Channel<ScoreServiceProto.ScoresResponse>) {
        val allScores = ScoreServiceProto.ScoresResponse.newBuilder()
                .addAllScores(scoresMap.entries.sortedByDescending { it.value }.map { (id, value) ->
                    Score {
                        userName = id
                        score = value
                    }
                })
                .build()

        listeners.forEach {
            launch { it.send(allScores) }
        }
    }

    @ExperimentalCoroutinesApi
    override fun scores(request: Empty): ReceiveChannel<ScoreServiceProto.ScoresResponse> {
        val channel = Channel<ScoreServiceProto.ScoresResponse>()
        listeners.add(channel)
        notifyListeners(channel)
        return channel
    }

    private fun Score(init: ScoreServiceProto.Score.Builder.() -> Unit) =
            ScoreServiceProto.Score.newBuilder()
                    .apply(init)
                    .build()

}