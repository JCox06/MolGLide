package uk.co.jcox.chemvis.application.chemengine

enum class BondOrder (val number: Int) {
    SINGLE(1),
    DOUBLE(2),
    ;


    companion object {
        val standardIncrements = listOf(SINGLE, DOUBLE)

        fun increment(current: BondOrder) : BondOrder {
            val index = standardIncrements.indexOf(current)
            val newIndex = index + 1
            if (newIndex < 0 || newIndex > standardIncrements.lastIndex) {
                return SINGLE
            }
            return standardIncrements[newIndex]
        }

        fun decrement(current: BondOrder) : BondOrder {
            val index = standardIncrements.indexOf(current)
            val newIndex = index - 1
            if (newIndex < 0 || newIndex > standardIncrements.lastIndex) {
                return SINGLE
            }
            return standardIncrements[newIndex]
        }
    }
}