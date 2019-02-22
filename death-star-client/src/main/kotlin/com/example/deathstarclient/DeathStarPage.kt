package com.example.deathstarclient

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.FileResource
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.*
import io.rouz.grpc.ManyToManyCall
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import ua.nedz.grpc.PlanetProto
import ua.nedz.grpc.ScoreServiceProto
import java.io.File
import java.util.concurrent.atomic.AtomicInteger


class DeathStarPage : VerticalLayout(), View {
    companion object {
        const val NAME = ""
    }

    val planetsCount = AtomicInteger(0)

    private val client = DeathStarClient()

    private val game = GridLayout(10, 6)
    private val services = HorizontalLayout()
    private val scoresTable = GridLayout(2, 1)
    private val logsArea = TextArea()

    private var userName = VaadinSession.getCurrent().getAttribute("user").toString()
    private var logo: Image

    private val basePath = VaadinService.getCurrent().baseDirectory.absolutePath

    init {
        addStyleName("death-star")

        logo = Image("", FileResource(File("$basePath/WEB-INF/images/GrpcDeathStarLogo.png")))
        with(logo) {
            setWidth("330px")
            addStyleName("logo")
        }

        with(services) {
            addComponents(scoresTable, logsArea)
            baseSettings()
        }
        with(scoresTable) {
            caption = "Scores"
            addComponents(Label("User"), Label("Score"))
            baseSettings()
        }
        with(logsArea) {
            caption = "Logs"
            baseSettings()
        }
        with(game) {
            caption = "Hello, $userName!"
            baseSettings()
            styleName = "game"
        }

    }

    override fun enter(event: ViewChangeEvent?) {
        userName = VaadinSession.getCurrent().getAttribute("user").toString()
        defaultComponentAlignment = Alignment.TOP_CENTER
        addComponents(logo, game, services)

        val (planets, logs, scores) = client.join(userName)

        val current = UI.getCurrent()

        GlobalScope.launch {
            receivePlanets(planets, current)
        }
        GlobalScope.launch {
            for (log in logs)
                current.access { logsArea.value += "${log.message}\n" }
        }
        GlobalScope.launch {
            receiveScores(scores, current)
        }
    }

    private suspend fun receiveScores(scores: ReceiveChannel<ScoreServiceProto.ScoresResponse>, current: UI) {
        for (scoresResponse in scores) {
            current.access {
                with(scoresTable) {
                    removeAllComponents()
                    addComponents(Label("User"), Label("Score"))
                    scoresResponse.scoresList.forEach { addComponents(Label(it.userName), Label("${it.score}")) }
                }
            }
        }
    }

    private suspend fun receivePlanets(planets: ManyToManyCall<PlanetProto.DestroyPlanetRequest, PlanetProto.Planets>, current: UI) {
        while (planetsCount.get() < 6)
            for (planetsInGame in planets) {
                planetsCount.incrementAndGet()
                planetsInGame.planetsList.forEach { planet ->
                    val planetImg = Image("", FileResource(File(
                            "$basePath/WEB-INF/images/planets_pxl/planet${planet.img}.png")))
                    with(planetImg) {
                        description = planet.name
                        caption = "${planet.weight}"
                        current.access {
                            setWidth("60px")
                            styleName = "planet-img"
                            val x = planet.coordinates.x
                            val y = planet.coordinates.y
                            val oldComponent = game.getComponent(x, y)
                            game.replaceComponent(oldComponent, this)
                            addClickListener {
                                if (client.succesfulDestroyAttempt(planet)) {
                                    val curUser = VaadinSession.getCurrent().getAttribute("user").toString()
                                    planets.send(DestroyPlanetRequest {
                                        userName = curUser
                                        planetId = planet.planetId
                                        weight = planet.weight
                                        coordinates = Coordinates {
                                            this.x = x
                                            this.y = y

                                        }
                                    })
                                }
                            }
                        }
                    }
                }
                planetsCount.decrementAndGet()
            }
    }

    private fun AbstractComponent.baseSettings() {
        setWidth("100%")
        setHeight("100%")
        defaultComponentAlignment = Alignment.MIDDLE_CENTER
    }

    private fun DestroyPlanetRequest(init: PlanetProto.DestroyPlanetRequest.Builder.() -> Unit) =
            PlanetProto.DestroyPlanetRequest.newBuilder()
                    .apply(init)
                    .build()

    private fun Coordinates(init: PlanetProto.Coordinates.Builder.() -> Unit) =
            PlanetProto.Coordinates.newBuilder()
                    .apply(init)
                    .build()

}
