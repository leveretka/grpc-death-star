package ua.nedz.demo

import java.util.concurrent.ConcurrentHashMap

object PlanetRepo {

    private val planetsList = ConcurrentHashMap<Long, Planet>()

    fun initialPlanets() {
        println("Generating initial planets")
        for (i in 1..60)
            planetsList[i + 500L] = Planet(i + 500L, randomName(), randomWeight(), randomImg())

        planetsList.values.forEach { println("Generated ${it.name}") }
    }

    fun getPlanetById(planetId: Long) = planetsList[planetId]

    fun getAllPlanets() : List<Planet> {
        println("Inside repo")
        return planetsList.values.filter { it.isAlive.get() }
    }
    fun deletePlanet(id: Long) : Boolean {
        println("Inside Repo Remove planet")
        println("Before: ${planetsList[id]}")
        val planet = planetsList[id]
        val result = planet?.isAlive?.compareAndSet(true, false) ?: false
        println("After: ${planetsList[id]}")
        return result
    }

    fun insertPlanet(planet: Planet) = planetsList.putIfAbsent(planet.id, planet)

    val names = listOf("Abafar", "Ahch-To", "Akiva", "Alderaan", "Ando", "Anoat", "Atollon", "Batuu", "Bespin",
            "Cantonica", "Castilon", "Cato Neimoidia", "Chandrila", "Christophsis", "Concord Dawn", "Corellia",
            "Coruscant", "Crait", "D'Qar", "Dagobah", "Dantooine", "Dathomir", "Devaron", "Eadu", "Endor", "Felucia",
            "Florrum", "Fondor", "Geonosis", "Hosnian Prime", "Hoth", "Iego", "Ilum", "Iridonia", "Jakku", "Jedha",
            "Kamino", "Kashyyyk", "Kessel", "Kuat", "Lah'mu", "Lira San", "Lothal", "Lotho Minor", "Malachor",
            "Malastare", "Mandalore", "Maridun", "Mimban", "Mon Cala", "Moraband", "Mortis", "Mustafar", "Mygeeto",
            "Naboo", "Nal Hutta", "Onderon", "Ord Mantell", "Polis Massa", "Pillio", "Rishi", "Rodia", "Ruusan",
            "Ryloth", "Saleucami", "Savareen", "Scarif", "Shili", "Starkiller Base", "Subterrel", "Sullust",
            "Abregado-rae", "Alzoc III", "Ambria", "Anoth", "Arkania", "Bakura", "Bonadan", "Borleias", "Byss",
            "Carida", "Da Soocha V", "Drall", "Dromund Kaas", "Dxun", "Hapes", "Honoghr", "Ithor", "J't'p'tan",
            "Khomm", "Korriban", "Kothlis", "Lwhekk", "Muunilinst", "Myrkr", "N'zoth", "Nkllon", "Ralltiir",
            "Rattatak", "Sacorria", "Selonia", "Thyferra", "Toprawa", "Vortex", "Wayland", "Zonama Sekot",
            "Abregado-rae", "Alzoc III", "Ambria", "Anoth", "Arkania", "Bakura", "Bonadan", "Borleias", "Byss",
            "Carida", "Da Soocha V", "Drall", "Dromund Kaas", "Dxun", "Hapes", "Honoghr", "Ithor", "J't'p'tan",
            "Khomm", "Korriban", "Kothlis", "Lwhekk", "Muunilinst", "Myrkr", "N'zoth", "Nkllon", "Ralltiir",
            "Rattatak", "Sacorria", "Selonia", "Thyferra", "Toprawa", "Vortex", "Wayland", "Zonama Sekot",
            "Abregado-rae", "Alzoc III", "Ambria", "Anoth", "Arkania", "Bakura", "Bonadan", "Borleias", "Byss",
            "Carida", "Da Soocha V", "Drall", "Dromund Kaas", "Dxun", "Hapes", "Honoghr", "Ithor", "J't'p'tan",
            "Khomm", "Korriban", "Kothlis", "Lwhekk", "Muunilinst", "Myrkr", "N'zoth", "Nkllon", "Ralltiir",
            "Rattatak", "Sacorria", "Selonia")

}