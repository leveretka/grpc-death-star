package com.example.deathstarclient

import javax.servlet.annotation.WebInitParam
import javax.servlet.annotation.WebServlet

@WebServlet(
        asyncSupported = true,
        urlPatterns = ["/*", "/VAADIN/*"],
        initParams = [WebInitParam(name="ui", value="com.example.deathstarclient.MainUI")])
class DeathStarClientServlet : com.vaadin.server.VaadinServlet()
