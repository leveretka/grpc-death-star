package ua.nedz.demo

import java.lang.RuntimeException

class PlanetNotFoundException(planetId: Long) : RuntimeException("Planet with id: $planetId was not found")
