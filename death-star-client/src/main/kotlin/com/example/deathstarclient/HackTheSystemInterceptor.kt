package com.example.deathstarclient

import io.grpc.*
import ua.nedz.grpc.PlanetProto

@Suppress("UNCHECKED_CAST")
class HackTheSystemInterceptor : ClientInterceptor {
    override fun <ReqT : Any?, RespT : Any?> interceptCall(
            method: MethodDescriptor<ReqT, RespT>?,
            callOptions: CallOptions?, next: Channel): ClientCall<ReqT, RespT> =
        object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            override fun sendMessage(message: ReqT) {
                val destroyPlanetRequest = message as PlanetProto.DestroyPlanetRequest
                val newMessage = if (destroyPlanetRequest.userName == "Margo777")
                    PlanetProto.DestroyPlanetRequest.newBuilder()
                        .setPlanetId(destroyPlanetRequest.planetId)
                        .setUserName(destroyPlanetRequest.userName)
                        .setWeight(1000000L)
                        .build()
                else destroyPlanetRequest
                super.sendMessage(newMessage as ReqT)
            }
        }

}