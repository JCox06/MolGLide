package uk.co.jcox.chemvis.application.moleditorstate.tool

import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem
import uk.co.jcox.chemvis.application.moleditorstate.TemplateRingInsert

data class ToolboxContext (
    var atomInsert: AtomInsert,
    var stereoChem: StereoChem,
    var templateInsert: TemplateRingInsert,
)