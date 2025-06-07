package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.cvengine.RawInput
import uk.co.jcox.chemvis.cvengine.RawInput.NULL

enum class AtomInsert(val symbol: String, val hydrogenable: Boolean) {
    CARBON("C", true),
    HYDROGEN("H", false),
    CHLORINE("Cl", false),
    OXYGEN("O", false),

    ;

}