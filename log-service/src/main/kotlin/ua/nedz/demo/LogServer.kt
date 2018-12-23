package ua.nedz.demo

import io.grpc.Server
import io.grpc.ServerBuilder

fun main(args: Array<String>) {
    val server = LogServer()
    server.start()
    server.blockUntilShutdown()
}

class LogServer (private val port: Int = 50081, private val serverBuilder: ServerBuilder<*> = ServerBuilder.forPort(port)) {
    private lateinit var server: Server

    fun start() {
        server = serverBuilder
                .addService(LogServiceImpl())
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