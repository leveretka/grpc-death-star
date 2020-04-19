package com.example.deathstarclient

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.FileResource
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.*
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

    private lateinit var sendFlow: FlowCollector<PlanetProto.DestroyPlanetRequest>

    @ObsoleteCoroutinesApi
    override fun enter(event: ViewChangeEvent?) {
        uName = VaadinSession.getCurrent().getAttribute("user").toString()
        defaultComponentAlignment = Alignment.TOP_CENTER
        addComponents(logo, game, services)

        val destroyFlow = flow<PlanetProto.DestroyPlanetRequest> { sendFlow = this }

        val (planets, logs, scores) = client.join(uName, destroyFlow)

        val current = UI.getCurrent()

        GlobalScope.launch {
            receivePlanets(planets, current)
        }
        GlobalScope.launch {
            println("Inside receive Logs")
            logs.collect {
                current.access { logsArea.value += "${it.message}\n" }
            }
        }
        GlobalScope.launch {
            receiveScores(scores, current)
        }
    }

    private suspend fun receiveScores(scores: Flow<ScoreServiceProto.ScoresResponse>, current: UI) {
        println("Inside receive Scores")
        scores.collect { scoresResponse ->
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
    private suspend fun receivePlanets(planets: Flow<PlanetProto.Planets>, current: UI) {
        println("Inside receive Planet")
        planets.collect {
            println("Inside receive Planet collect")

            planets.collect {
                println("PlanetsInGame: ${it.planetsList}")

                it.planetsList.forEach { planet ->
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
                                GlobalScope.launch {
                                    if (client.succesfulDestroyAttempt(planet)) {
                                        sendFlow.emit(client.DestroyPlanetRequest {
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
        }
    }

    private fun AbstractComponent.baseSettings() {
        setWidth("100%")
        setHeight("100%")
        defaultComponentAlignment = Alignment.MIDDLE_CENTER
    }
}
