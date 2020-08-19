package utilites.itemrarity

object rarityProb {
    val statsprob: MutableMap<Int?, Double> = mutableMapOf()
    fun getstatprob(id: Int): Double { // Its for no use excesive ram memory
        return statsprob[id] ?: 0.0
    }
}