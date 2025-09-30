package uk.co.jcox.chemvis.application.ui.tool

import imgui.ImGui
import imgui.type.ImInt
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem

class AtomBondToolView : ToolViewUI() {

    private val atomInsertStrings = AtomInsert.entries.map { it.symbol }
    val activeInsert: ImInt = ImInt(0)


    var stereoChem = StereoChem.IN_PLANE
    private set

    override fun renderMenuButtons() {
        renderButtons(atomInsertStrings, activeInsert, true)

        ImGui.separator()

        if (ImGui.beginMenu("StereoChem")) {

            if (ImGui.menuItem("Normal", stereoChem == StereoChem.IN_PLANE)) {
                stereoChem = StereoChem.IN_PLANE
            }
            if (ImGui.menuItem("Wedged", stereoChem == StereoChem.FACING_VIEW)) {
                stereoChem = StereoChem.FACING_VIEW
            }
            if (ImGui.menuItem("Dashed", stereoChem == StereoChem.FACING_PAPER)) {
                stereoChem = StereoChem.FACING_PAPER
            }

            ImGui.endMenu()
        }
    }

    fun getInsert() : AtomInsert {
        return AtomInsert.fromSymbol(atomInsertStrings[activeInsert.get()])
    }
}