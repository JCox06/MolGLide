package uk.co.jcox.chemvis.application.ui.tool

import imgui.ImGui
import imgui.type.ImInt
import uk.co.jcox.chemvis.application.moleditorstate.TemplateRingInsert

class CommonTemplateToolView : ToolViewUI() {


    private val templateSelections = TemplateRingInsert.entries.map { it.template }
    private val activeTemplate = ImInt(0)

    override fun renderMenuButtons() {
        renderButtons(templateSelections, activeTemplate, false)
    }

    fun getTemplateInsert() : TemplateRingInsert {
        val found = TemplateRingInsert.entries.find { it.ordinal == activeTemplate.get() }
        if (found == null) {
            return TemplateRingInsert.BENZENE
        }
        return found
    }
}