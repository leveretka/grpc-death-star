package ua.nedz.demo

import io.grpc.*
import kotlin.random.Random

class NotThatEasyInterceptor: ServerInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(call: ServerCall<ReqT, RespT>?,
                                                           headers: Metadata?,
                                                           next: ServerCallHandler<ReqT, RespT>)
            : ServerCall.Listener<ReqT> {

        val rand = Random.nextInt(0, 100)
        if (rand < 10)
            return next.startCall(call, headers)

        return object : ServerCall.Listener<ReqT>() {}
    }
}