package com.example.deathstarclient

import com.vaadin.annotations.Theme
import com.vaadin.server.VaadinRequest
import com.vaadin.ui.Label
import com.vaadin.ui.UI

@Theme("DeathStarClient")
class DeathStarClientUI : UI() {

    override fun init(request: VaadinRequest) {
        val lbl = Label("Hello vaadin")
        content = lbl
    }
}
