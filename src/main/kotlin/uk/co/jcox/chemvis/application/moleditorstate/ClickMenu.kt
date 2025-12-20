package uk.co.jcox.chemvis.application.moleditorstate

import imgui.ImGui
import imgui.type.ImString

class ClickMenu {

    var showBondMenu = false
    var showAtomMenu = false


    var showCustomLabelInput = false

    fun renderMenu() {
        displayBondMenu()
        displayAtomMenu()

        if (showCustomLabelInput) {
            displayCustomInput()
        }
    }


    private fun displayCustomInput() {
        ImGui.begin("Custom Input")

        ImGui.text("Enter a custom group")

        ImGui.inputText("##", ImString())

        ImGui.sameLine()

        if (ImGui.button("Cancel")) {
            showCustomLabelInput = false
        }

        ImGui.sameLine()

        if (ImGui.button("Accept")) {
            showCustomLabelInput = false
            //todo actually add the group to the atom (The atom being the first letter in the input)
        }

        ImGui.end()
    }

    private fun displayBondMenu() {
        if (showBondMenu) {

            ImGui.openPopup("BondMenu")
            showBondMenu = false
        }

        if (ImGui.beginPopup("BondMenu")) {

            if (ImGui.menuItem("Flip Bond")) {

            }

            ImGui.separator()

            if (ImGui.beginMenu("Single")) {
                if (ImGui.menuItem("Plain", true)) {

                }
                if (ImGui.menuItem("Wedged", false)) {

                }
                if (ImGui.menuItem("Dashed", false)) {

                }

                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Double")) {

                if (ImGui.menuItem("Plain", false)) {

                }

                if (ImGui.menuItem("Aromatic", false)) {

                }

                ImGui.separator()

                if (ImGui.menuItem("Centre/Carbonyl Bond", false)) {

                }
                ImGui.endMenu()
            }

            if (ImGui.menuItem("Triple Bond", false)) {

            }

            ImGui.separator()

            if (ImGui.menuItem("Delete")) {

            }

            ImGui.endPopup()
        }
    }


    private fun displayAtomMenu() {
        if (showAtomMenu) {
            ImGui.openPopup("AtomMenu")
            showAtomMenu = false
        }

        if (ImGui.beginPopup("AtomMenu")) {

            if (ImGui.menuItem("Edit Label")) {
                showCustomLabelInput = true
            }

            if (ImGui.menuItem("Edit Properties")) {

            }

            ImGui.separator()

            if (ImGui.menuItem("Delete")) {

            }

            ImGui.endMenu()
        }

    }

    fun closeWindows() {
        showAtomMenu = false
        showBondMenu = false
        showCustomLabelInput = false
    }
}