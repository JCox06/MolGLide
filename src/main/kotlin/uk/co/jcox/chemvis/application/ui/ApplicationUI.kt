package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import imgui.type.ImBoolean
import org.joda.time.LocalDateTime
import org.joml.Vector2f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.mainstate.MainState
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.RenderTarget
import java.io.File

class ApplicationUI (
    val appManager: MainState,
    val engineManager: ICVServices,
) {



    //todo FIX - The wrong session ID is being reported here which means sometimes the incorrect tab is closed and sometimes the incorrect render target data is read when attempting to take a screenshot

    private val menuBar = MenuBar(appManager, engineManager)
    private val welcomeUI = WelcomeUI(engineManager)
    private var activeSession: OrganicEditorState? = null
    private var activeTarget: RenderTarget? = null
    private var activeSessionID: String? = null
    private val customThemeConfig: CustomThemeConfig = CustomThemeConfig(appManager)

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

        menuBar.getMass = {
            activeSession?.getWeight()
        }

        menuBar.takeScreenshot = {
            takeScreenshot()
        }

        menuBar.closeCurrentTab = {
            closeCurrentEditorWindow()
        }

        menuBar.saveProjectAs = { file ->
            val session = activeSession

            session?.let { appManager.saveProjectToFile(it, file)}
        }

        menuBar.openProject = {file ->
            appManager.openProject(file, welcomeUI.msaaSamples[0])
            if (menuBar.selectedToolset == null) {
                menuBar.selectedToolset = appManager.toolRegistry.getEntries().values.firstOrNull()
            }
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

    private fun closeCurrentEditorWindow() {
        activeSessionID?.let { appManager.closeEditor(it) }
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

        if (menuBar.drawThemeConfig) {
            customThemeConfig.drawUI()
        }

    }

    fun drawEditors(dockingID: Int) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, ImVec2(0.0f, 0.0f))

        val closeList = mutableListOf<String>()

        appManager.editors.forEach { id ->
            val renderTarget = engineManager.resourceManager().getRenderTarget(id)
            val renderContext = engineManager.getAppStateRenderingContext(id)


            renderTarget.clearColour = appManager.themeStyleManager.backgroundColour

            ImGui.setNextWindowDockID(dockingID, ImGuiCond.FirstUseEver)


            //Adding true enables the X button to be present
            val keepOpen = ImBoolean(true)
            ImGui.begin(id, keepOpen)

            if (! keepOpen.get()) {
                closeList.add(id)
            }



            val windowPos = ImGui.getWindowPos()
            renderContext?.setRelativeWindowPos(Vector2f(windowPos.x, windowPos.y))

            val state = engineManager.getState(id)
            if (state is OrganicEditorState) {
                activeSession = state
                activeTarget = engineManager.resourceManager().getRenderTarget(id)
                activeSessionID = id

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

        closeList.forEach {
            appManager.closeEditor(it)
        }
    }

    private fun displayProbeInfo() {
        if (! menuBar.enableProbe) {
            return
        }

        val selection = activeSession?.selectionManager?.primarySelection
        val container = activeSession?.levelContainer

        if (selection == null || container == null || selection !is SelectionManager.Type.ActiveAtom) {
            return
        }

        val atom = selection.atom

        val insert = AtomInsert.fromSymbol(atom.getSymbol())
        val implicitHydrogen = atom.iAtom.implicitHydrogenCount
        val bondCount = atom.iAtom.bondCount


        ImGui.setTooltip("Atom Symbol ${insert.symbol} \nImplicit H $implicitHydrogen \nBondCount ${bondCount}")
    }
}