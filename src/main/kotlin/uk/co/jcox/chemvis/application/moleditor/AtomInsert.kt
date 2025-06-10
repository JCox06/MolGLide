package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.RawInput.NULL

enum class AtomInsert(val symbol: String, val hydrogenable: Boolean) {
    //todo Make some of these more hydrogenable like nitrogen and oxygen
    CARBON("C", true),
    HYDROGEN("H", false),
    CHLORINE("Cl", false),
    OXYGEN("O", true),
    NITROGEN("N", true),
    PHOSPHORUS("P", false),
    FLUORINE("F", false),
    BROMINE("Br", false),
    IODINE("I", false)

    ;

    companion object {

        private val symbolCache = AtomInsert.entries.associateBy { it.symbol }

        fun fromSymbol(symbol: String) : AtomInsert {
            val key = symbolCache[symbol]
            return key ?: CARBON
        }
    }

}