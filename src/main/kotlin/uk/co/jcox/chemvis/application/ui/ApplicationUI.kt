package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import org.joml.Vector2f
import uk.co.jcox.chemvis.application.mainstate.MainState
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.ICVServices

class ApplicationUI (
    val appManager: MainState,
    val engineManager: ICVServices,
) {

    private val menuBar = MenuBar(appManager, engineManager)

    fun setup() {

    }

    fun drawApplicationUI() {
        val dockID = ImGui.dockSpaceOverViewport()
        menuBar.draw()


        drawEditors(dockID)
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


    class MenuBar(val appManager: MainState, val engineManager: ICVServices) {


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
        }


        private fun drawFileMenu() {

            if (ImGui.menuItem("${Icons.NEW_ICON} New Project")) {
                val renderID = appManager.createNewEditor()
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
    }
}