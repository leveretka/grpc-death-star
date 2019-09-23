package ua.nedz.demo

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import ua.nedz.grpc.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class DeathStarServiceKrotoImpl : DeathStarServiceCoroutineGrpc.DeathStarServiceImplBase() {


    override val initialContext: CoroutineContext =
            Executors.newFixedThreadPool(16).asCoroutineDispatcher()

    private var planetTarget: String = System.getenv("PLANET_SERVICE_TARGET") ?: "localhost:50061"
    private var scoreTarget: String = System.getenv("SCORE_SERVICE_TARGET") ?: "localhost:50071"
    private var logTarget: String = System.getenv("LOG_SERVICE_TARGET") ?: "localhost:50081"

    private val planetChannel = channelForTarget(planetTarget)
    private val planetStub = PlanetServiceCoroutineGrpc.newStub(planetChannel)

    private val scoreChannel = channelForTarget(scoreTarget)
    private val scoreStub = ScoreServiceCoroutineGrpc.newStub(scoreChannel)

    private val logChannel = channelForTarget(logTarget)
    private val logStub = LogServiceCoroutineGrpc.newStub(logChannel)

    private val planetDestructionBroadcast = BroadcastChannel<PlanetProto.Planets>(Channel.BUFFERED)

    private val planetDestroyer = CoroutineScope(initialContext).actor<PlanetProto.DestroyPlanetRequest> {

        consumeEach { request ->
            val wasRemoved = planetStub.removePlanet{ planetId = request.planetId }
            if (wasRemoved.result) {
                scoreStub.addScore {
                    userName = request.userName
                    toAdd = request.weight
                }
                logStub.destroyedPlanet(request)

                val newPlanet = planetStub.generateNewPlanet()

                logStub.newPlanet(newPlanet)

                launch {
                    planetDestructionBroadcast.send {
                        addPlanets(populateWithCoordinates(newPlanet, request.coordinates.x, request.coordinates.y))
                    }
                }
            }
        }
    }


    override suspend fun destroy(
            requestChannel: ReceiveChannel<PlanetProto.DestroyPlanetRequest>,
            responseChannel: SendChannel<PlanetProto.Planets>
    ) = coroutineScope {

        val allPlanets = populateWithCoordinates(planetStub.getAllPlanets())
        responseChannel.send(allPlanets)

        launch {
            requestChannel.consumeEach { planetDestroyer.send(it) }
        }

        val subscription = planetDestructionBroadcast.openSubscription()

        subscription.consumeEach { responseChannel.send(it) }
    }

}