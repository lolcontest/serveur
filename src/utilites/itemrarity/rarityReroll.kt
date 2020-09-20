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
    private fun reroll(obj: Objeto?, capacity: ObjetoModelo.CAPACIDAD_STATS): Boolean {
        if (!AtlantaMain.RARITY_SYSTEM) return false
        obj ?: return false
        val rarity = getRarity(obj)
        rarity ?: return false
        obj.objModelo?.generarStatsModelo(capacity)?.let { obj.convertirStringAStats(it) }
        var s = obj.convertirStatsAString(false)
        s += "," + obj.generateStats(rarity.getstats(obj.objModelo?.nivel?.toInt()
                ?: 1), ObjetoModelo.CAPACIDAD_STATS.RANDOM)
        obj.convertirStringAStats_Base(s)
        return true
    }

    fun normalroll(obj: Objeto?): Boolean {
        return reroll(obj, ObjetoModelo.CAPACIDAD_STATS.RANDOM)
    }

    fun minroll(obj: Objeto?): Boolean {
        return reroll(obj, ObjetoModelo.CAPACIDAD_STATS.MINIMO)
    }

    fun maxroll(obj: Objeto?): Boolean {
        return reroll(obj, ObjetoModelo.CAPACIDAD_STATS.MAXIMO)
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

    fun canReroll(obj: Objeto?, personaje: Personaje?): Boolean {
        personaje ?: return false
        obj ?: return false
        val modelo = obj.objModelo ?: return false
        val tipo = modelo.tipo
        if (!AtlantaMain.RARITY_TYPES.contains(tipo.toInt())) return false
        val runeID = GestorSQL.GET_REROLL_REQUIRED_OBJ_ID(obj)
        if (runeID == -1) return false
        return personaje.tieneObjetoIDModelo(runeID)
    }

    fun Reroll(obj: Objeto?, personaje: Personaje?): Boolean {
        personaje ?: return false
        obj ?: return false
        val runeID = GestorSQL.GET_REROLL_REQUIRED_OBJ_ID(obj)
        if (runeID == -1) return false
        if (personaje.tieneObjetoIDModelo(runeID)) { // Verify if has the obj
            if (maxroll(obj)) { // reroll
                personaje.tenerYEliminarObjPorModYCant(runeID, 1) // remove the required obj
                GestorSalida.ENVIAR_OCK_ACTUALIZA_OBJETO(personaje, obj)
                return true
            }
        }
        return false
    }
}