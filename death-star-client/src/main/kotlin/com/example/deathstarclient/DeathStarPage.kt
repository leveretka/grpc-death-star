package com.example.deathstarclient

import com.github.marcoferrer.krotoplus.coroutines.client.ClientBidiCallChannel
import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.FileResource
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.*
import io.rouz.grpc.ManyToManyCall
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import ua.nedz.grpc.PlanetProto
import ua.nedz.grpc.ScoreServiceProto
import java.io.File


class DeathStarPage : VerticalLayout(), View {
    companion object {
        const val NAME = ""
    }

    private val client = DeathStarClient()

    private val game = GridLayout(10, 6)
    private val services = HorizontalLayout()
    private val scoresTable = GridLayout(2, 1)
    private val logsArea = TextArea()

    private var uName = VaadinSession.getCurrent().getAttribute("user").toString()
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
            caption = "Hello, $uName!"
            baseSettings()
            styleName = "game"
        }

    }

    override fun enter(event: ViewChangeEvent?) {
        uName = VaadinSession.getCurrent().getAttribute("user").toString()
        defaultComponentAlignment = Alignment.TOP_CENTER
        addComponents(logo, game, services)

        val (krotoPlanets, planets, logs, scores) = client.join(uName)

        val current = UI.getCurrent()

        GlobalScope.launch {
            receivePlanets(krotoPlanets, current)
        }
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

    @ObsoleteCoroutinesApi
    private suspend fun receivePlanets(planets: ClientBidiCallChannel<PlanetProto.DestroyPlanetRequest, PlanetProto.Planets>, current: UI) {
        val (inChannel, outChannel) = planets
        outChannel.consumeEach {
            it.planetsList.forEach { planet ->
                val planetImg = Image("", FileResource(File(
                        "$basePath/WEB-INF/images/planets/planet${planet.img}.png")))
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
                            GlobalScope.launch {
                                client.tryDestroy(planet, uName, inChannel, x, y)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun receivePlanets(planets: ManyToManyCall<PlanetProto.DestroyPlanetRequest, PlanetProto.Planets>, current: UI) {
        println("Inside receive planets")
        for (planetsInGame in planets) {
            println("PlanetsInGame: ${planetsInGame.planetsList}")
            planetsInGame.planetsList.forEach { planet ->
                println("planet is $planet")
                val planetImg = Image("", FileResource(File(
                        "$basePath/WEB-INF/images/planets/planet${planet.img}.png")))
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
                                planets.send(client.DestroyPlanetRequest {
                                    userName = uName
                                    planetId = planet.planetId
                                    weight = planet.weight
                                    coordinates = client.Coordinates {
                                        this.x = x
                                        this.y = y
                                    }
                                })
                            }

                        }
                    }
                }
            }
        }
    }

    private fun AbstractComponent.baseSettings() {
        setWidth("100%")
        setHeight("100%")
        defaultComponentAlignment = Alignment.MIDDLE_CENTER
    }


}
