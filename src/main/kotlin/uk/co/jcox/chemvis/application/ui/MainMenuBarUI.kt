package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.type.ImInt
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.CompoundInsert
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

    private var switchAtomBondTool: (() -> Unit)? = null
    private var switchTemplateTool: (() -> Unit)? = null


    private var activeTool = 0

    private val activeInsert: ImInt = ImInt(0)
    private val activeTemplate: ImInt = ImInt(0)

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

    fun onSwitchAtomBondTool(func: () -> Unit) {
        this.switchAtomBondTool =  func
    }

    fun onSwitchTemplateTool(func: () -> Unit) {
        this.switchTemplateTool =  func
    }


    fun getSelectedInsert() : AtomInsert {
        return AtomInsert.entries[activeInsert.get()]
    }


    fun getSelectedCompoundInsert() : CompoundInsert {
        return CompoundInsert.entries[activeTemplate.get()]
    }


    fun draw() {

        if (ImGui.beginMainMenuBar()) {

            drawMenuLists()

            ImGui.separator()

            if (ImGui.button("Toggle Screenshot Mode")) {
                screenshot?.invoke()
            }

            if (ImGui.beginMenu("${ApplicationUI.TOOLS_ICON} Tools")) {
                drawTools()
                ImGui.endMenu()
            }

            drawToolOptions()

            ImGui.separator()

            ImGui.text("Formula: $inspectedFormula")

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


    private fun drawTools() {
        if (ImGui.menuItem("${ApplicationUI.ATOM_BOND_TOOL_ICON} Atom Bond Tool", activeTool == 0)) {
            activeTool = 0
            switchAtomBondTool?.invoke()
        }
        if (ImGui.menuItem("${ApplicationUI.TEMPLATE_TOOL_ICON} Template Tool", activeTool == 1)) {
            activeTool = 1
            switchTemplateTool?.invoke()
        }
    }

    private fun drawToolOptions() {
        //Tool = 0 is the AtomBondTool
        if (activeTool == 0) {
            val inserts = AtomInsert.entries
            renderButtons(inserts.map { it.symbol }, activeInsert, true)
        }

        //Tool 1 is the Template Tool
        if (activeTool == 1) {
            renderButtons(CompoundInsert.entries.map { it.ringName }, activeTemplate, false)
        }
    }


    private fun renderButtons(elements: List<String>, activeOption: ImInt, uniformSize: Boolean) {

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