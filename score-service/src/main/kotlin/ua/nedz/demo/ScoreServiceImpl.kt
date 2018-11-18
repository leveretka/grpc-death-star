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

    private val scoresMap = mutableMapOf<Long, Long>()
    private val listeners = mutableListOf<Channel<ScoreServiceProto.ScoresResponse>>()


    override suspend fun addScore(request: ScoreServiceProto.AddScoreRequest): Empty {
        scoresMap.putIfAbsent(request.userId, 0)
        scoresMap[request.userId]?.let {
            scoresMap[request.userId] = it + request.toAdd
        }
        notifyListeners(*listeners.toTypedArray())
        return Empty.getDefaultInstance()
    }

    private fun notifyListeners(vararg listeners: Channel<ScoreServiceProto.ScoresResponse>) {
        val allScores = ScoreServiceProto.ScoresResponse.newBuilder()
                .addAllScores(scoresMap.entries.map { (id, score) ->
                    ScoreServiceProto.Score.newBuilder()
                            .setUserId(id)
                            .setScore(score)
                            .build()
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

}