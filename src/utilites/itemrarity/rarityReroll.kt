package utilites.itemrarity

import estaticos.AtlantaMain
import estaticos.Constantes
import estaticos.GestorSalida
import estaticos.Mundo
import estaticos.database.GestorSQL
import variables.objeto.Objeto
import variables.objeto.ObjetoModelo
import variables.personaje.Personaje

object rarityReroll {
    private fun reroll(obj: Objeto?, capacityBase: ObjetoModelo.CAPACIDAD_STATS, capacityRarity: ObjetoModelo.CAPACIDAD_STATS = ObjetoModelo.CAPACIDAD_STATS.RANDOM): Boolean {
        if (!AtlantaMain.RARITY_SYSTEM) return false
        obj ?: return false
        val rarity = getRarity(obj)
        rarity ?: return false
        obj.objModelo?.generarStatsModelo(capacityBase)?.let { obj.convertirStringAStats(it) }
        var s = obj.convertirStatsAString(false)
        s += "," + obj.generateStats(rarity.getstats(obj.objModelo?.nivel?.toInt()
                ?: 1), capacityRarity)
        obj.convertirStringAStats_Base(s)
        return true
    }

    private fun perfectreroll(obj: Objeto?): Boolean {
        if (!AtlantaMain.RARITY_SYSTEM) return false
        obj ?: return false
        val rarity = getRarity(obj)
        rarity ?: return false
        var permanentstats = "${rarity.getcolorstat()},"
        var cs1 = 0
        var cs2 = 0
        val listofstats = mutableListOf<Int>()
        var objcopy = obj.clonarObjeto(1, -1)
        objcopy.objModelo?.generarStatsModelo(ObjetoModelo.CAPACIDAD_STATS.MAXIMO)?.let { objcopy.convertirStringAStats_Base(it) }
        obj.stats._statsIDs?.forEach { e, v ->
            val statoriginal = objcopy.stats._statsIDs?.get(e) ?: 0
            if (v - statoriginal > 0) {
                listofstats.add(e)
            }
        }
        var c = 0
        while (cs1 < rarity.cant1S || cs2 < rarity.cant2S) {
            if (listofstats.isNotEmpty() && c < 5) { // If c > 5 is because the stat was not detected for the reroller
                val s1 = rarity.searchFirststatsSlot1(obj.objModelo?.nivel ?: 1, listofstats)
                val s2 = rarity.searchFirststatsSlot2(obj.objModelo?.nivel ?: 1, listofstats)
                if (s1.isNotBlank() && cs1 < rarity.cant1S) {
                    listofstats.remove(s1.split("#").first().toInt(radix = 16)) // remove from the list
                    permanentstats += "$s1,"
                    cs1 += 1
                    continue
                } else if (s2.isNotBlank() && cs2 < rarity.cant2S) {
                    listofstats.remove(s2.split("#").first().toInt(16))
                    permanentstats += "$s2,"
                    cs2 += 1
                    continue
                } else {
                    c += 1
                }
            } else {
                when {
                    cs1 < rarity.cant1S -> {
                        permanentstats += "${rarity.getnumberofstatsSlot1(obj.objModelo?.nivel?.toInt() ?: 1, rarity.cant1S - cs1)},"
                        cs1 = rarity.cant1S // Is fulled
                    }
                    cs2 < rarity.cant2S -> {
                        permanentstats += "${rarity.getnumberofstatsSlot2(obj.objModelo?.nivel?.toInt() ?: 1, rarity.cant2S - cs2)},"
                        cs2 = rarity.cant2S
                    }
                }
            }
        }
        permanentstats = permanentstats.dropLastWhile { it == ',' }
        objcopy = obj.clonarObjeto(1, -1) // Another Copy to make a little comparison
        obj.objModelo?.generarStatsModelo(ObjetoModelo.CAPACIDAD_STATS.MAXIMO)?.let { obj.convertirStringAStats(it) }
        var semifinal = obj.convertirStatsAString(false)
        semifinal += ",${obj.generateStats(permanentstats, ObjetoModelo.CAPACIDAD_STATS.MAXIMO)}"
        obj.convertirStringAStats_Base(semifinal)
        objcopy.stats._statsIDs?.forEach { e, v ->
            if (obj.stats._statsIDs?.get(e) ?: Int.MIN_VALUE < v) { // For be able to stack additionals EXO
                obj.stats._statsIDs?.set(e, v)
            }
        }
        return true
    }

    fun rerollALL(obj: Objeto?): Boolean {
        return reroll(obj, ObjetoModelo.CAPACIDAD_STATS.RANDOM, ObjetoModelo.CAPACIDAD_STATS.RANDOM)
    }

    fun baseperfect_rollrarity(obj: Objeto?): Boolean {
        return reroll(obj, ObjetoModelo.CAPACIDAD_STATS.MAXIMO, ObjetoModelo.CAPACIDAD_STATS.RANDOM)
    }

    fun identifyColorRarity(obj: Objeto?): Int {
        if (obj != null) {
            if (obj.stats.tieneStatTexto(Constantes.STAT_COLOR_NOMBRE_OBJETO)) {
                return obj.stats.getStatTexto(Constantes.STAT_COLOR_NOMBRE_OBJETO)?.split("#")?.get(0)?.toInt()!!
            }
        }
        return -1 // Not haves color
    }

    fun getRarity(obj: Objeto?): rarityTemplate? {
        val rarityColor = identifyColorRarity(obj)
        var rarity: rarityTemplate? = null
        for (template in Mundo.RARITYTEMPLATES.values) {
            if (template.color == rarityColor) {
                rarity = template
                break
            }
        }
        return rarity
    }

    fun canReroll(obj: Objeto?, personaje: Personaje?, typeofroll: Int = 2): Boolean {
        personaje ?: return false
        obj ?: return false
        val modelo = obj.objModelo ?: return false
        val tipo = modelo.tipo
        if (!AtlantaMain.RARITY_TYPES.contains(tipo.toInt())) return false
        val runeID = GestorSQL.GET_REROLL_REQUIRED_OBJ_ID(obj, typeofroll)
        if (runeID == -1) return false
        return personaje.tieneObjetoIDModelo(runeID)
    }

    fun Reroll(obj: Objeto?, personaje: Personaje?, typeofroll: Int): Boolean {
        personaje ?: return false
        obj ?: return false
        val runeID = GestorSQL.GET_REROLL_REQUIRED_OBJ_ID(obj, typeofroll)
        if (runeID == -1) return false
        var r = false
        if (personaje.tieneObjetoIDModelo(runeID)) { // Verify if has the obj
            when (typeofroll) {
                1 -> {
                    if (rerollALL(obj)) {
                        r = true
                    }
                }
                2 -> {
                    if (baseperfect_rollrarity(obj)) {
                        r = true
                    }
                }
                3 -> {
                    if (perfectreroll(obj)) {
                        r = true
                    }
                }
            }

        }
        if (r) {
            personaje.tenerYEliminarObjPorModYCant(runeID, 1) // remove the required obj
            GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(personaje, obj)
        }
        return r
    }
}