package uk.co.jcox.chemvis.application.moleditor

import imgui.ImGui
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.cvengine.ICVServices
import java.awt.Desktop
import java.net.URI

class ApplicationUI {

    private val elements = listOf("C", "H", "O", "N", "P", "F", "Cl", "Br", "I")
    private var activeElement = 0


    private var showMetrics = false
    private var showStyles = false
    private var showWelcome = true

    fun mainMenu(services: ICVServices, workState: WorkState, tool: Tool, formula: String) {
        if (ImGui.beginMainMenuBar()) {

            renderFile(services)
            renderEdit(workState, tool)
            renderDebug()

            ImGui.separator()

           //Add buttons
            renderButtons()

            ImGui.separator()

            ImGui.text(formula)

            ImGui.separator()

            ImGui.textColored(1.0f, 0.0f, 0.0f, 1.0f, MolGLide.VERSION)

            ImGui.endMainMenuBar()
        }
    }

    private fun renderFile(services: ICVServices) {

        if (ImGui.beginMenu("File")) {

            if (ImGui.menuItem("Save")) {

            }

            if (ImGui.menuItem("Save as")) {

            }

            if (ImGui.menuItem("Open")) {

            }

            ImGui.separator()

            if (ImGui.menuItem("Show Welcome")) {
                showWelcome = !showWelcome

            }

            if (ImGui.menuItem("Visit Website")) {
                Desktop.getDesktop().browse(URI("https://github.com/JCox06/MolGLide/tree/master"))

            }

            ImGui.separator()

            if (ImGui.menuItem("Quit")) {
                services.shutdown()
            }

            ImGui.endMenu()
        }
    }

    private fun renderEdit(workState: WorkState, tool: Tool) {
        if (ImGui.beginMenu("Edit")) {

            if (ImGui.menuItem("Undo (CTRL + Z)")) {
                workState.undo()
                tool.refreshWorkingState()
            }

            if (ImGui.menuItem("Redo (CTRL + Y)")) {
                workState.redo()
                tool.refreshWorkingState()
            }

            ImGui.endMenu()
        }
    }

    private fun renderDebug() {
        if (ImGui.beginMenu("Debug")) {
            if (ImGui.menuItem("Show metrics", showMetrics)) {
                showMetrics = !showMetrics
            }

            if (ImGui.menuItem("Show ImGui style editor", showStyles)) {
                showStyles = !showStyles
            }

            ImGui.endMenu()
        }
    }


    fun renderWidgets() {

        if (showMetrics) {
            ImGui.showMetricsWindow()
        }

        if (showStyles) {
            ImGui.showStyleEditor()
        }

        if (showWelcome) {
            renderWelcome()
        }
    }

    private fun renderWelcome() {
        ImGui.openPopup("Welcome to MolGLide")
        if (ImGui.beginPopupModal("Welcome to MolGLide", null, ImGuiWindowFlags.NoBackground or ImGuiWindowFlags.NoDecoration or ImGuiWindowFlags.NoMove)) {


            ImGui.text("Thank you for using MolGLide!")
            ImGui.text("This is a very simple molecule editor that allows you to create simple organic molecules.")

            ImGui.separator()

            ImGui.bulletText("Use the AtomBondTool to create and insert atoms")
            ImGui.bulletText("Go back using standard undo/redo")

            if (ImGui.button("Close Welcome Menu")) {
                showWelcome = false
                ImGui.closeCurrentPopup()
            }
            ImGui.endPopup()
        }
    }


    private fun renderButtons() {
        for ((index, string) in elements.withIndex()) {
            if (index == activeElement) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImVec4(0.0f, 100.0f, 0.0f, 255.0f))
                ImGui.button(string, 40.0f, 25.0f)
                ImGui.popStyleColor()
            } else {
                if (ImGui.button(string, 30.0f, 25.0f)) {
                    activeElement = index
                }
            }
        }
    }


    fun getActiveElement() : AtomInsert{
        return AtomInsert.fromSymbol(elements[activeElement])
    }
}