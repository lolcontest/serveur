package utilites.itemrarity

import estaticos.Constantes
import estaticos.Mundo
import variables.objeto.ObjetoModelo
import kotlin.math.max

class rarityTemplate(val id: Int, val name: String, val color: Int, val dropProb: Int, val cant1S: Int,
                     val cant2S: Int) {
    fun getdropProb(): Double {
        return (dropProb.toDouble() / 100) // Changes the int to decimal prob
    }

    val slot1: MutableMap<Int?, String> = mutableMapOf()
    val slot2: MutableMap<Int?, String> = mutableMapOf()

    fun addslot1(level: Int, stats: String) {
        slot1[level] = stats
    }

    fun addslot2(level: Int, stats: String) {
        slot2[level] = stats
    }

    private fun getmaxkeyMap(m: MutableMap<Int?, String>): Int {
        var prev = Int.MIN_VALUE
        for (key in m.keys) {
            prev = max(key ?: Int.MIN_VALUE, prev)
        }
        return prev
    }

    fun getcolorstat(): String {
        return if (color != -1) {
            Integer.toHexString(Constantes.STAT_COLOR_NOMBRE_OBJETO) + "#$color"
        } else ""
    }

    fun getstatsSlot1(level: Int): String {
        return if (max(level, getmaxkeyMap(slot1)) == level) {
            slot1[getmaxkeyMap(slot1)] ?: ""
        } else {
            slot1[level] ?: ""
        }
    }

    fun searchFirststatsSlot1(level: Short, list: MutableList<Int>): String {
        val stats = getstatsSlot1(level.toInt())
        if (stats.isNotEmpty()) {
            for (s in stats.split(",")) {
                if (list.contains(s.split("#").first().toInt(radix = 16))) {
                    return s
                }
            }
        }
        return ""
    }

    fun searchFirststatsSlot2(level: Short, list: MutableList<Int>): String {
        val stats = getstatsSlot2(level.toInt())
        if (stats.isNotEmpty()) {
            for (s in stats.split(",")) {
                if (list.contains(s.split("#").first().toInt(radix = 16))) {
                    return s
                }
            }
        }
        return ""
    }

    private fun getstatsSlot2(level: Int): String {
        return if (max(level, getmaxkeyMap(slot2)) == level) {
            slot2[getmaxkeyMap(slot2)] ?: ""
        } else {
            slot2[level] ?: ""
        }
    }

    fun getnumberofstatsSlot1(level: Int, cant: Int): String {
        var stats = ""
        val slot = getstatsSlot1(level).split(",")
        var c = 0
        val statsalready = emptyArray<Int>().toMutableList()
        try {
            while (c < cant) {
                val stat = slot.random()
                val statid = ObjetoModelo.statSimiliar(stat.split("#").first().toInt(radix = 16))
                if (statsalready.contains(statid)) continue
                if (Math.random() <= rarityProb.getstatprob(statid)) {
                    statsalready.add(statid)
                    stats += "$stat,"
                    c += 1
                }
            }
        } catch (e: Exception) {
        }
        return stats.dropLastWhile { it == ',' }
    }

    fun getnumberofstatsSlot2(level: Int, cant: Int): String {
        var stats = ""
        val slot = getstatsSlot2(level).split(",")
        var c = 0
        val statsalready = emptyArray<Int>().toMutableList()
        try {
            while (c < cant) {
                val stat = slot.random()
                val statid = ObjetoModelo.statSimiliar(stat.split("#").first().toInt(radix = 16))
                if (statsalready.contains(statid)) continue
                if (Math.random() <= rarityProb.getstatprob(statid)) {
                    statsalready.add(statid)
                    stats += "$stat,"
                    c += 1
                }
            }
        } catch (e: Exception) {
        }
        return stats.dropLastWhile { it == ',' }
    }

    fun getstats(level: Int): String {
        val color = getcolorstat()
        var stats = if (color.isNotBlank()) getcolorstat() + "," else ""
        val slot1S = getstatsSlot1(level).split(",")
        val slot2S = getstatsSlot2(level).split(",")
        var c = 0
        val statsalready = emptyArray<Int>().toMutableList()
        try {
            while (c < cant1S) {
                val stat = slot1S.random()
                val statid = ObjetoModelo.statSimiliar(stat.split("#").first().toInt(radix = 16))
                if (statsalready.contains(statid)) continue
                if (Math.random() <= rarityProb.getstatprob(statid)) {
                    statsalready.add(statid)
                    stats += "$stat,"
                    c += 1
                }
            }
        } catch (e: Exception) {
        }
        c = 0
        try {
            while (c < cant2S) {
                val stat = slot2S.random()
                val statid = ObjetoModelo.statSimiliar(stat.split("#").first().toInt(radix = 16))
                if (statsalready.contains(statid)) continue
                if (Math.random() <= rarityProb.getstatprob(statid)) {
                    statsalready.add(statid)
                    stats += "$stat,"
                    c += 1
                }
            }
        } catch (e: Exception) {
        }
        return stats.dropLastWhile { it == ',' }
    }

    init {
        Mundo.RARITYTEMPLATES[id] = this // Adding that
    }
}