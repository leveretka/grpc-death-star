package ua.nedz.demo

import com.google.protobuf.Empty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import ua.nedz.grpc.ScoreServiceGrpcKt
import ua.nedz.grpc.ScoreServiceProto

class ScoreServiceImpl: ScoreServiceGrpcKt.ScoreServiceCoroutineImplBase() {

    private val scoresMap = mutableMapOf<String, Long>()
    @OptIn(ExperimentalCoroutinesApi::class)
    private val listeners = mutableListOf<ProducerScope<ScoreServiceProto.ScoresResponse>>()

    override suspend fun addScore(request: ScoreServiceProto.AddScoreRequest): Empty {
        scoresMap.putIfAbsent(request.userName, 0)
        scoresMap[request.userName]?.let {
            scoresMap[request.userName] = it + request.toAdd
        }
        notifyListeners(*listeners.toTypedArray())
        return Empty.getDefaultInstance()
    }

    private fun notifyListeners(vararg listeners: ProducerScope<ScoreServiceProto.ScoresResponse>) {
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
    override fun scores(request: Empty): Flow<ScoreServiceProto.ScoresResponse> = channelFlow {
        listeners.add(this)
        notifyListeners(this)
        awaitClose {  }
    }

    private fun Score(init: ScoreServiceProto.Score.Builder.() -> Unit) =
            ScoreServiceProto.Score.newBuilder()
                    .apply(init)
                    .build()

}