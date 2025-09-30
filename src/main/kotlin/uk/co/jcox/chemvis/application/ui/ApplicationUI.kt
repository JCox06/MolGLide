package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.type.ImInt
import org.joda.time.LocalDateTime
import org.joml.Vector2f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.ToolRegistry
import uk.co.jcox.chemvis.application.mainstate.MainState
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.application.moleditorstate.TemplateRingInsert
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.RenderTarget
import java.io.File

class ApplicationUI (
    val appManager: MainState,
    val engineManager: ICVServices,
) {


    private val menuBar = MenuBar(appManager, engineManager)
    private val welcomeUI = WelcomeUI()
    private var activeSession: OrganicEditorState? = null
    private var activeTarget: RenderTarget? = null

    fun setup() {

        val newWin: () -> Unit = {

            if (menuBar.selectedToolset == null) {
                menuBar.selectedToolset = appManager.toolRegistry.getEntries().values.firstOrNull()
            }

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

        menuBar.takeScreenshot = {
            takeScreenshot()
        }


        welcomeUI.setup()
    }


    private fun takeScreenshot() {
        //Capture image:
        val target = activeTarget
        val session = activeSession
        if (target != null && session != null) {
            showScreenshotExplorer(target, session)
        }
    }

    private fun showScreenshotExplorer(renderTarget: RenderTarget, session: OrganicEditorState) {
        val width = renderTarget.width.toInt()
        val height = renderTarget.height.toInt()

        val imgBuff = BufferUtils.createIntBuffer(4 * width * height)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, renderTarget.getSamplableFrameBuffer())
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_INT, imgBuff)

        val saveImgThread = Runnable {
            val home = System.getProperty("user.home")
            val dateTime = LocalDateTime.now()
            val molphoto = File(home, "Pictures/MolGLide")
            if (!molphoto.exists()) {
                molphoto.mkdir()
            }

            Utils.saveBufferToImg(File(molphoto.toString(), dateTime.toString()), imgBuff, width, height)
        }

        val thread = Thread(saveImgThread)
        thread.start()
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


            renderTarget.clearColour = appManager.themeStyleManager.activeTheme.backgroundColour

            ImGui.setNextWindowDockID(dockingID, ImGuiCond.FirstUseEver)


            ImGui.begin(id)

            val windowPos = ImGui.getWindowPos()
            renderContext?.setRelativeWindowPos(Vector2f(windowPos.x, windowPos.y))

            val state = engineManager.getState(id)
            if (state is OrganicEditorState) {
                activeSession = state
                activeTarget = engineManager.resourceManager().getRenderTarget(id)

                if (state.currentTool == null) {
                    state.currentTool = menuBar.selectedToolset?.toolCreator(state)
                }
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

        var selectedToolset: ToolRegistry.ToolSetup? = null

        private var selectedTheme = 0

        var newWindow: () -> Unit = {}

        var undo: () -> Unit = {}

        var redo: () -> Unit = {}

        var takeScreenshot: () -> Unit = {}

        var getFormula: () -> String? = {"Waiting..."}


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

            if (ImGui.beginMenu("${Icons.PAINT_BRUSH} Themes")) {
                drawThemeMenu()
                ImGui.endMenu()
            }

            if (ImGui.button("Take Screenshot")) {
                takeScreenshot()
            }

            if (ImGui.beginMenu("${Icons.TOOLS_ICON} Tool Selection")) {
                drawToolSelectionMenu()
                ImGui.endMenu()
            }

            selectedToolset?.toolViewUI?.renderMenuButtons()

            ImGui.text("Formula: ${getFormula()}")

            if (ImGui.checkbox("Enable probe", enableProbe)) {
                enableProbe = !enableProbe
            }
        }


        private fun drawToolSelectionMenu() {
            //Get the available tools from the app manager
            appManager.toolRegistry.getEntries().forEach { toolSet ->
                if (ImGui.menuItem(toolSet.value.name, toolSet.value == selectedToolset)) {
                    selectedToolset = toolSet.value

                    appManager.editors.forEach { id ->
                        val state = engineManager.getState(id)
                        if (state is OrganicEditorState) {
                            state.currentTool = toolSet.value.toolCreator(state)
                        }
                    }
                }
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

        private fun drawThemeMenu() {
            if (ImGui.menuItem("MolGLide Edit Theme", selectedTheme == 0)) {
                selectedTheme = 0
                appManager.themeStyleManager.applyMolGLideEdit()
            }
            if (ImGui.menuItem("CPK Theme", selectedTheme == 1)) {
                selectedTheme = 1
                appManager.themeStyleManager.applyCPKTheme()
            }
            if (ImGui.menuItem("MolGLide Screenshot theme", selectedTheme == 2)) {
                selectedTheme = 2
                appManager.themeStyleManager.applyScreenshotMolGLide()
            }
            if (ImGui.menuItem("Screenshot White", selectedTheme == 3)) {
                selectedTheme = 3
                appManager.themeStyleManager.applyScreenshotWhite()
            }
        }

    }
}