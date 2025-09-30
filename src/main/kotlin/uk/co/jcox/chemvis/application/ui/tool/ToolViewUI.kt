package uk.co.jcox.chemvis.application.ui.tool

import imgui.ImGui
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.type.ImInt

open class ToolViewUI {


    open fun renderMenuButtons() {
        //The basic tool UI does not draw anything to the menu bar - And therefore has no state
    }

    protected fun renderButtons(elements: List<String>, activeOption: ImInt, uniformSize: Boolean) {

        for ((index, insert) in elements.withIndex()) {

            val standardButtonSize = ImGui.getFrameHeight()

            if (index == activeOption.get()) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImVec4(0.0f, 100.0f, 0.0f, 255.0f))
                if (uniformSize) {
                    ImGui.button(insert, standardButtonSize * 2, standardButtonSize)
                } else {
                    ImGui.button(insert)
                }
                ImGui.popStyleColor()
            } else {
                if (uniformSize) {
                    if (ImGui.button(insert, standardButtonSize * 1.5f, standardButtonSize)) {
                        activeOption.set(index)
                    }
                } else {
                    if (ImGui.button(insert)) {
                        activeOption.set(index)
                    }
                }

            }
        }
    }
}