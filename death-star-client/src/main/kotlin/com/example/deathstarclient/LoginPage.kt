package com.example.deathstarclient

import com.vaadin.navigator.View
import com.vaadin.server.FileResource
import com.vaadin.server.Page
import com.vaadin.server.VaadinService
import com.vaadin.server.VaadinSession
import com.vaadin.ui.*
import java.io.File


class LoginPage : VerticalLayout(), View {

    companion object {
        const val NAME = ""
    }

    private val panel = Panel("Login")
    init {

        val basepath = VaadinService.getCurrent()
                .baseDirectory.absolutePath

        val resource = FileResource(File(
                "$basepath/WEB-INF/images/GrpcDeathStarLogo.png"))

        val image = Image("", resource)
        image.setWidth("345px")
        addComponent(image)

        panel.setSizeUndefined()
        addComponent(panel)
        val content = FormLayout()
        val username = TextField("Username")
        content.addComponent(username)

        val send = Button("Enter")
        send.addClickListener {
            VaadinSession.getCurrent().setAttribute("user", username.value)
            ui.navigator.addView(DeathStarPage.NAME, DeathStarPage::class.java)
            Page.getCurrent().uriFragment = "!${DeathStarPage.NAME}"
        }

        content.run {
            addComponent(send)
            setSizeUndefined()
            setMargin(true)
        }
        panel.content = content
        setComponentAlignment(panel, Alignment.MIDDLE_CENTER)
        setComponentAlignment(image, Alignment.MIDDLE_CENTER)
    }
}