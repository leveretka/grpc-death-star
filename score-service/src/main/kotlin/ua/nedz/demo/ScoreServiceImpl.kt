package ua.nedz.demo

import com.google.protobuf.Empty
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import ua.nedz.grpc.ScoreServiceGrpcKt
import ua.nedz.grpc.ScoreServiceProto

class ScoreServiceImpl: ScoreServiceGrpcKt.ScoreServiceCoroutineImplBase() {

    private val scoresMap = mutableMapOf<String, Long>()
    private val listeners = mutableListOf<FlowCollector<ScoreServiceProto.ScoresResponse>>()

    override suspend fun addScore(request: ScoreServiceProto.AddScoreRequest): Empty {
        scoresMap.putIfAbsent(request.userName, 0)
        scoresMap[request.userName]?.let {
            scoresMap[request.userName] = it + request.toAdd
        }
        notifyListeners(*listeners.toTypedArray())
        return Empty.getDefaultInstance()
    }

    private fun notifyListeners(vararg listeners: FlowCollector<ScoreServiceProto.ScoresResponse>) {
        val allScores = ScoreServiceProto.ScoresResponse.newBuilder()
                .addAllScores(scoresMap.entries.sortedByDescending { it.value }.map { (id, value) ->
                    Score {
                        userName = id
                        score = value
                    }
                })
                .build()

        listeners.forEach {
            GlobalScope.launch { it.emit(allScores) }
        }
    }

    @ExperimentalCoroutinesApi
    override fun scores(request: Empty): Flow<ScoreServiceProto.ScoresResponse> = flow {
        listeners.add(this)
        notifyListeners(this)
    }

    private fun Score(init: ScoreServiceProto.Score.Builder.() -> Unit) =
            ScoreServiceProto.Score.newBuilder()
                    .apply(init)
                    .build()

}