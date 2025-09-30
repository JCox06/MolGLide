package uk.co.jcox.chemvis.application.ui.tool

import imgui.ImGui

class AtomBondToolView : ToolViewUI() {


    override fun renderMenuButtons() {
        ImGui.text("I am the Atom Bond Tool View")
    }
}