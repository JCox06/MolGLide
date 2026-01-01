package uk.co.jcox.chemvis.application.ui.tool

import imgui.ImGui
import imgui.type.ImInt
import org.apache.jena.sparql.syntax.Template
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.RingInsert

class CommonTemplateToolView : ToolViewUI() {

    private val atomTemplateStrings = RingInsert.entries.map { it.friendlyName }
    private val activeInsert: ImInt = ImInt(0)

    fun getTemplate() : RingInsert {
        return RingInsert.entries[activeInsert.get()]
    }

    override fun renderMenuButtons() {
        renderButtons(atomTemplateStrings, activeInsert, false)
    }

}
