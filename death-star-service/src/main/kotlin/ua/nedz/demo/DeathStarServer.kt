package ua.nedz.demo

import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.ServerInterceptors

fun main(args: Array<String>) {
    val server = DeathStarServer()
    server.start()
    server.blockUntilShutdown()
}

class DeathStarServer (private val port: Int = 50051, private val serverBuilder: ServerBuilder<*> = ServerBuilder.forPort(port)) {
    private lateinit var server: Server

    fun start() {
        server = serverBuilder
//                .addService(ServerInterceptors.intercept(DeathStarServiceImpl(), UnknownStatusIntertceptor()))
                .addService(ServerInterceptors.intercept(DeathStarServiceKrotoImpl(), UnknownStatusIntertceptor()))
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