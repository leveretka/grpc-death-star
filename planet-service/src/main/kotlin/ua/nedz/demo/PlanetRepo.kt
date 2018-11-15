package ua.nedz.demo

import com.firefly.db.SQLClient
import com.firefly.db.jdbc.JDBCClient
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

object PlanetRepo {
/*    private val sqlClient: SQLClient

        init {
            val config = HikariConfig()
            config.jdbcUrl = "jdbc:mysql://192.168.0.102:3306/death_star_db"
            config.driverClassName = "com.mysql.jdbc.Driver"
            config.isAutoCommit = false
            config.username = "root"
            config.password = "test"
            val ds = HikariDataSource(config)
            sqlClient = JDBCClient(ds)
        */


    private val planetsList = mutableListOf<Planet>()

    fun getAllPlanets() = planetsList.filter { it.isAlive }
    fun deletePlanet(id: Long) = planetsList.filter { it.id == id }.map { it.copy(isAlive = false) }
    fun insertPlanet(planet: Planet) = planetsList.add(planet)

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