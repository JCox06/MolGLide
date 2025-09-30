package uk.co.jcox.chemvis.application.ui.tool

import imgui.ImGui

class CommonTemplateToolView : ToolViewUI() {

    override fun renderMenuButtons() {
        ImGui.text("I am the common template view")
    }
}