package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImGuiStyle
import imgui.ImVec2
import imgui.flag.ImGuiCond
import imgui.flag.ImGuiStyleVar
import org.joml.Vector2f
import uk.co.jcox.chemvis.application.GlobalAppState
import uk.co.jcox.chemvis.application.main
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.ApplicationState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.cvengine.RenderTarget

class ApplicationUI (
    private val mainState: GlobalAppState,
    private val services: ICVServices,
) {

    private val menuBar = MainMenuBarUI()
    private val welcomeUI = WelcomeUI()

    private var screenShotUI: ScreenshotConfigurationUI? = null

    private val renderStateIDs = mutableListOf<String>()
    private var activeState: Pair<String, OrganicEditorState>? = null


    fun setup() {
        menuBar.onQuitApplication {
            services.shutdown()
        }

        menuBar.onNewOrganicEditor {
            val renderStateID = mainState.createOrganicEditor(welcomeUI.getSamples())
            renderStateIDs.add(renderStateID)
            restoreColour(services.resourceManager().getRenderTarget(renderStateID))
        }

        menuBar.onCloseCurrentWindow {
            val stateID = activeState?.first

            if (renderStateIDs.contains(stateID) && stateID != null) {
                renderStateIDs.remove(stateID)
                mainState.closeOrganicEditor(stateID)
                activeState = null
            }
        }

        menuBar.onUndo {
            activeState?.second?.undo()
        }

        menuBar.onRedo {
            activeState?.second?.redo()
        }

        menuBar.onScreenshot {
            if (screenShotUI == null) {
                activeState?.let {
                    setupScreenshotUI(it.first, it.second, services.resourceManager().getRenderTarget(it.first))
                }
            } else {
                activeState?.let {
                    destroyScreenshotUI(it.second, services.resourceManager().getRenderTarget(it.first))
                }
            }
        }

        menuBar.onSwitchAtomBondTool {
            activeState?.second?.setAtomBondTool()
        }

        menuBar.onSwitchTemplateTool {
            activeState?.second?.setTemplateTool()
        }
    }

    fun drawApplicationUI() {
        val dockID = ImGui.dockSpaceOverViewport()

        menuBar.draw()
        welcomeUI.draw(dockID)
        drawRenderTargets(dockID)
        activeState?.second?.atomInsert = menuBar.getSelectedInsert()
        activeState?.second?.compoundInsert = menuBar.getSelectedCompoundInsert()


        screenShotUI?.draw()
    }

    private fun drawRenderTargets(dockID: Int) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, ImVec2(0.0f, 0.0f))

        renderStateIDs.forEach { stateID ->
            val renderTarget = services.resourceManager().getRenderTarget(stateID)
            val renderingContext = services.getAppStateRenderingContext(stateID)

            ImGui.setNextWindowDockID(dockID, ImGuiCond.FirstUseEver)
            ImGui.begin(stateID)

            val windowPos = ImGui.getWindowPos()
            renderingContext?.setRelativeWindowPos(Vector2f(windowPos.x, windowPos.y))

            val state = services.getState(stateID)

            if (ImGui.isWindowHovered()) {
                services.resumeAppState(stateID)

                if (state is OrganicEditorState) {

                    if (screenShotUI != null && activeState?.first != stateID) {
                        //Screenshot mode active in other state
                        destroyScreenshotUI(state, renderTarget)
                    }
                    activeState = Pair(stateID, state)

                    menuBar.inspectedFormula = state.moformula
                }
            } else {
                services.pauseAppState(stateID)
            }

            val width = ImGui.getContentRegionAvailX()
            val height = ImGui.getContentRegionAvailY()

            services.resourceManager().resizeRenderTarget(stateID, width, height)

            ImGui.image(renderTarget.getSamplableTextureAttachment().toLong(), ImVec2(width, height), ImVec2(0.0f, 1.0f), ImVec2(1.0f, 0.0f))

            renderingContext?.recalculate()
            ImGui.end()
        }
        ImGui.popStyleVar()
    }


    private fun setupScreenshotUI(stateID: String, state: OrganicEditorState, target: RenderTarget) {
        state.readOnly = true
        state.makeCheckpoint()

        screenShotUI = ScreenshotConfigurationUI(stateID, state, target)
    }

    private fun destroyScreenshotUI(state: OrganicEditorState, renderTarget: RenderTarget) {
        screenShotUI = null
        state.readOnly = false

        restoreColour(renderTarget)

        state.undo()
    }

    private fun restoreColour(renderTarget: RenderTarget) {
        renderTarget.clearColour.x = 0.22f
        renderTarget.clearColour.y = 0.22f
        renderTarget.clearColour.z = 0.226f
        renderTarget.clearColour.w = 1.0f
    }


    companion object {
        const val FILE_ICON = "\uf07c"
        const val EDIT_ICON = "\uf304"
        const val NEW_ICON = "\uf15b"
        const val CLOSE_ICON = "\uf2d3"
        const val UNDO_ICON = "\uf2ea"
        const val REDO_ICON = "\uf2f9"
        const val SAVE_IMAGE_ICON ="\uf0c7"
        const val CLOSE_WINDOW_ICON = "\uf04d"
        const val ATOM_BOND_TOOL_ICON = "\uf0fe"
        const val TEMPLATE_TOOL_ICON ="\uf1b2"
        const val TOOLS_ICON ="\uf6e3"
    }
}