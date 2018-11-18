package com.example.deathstarclient

import com.vaadin.navigator.View
import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent
import com.vaadin.server.FileResource
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ua.nedz.grpc.PlanetProto
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

        logo = Image("", FileResource(File("$basePath/WEB-INF/images/GrpcDeathStarLogo.png")))
        logo.setWidth("330px")

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
            while (true) {
                println("Inside launched planets coroutine")
                val planetsInGame = planets.receive()
                current.access { game.removeAllComponents() }
                planetsInGame.planetsList.forEach { planet ->
                    println("received planet")
                    val planetImg = Image("", FileResource(File(
                            "$basePath/WEB-INF/images/planets/planet${planet.img}.png")))
                    planetImg.description = planet.name
                    planetImg.caption = "${planet.weight}"
                    planetImg.addClickListener {
                        val curUser = VaadinSession.getCurrent().getAttribute("user").toString()
                        GlobalScope.launch {
                            planets.send(PlanetProto.DestroyPlanetRequest.newBuilder()
                                    .setUserName(curUser)
                                    .setPlanetId(planet.planetId)
                                    .setWeight(planet.weight)
                                    .build())
                        }
                    }
                    current.access {
                        planetImg.setWidth("60px")
                        game.addComponent(planetImg)
                    }
                }
            }
        }

        GlobalScope.launch {
            while (true) {
                val log = logs.receive()
                current.access {
                    logsArea.value += "${log.message}\n"
                }
            }
        }

        GlobalScope.launch {
            while (true) {
                val scoresResponse = scores.receive()
                current.access {
                    scoresTable.removeAllComponents()
                    scoresTable.addComponents(Label("User"), Label("Score"))
                    scoresResponse.scoresList.forEach {
                        scoresTable.addComponents(Label(it.userName), Label("${it.score}"))
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
