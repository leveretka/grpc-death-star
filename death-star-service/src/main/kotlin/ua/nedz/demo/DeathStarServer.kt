package ua.nedz.demo

import io.grpc.Server
import io.grpc.ServerBuilder

fun main(args: Array<String>) {
    val server = DeathStarServer()
    server.start()
    server.blockUntilShutdown()
}

class DeathStarServer (val port: Int = 50051, val serverBuilder: ServerBuilder<*> = ServerBuilder.forPort(port)) {
    lateinit var server: Server

    fun start() {
        server = serverBuilder
                //.addService(ChatServiceImpl())
                //.addService(ManualFlowChatServiceImpl())
                .build()
                .start()
        println("Server started!")
    }

    /**
     * Await termination on the ua.nedz.demo.main thread since the grpc library uses daemon threads.
     */
    @Throws(InterruptedException::class)
    fun blockUntilShutdown() {
        server.awaitTermination()
    }

    fun stop() {
        if (::server.isInitialized)
            server.shutdown()
    }
}