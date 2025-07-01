package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec4
import imgui.flag.ImGuiCol
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.ui.ApplicationUI.Companion.CLOSE_ICON
import uk.co.jcox.chemvis.application.ui.ApplicationUI.Companion.CLOSE_WINDOW_ICON
import uk.co.jcox.chemvis.application.ui.ApplicationUI.Companion.EDIT_ICON
import uk.co.jcox.chemvis.application.ui.ApplicationUI.Companion.FILE_ICON
import uk.co.jcox.chemvis.application.ui.ApplicationUI.Companion.NEW_ICON
import uk.co.jcox.chemvis.application.ui.ApplicationUI.Companion.REDO_ICON
import uk.co.jcox.chemvis.application.ui.ApplicationUI.Companion.UNDO_ICON

class MainMenuBarUI {

    private var newOrganicEditor: (() -> Unit)? = null
    private var closeCurrentWindow: (() -> Unit)? = null
    private var quitApplication: (() -> Unit)? = null
    private var undo: (() -> Unit)? = null
    private var redo: (() -> Unit)? = null
    private var screenshot: (() -> Unit)? = null

    private var activeInsert = 0

    var inspectedFormula = "Waiting For Data"

    fun onNewOrganicEditor(func: () -> Unit) {
        this.newOrganicEditor = func
    }

    fun onCloseCurrentWindow(func: () -> Unit) {
        this.closeCurrentWindow = func
    }

    fun onQuitApplication(func: () -> Unit) {
        this.quitApplication =  func
    }

    fun onUndo(func: () -> Unit) {
        this.undo =  func
    }

    fun onRedo(func: () -> Unit) {
        this.redo =  func
    }

    fun onScreenshot(func: () -> Unit) {
        this.screenshot =  func
    }


    fun getSelectedInsert() : AtomInsert {
        return AtomInsert.entries[activeInsert]
    }


    fun draw() {

        if (ImGui.beginMainMenuBar()) {

            drawMenuLists()

            ImGui.separator()

            if (ImGui.button("Toggle Screenshot Mode")) {
                screenshot?.invoke()
            }

            ImGui.separator()

            drawAvailableInserts()

            ImGui.separator()

            ImGui.text("Formula $inspectedFormula")

            ImGui.endMainMenuBar()
        }
    }


    private fun drawMenuLists() {
        if (ImGui.beginMenu("$FILE_ICON File")) {
            drawFileMenu()
            ImGui.endMenu()
        }

        if (ImGui.beginMenu("$EDIT_ICON Edit")) {
            drawEditMenu()
            ImGui.endMenu()
        }

    }

    private fun drawFileMenu() {
        if (ImGui.menuItem("$NEW_ICON New")) {
            newOrganicEditor?.invoke()
        }

        if (ImGui.menuItem("$CLOSE_WINDOW_ICON Close Window")) {
            closeCurrentWindow?.invoke()
        }

        if (ImGui.menuItem("$CLOSE_ICON Quit MolGLide")) {
            quitApplication?.invoke()
        }
    }

    private fun drawEditMenu() {
        if (ImGui.menuItem("$UNDO_ICON Undo")) {
            undo?.invoke()
        }

        if (ImGui.menuItem("$REDO_ICON Redo")) {
            redo?.invoke()
        }
    }


    private fun drawAvailableInserts() {
        val inserts = AtomInsert.entries
        renderButtons(inserts)
    }


    private fun renderButtons(elements: List<AtomInsert>) {

        for ((index, insert) in elements.withIndex()) {
            if (index == activeInsert) {
                ImGui.pushStyleColor(ImGuiCol.Button, ImVec4(0.0f, 100.0f, 0.0f, 255.0f))
                ImGui.button(insert.symbol, 40.0f, 25.0f)
                ImGui.popStyleColor()
            } else {
                if (ImGui.button(insert.symbol, 30.0f, 25.0f)) {
                    activeInsert = index
                }
            }
        }
    }

}