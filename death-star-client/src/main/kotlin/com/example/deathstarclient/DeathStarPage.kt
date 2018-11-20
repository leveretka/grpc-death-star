package com.example.deathstarclient

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.FileResource
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import ua.nedz.grpc.DeathStarServiceGrpcKt
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
            caption = "Destroy Planets by clicking!"
            baseSettings()
            styleName = "game"
        }

    }

    override fun enter(event: ViewChangeEvent?) {
        userName = VaadinSession.getCurrent().getAttribute("user").toString()
        defaultComponentAlignment = Alignment.TOP_CENTER
        addComponents(logo, game, services)

        val (planets, logs, scores)
                = client.join(userName)

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

    private suspend fun receivePlanets(planets: DeathStarServiceGrpcKt.ManyToManyCall<PlanetProto.DestroyPlanetRequest, PlanetProto.Planets>, current: UI) {
        for (planetsInGame in planets) {
            current.access { game.removeAllComponents() }
            planetsInGame.planetsList.forEach { planet ->
                val planetImg = Image("", FileResource(File(
                        "$basePath/WEB-INF/images/planets/planet${planet.img}.png")))
                with(planetImg) {
                    description = planet.name
                    caption = "${planet.weight}"
                    current.access {
                        setWidth("60px")
                        styleName = "planet-img"
                        game.addComponent(this)
                    }
                    addClickListener {
                        if (client.succesfulDestroyAttempt(planet)) {
                            val curUser = VaadinSession.getCurrent().getAttribute("user").toString()
                            GlobalScope.launch {
                                planets.send(DestroyPlanetRequest {
                                    userName = curUser
                                    planetId = planet.planetId
                                    weight = planet.weight
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

    private fun DestroyPlanetRequest(init: PlanetProto.DestroyPlanetRequest.Builder.() -> Unit) =
        PlanetProto.DestroyPlanetRequest.newBuilder()
                .apply(init)
                .build()

}
