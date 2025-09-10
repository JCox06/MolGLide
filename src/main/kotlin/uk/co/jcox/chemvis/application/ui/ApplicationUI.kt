package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.type.ImInt
import org.joml.Vector2f
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.v
import uk.co.jcox.chemvis.application.mainstate.MainState
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.tool.Tool
import uk.co.jcox.chemvis.application.moleditorstate.tool.ToolboxContext
import uk.co.jcox.chemvis.cvengine.ICVServices

class ApplicationUI (
    val appManager: MainState,
    val engineManager: ICVServices,
) {


    private val menuBar = MenuBar(appManager, engineManager)
    private val welcomeUI = WelcomeUI()
    private var activeSession: OrganicEditorState? = null

    fun setup() {

        val newWin: () -> Unit = {
            appManager.createNewEditor(welcomeUI.msaaSamples[0])
        }

        welcomeUI.newWindow = newWin
        menuBar.newWindow = newWin

        menuBar.undo = {
            activeSession?.undo()
        }

        menuBar.redo = {
            activeSession?.redo()
        }

        menuBar.getFormula = {
            activeSession?.getFormula()
        }

        welcomeUI.setup()
    }

    fun drawApplicationUI() {
        val dockID = ImGui.dockSpaceOverViewport()
        menuBar.draw()
        welcomeUI.draw(dockID)

        drawEditors(dockID)

        displayProbeInfo()
    }


    fun drawEditors(dockingID: Int) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, ImVec2(0.0f, 0.0f))

        appManager.editors.forEach { id ->
            val renderTarget = engineManager.resourceManager().getRenderTarget(id)
            val renderContext = engineManager.getAppStateRenderingContext(id)


            renderTarget.clearColour = appManager.getCurrentTheme().backgroundColour

            ImGui.setNextWindowDockID(dockingID, ImGuiCond.FirstUseEver)


            ImGui.begin(id)

            val windowPos = ImGui.getWindowPos()
            renderContext?.setRelativeWindowPos(Vector2f(windowPos.x, windowPos.y))

            val state = engineManager.getState(id)
            if (state is OrganicEditorState) {
                activeSession = state
                state.toolbox.atomInsert = menuBar.getAtomInsert()
            }

            if (ImGui.isWindowHovered()) {
                engineManager.resumeAppState(id)
            } else {
                engineManager.pauseAppState(id)
            }


            val width = ImGui.getContentRegionAvailX()
            val height = ImGui.getContentRegionAvailY()

            engineManager.resourceManager().resizeRenderTarget(id, width, height)

            ImGui.image(renderTarget.getSamplableTextureAttachment().toLong(), ImVec2(width, height), ImVec2(0.0f, 1.0f), ImVec2(1.0f, 0.0f))

            renderContext?.recalculate()

            ImGui.end()
        }

        ImGui.popStyleVar()
    }


    private fun displayProbeInfo() {
        if (! menuBar.enableProbe) {
            return
        }

        val selection = activeSession?.selectionManager?.primarySelection
        val container = activeSession?.levelContainer

        if (selection == null || container == null || selection !is SelectionManager.Type.Active) {
            return
        }

        val atom = selection.atom

        val insert = container.chemManager.getAtomInsert(atom.molManagerLink)
        val implicitHydrogen = container.chemManager.getImplicitHydrogens(atom.molManagerLink)
        val bondCount = container.chemManager.getBondCount(atom.parent.molManagerLink, atom.molManagerLink)


        ImGui.setTooltip("Atom Symbol ${insert.symbol} \nImplicit H $implicitHydrogen \nBondCount ${bondCount}")
    }

    class MenuBar(val appManager: MainState, val engineManager: ICVServices) {

        var newWindow: () -> Unit = {}

        var undo: () -> Unit = {}

        var redo: () -> Unit = {}

        var getFormula: () -> String? = {"Waiting..."}

        private val atomSelections = AtomInsert.entries.map { it.symbol }
        private val selected: ImInt = ImInt(0)

        var enableProbe = false
        private set

        fun draw() {

            if (ImGui.beginMainMenuBar()) {

                drawMenuLists()

                ImGui.endMainMenuBar()
            }
        }


        private fun drawMenuLists() {
            if (ImGui.beginMenu("${Icons.FILE_ICON} File")) {
                drawFileMenu()
                ImGui.endMenu()
            }

            if (ImGui.beginMenu("${Icons.EDIT_ICON} Edit")) {
                drawEditMenu()
                ImGui.endMenu()
            }


            renderButtons(atomSelections, selected, true)

            ImGui.text("Formula: ${getFormula()}")

            if (ImGui.checkbox("Enable probe", enableProbe)) {
                enableProbe = !enableProbe
            }
        }


        private fun drawFileMenu() {

            if (ImGui.menuItem("${Icons.NEW_ICON} New Project")) {
                newWindow()
            }

            if (ImGui.menuItem("${Icons.DATABASE_ICON} Load From Disc")) {
                TODO()
            }

            if (ImGui.menuItem("${Icons.CLOSE_ICON} Close Tab")) {
                TODO()
            }

            if (ImGui.menuItem("${Icons.CLOSE_WINDOW_ICON} Close Window")) {
                engineManager.shutdown()
            }

        }

        private fun drawEditMenu() {
            if (ImGui.menuItem("${Icons.UNDO_ICON} Undo")) {
                undo()
            }
            if (ImGui.menuItem("${Icons.REDO_ICON} Redo")) {
                redo()
            }
        }

        fun getAtomInsert() : AtomInsert {
            val symobl = atomSelections[selected.get()]
            return AtomInsert.fromSymbol(symobl)
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
}