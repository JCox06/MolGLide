package uk.co.jcox.chemvis.application.ui.tool

import imgui.ImGui
import imgui.type.ImInt
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert

class AtomBondToolView : ToolViewUI() {

    private val atomInsertStrings = AtomInsert.entries.map { it.symbol }
    val activeInsert: ImInt = ImInt(0)


    var stereoChem = IBond.Display.Solid
    private set

    override fun renderMenuButtons() {
        renderButtons(atomInsertStrings, activeInsert, true)

        ImGui.separator()

        if (ImGui.beginMenu("StereoChem")) {

            if (ImGui.menuItem("Normal", stereoChem == IBond.Display.Solid)) {
                stereoChem = IBond.Display.Solid
            }
            if (ImGui.menuItem("Wedged", stereoChem ==IBond.Display.WedgeEnd)) {
                stereoChem = IBond.Display.WedgeEnd
            }
            if (ImGui.menuItem("Dashed", stereoChem == IBond.Display.WedgedHashEnd)) {
                stereoChem = IBond.Display.WedgedHashEnd
            }

            ImGui.endMenu()
        }
    }

    fun getInsert() : AtomInsert {
        return AtomInsert.fromSymbol(atomInsertStrings[activeInsert.get()])
    }
}