package uk.co.jcox.chemvis.application.moleditorstate

enum class AtomInsert(val symbol: String, val hydrogenable: Boolean) {
    CARBON("C", true),
    HYDROGEN("H", false),
    OXYGEN("O", true),
    NITROGEN("N", true),
    PHOSPHORUS("P", true),
    FLUORINE("F", false),
    CHLORINE("Cl", false),
    BROMINE("Br", false),
    IODINE("I", false),
    MAGNESIUM("Mg", false),
    LITHIUM("Li", false),
    SULPHUR("S", true)

    ;

    companion object {

        private val symbolCache = entries.associateBy { it.symbol }

        fun fromSymbol(symbol: String) : AtomInsert {
            val key = symbolCache[symbol]
            return key ?: CARBON
        }
    }

}