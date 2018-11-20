package ua.nedz.demo

import com.google.protobuf.Empty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import ua.nedz.grpc.ScoreServiceGrpcKt
import ua.nedz.grpc.ScoreServiceProto

class ScoreServiceImpl: ScoreServiceGrpcKt.ScoreServiceImplBase() {

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
            GlobalScope.launch { it.send(allScores) }
        }
    }

    @ExperimentalCoroutinesApi
    override suspend fun scores(request: Empty): ReceiveChannel<ScoreServiceProto.ScoresResponse> {
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