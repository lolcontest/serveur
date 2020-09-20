package utilites.itemrarity

import estaticos.Constantes
import variables.oficio.StatOficio
import variables.personaje.Personaje

object rarityTable {
    fun startTable(personaje: Personaje?): Boolean {
        try {
            val statOficio: StatOficio = personaje?.getStatOficioPorTrabajo(Constantes.SKILL_REROLLER) ?: return false
            return statOficio.iniciarTrabajo(Constantes.SKILL_REROLLER, personaje, null, 2, personaje.celda)
        } catch (ignored: Exception) {
        }
        return false
    }
}