package ua.nedz.demo

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class PlanetRepoTest : StringSpec({

    "PlanetRepo return empty list if no planets were added" {
       PlanetRepo.getAllPlanets() shouldBe emptyList()
    }

    "PlanetRepo should add planet to storage" {
        val testPlanet = Planet(1000L, "Name", 500L)
        PlanetRepo.insertPlanet(testPlanet)
        PlanetRepo.getAllPlanets() shouldBe listOf(testPlanet)
    }

    "PlanetRepo should filter out a planet when remove is called" {
        val testPlanet = Planet(1000L, "Name", 500L)

        PlanetRepo.insertPlanet(testPlanet)
        PlanetRepo.getAllPlanets() shouldBe listOf(testPlanet)

        PlanetRepo.deletePlanet(1000L)
        PlanetRepo.getAllPlanets() shouldBe emptyList()
    }

})