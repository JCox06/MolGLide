package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImGuiViewport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.lwjgl.system.Platform
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.ToolRegistry
import uk.co.jcox.chemvis.application.mainstate.MainState
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.ICVServices
import uk.co.jcox.chemvis.application.ui.ImGuiUtils
import uk.co.jcox.chemvis.cvengine.IFileServices
import uk.co.jcox.chemvis.cvengine.NativeFIleService
import java.awt.Desktop
import java.io.File
import java.net.URI

class MenuBar(val appManager: MainState, val engineManager: ICVServices) {

    var selectedToolset: ToolRegistry.ToolSetup? = null

    private var selectedTheme = 0

    var newWindow: () -> Unit = {}

    var undo: () -> Unit = {}

    var redo: () -> Unit = {}

    var takeScreenshot: () -> Unit = {}

    var getFormula: () -> String? = {"Waiting..."}

    var getMass: () -> Double? = {0.0}

    var closeCurrentTab: () -> Unit = {}

    var saveProjectAs: (file: File) -> Unit = {}

    var openProject: (file: File) -> Unit = {}


    var drawThemeConfig = false
        private set


    var enableProbe = false
        private set

    private var showMolAbout = false
    private var showImGuiAbout = false
    private var showMetrics = false

    fun draw() {

        if (ImGui.beginMainMenuBar()) {

            drawMenuLists()

            ImGui.endMainMenuBar()
        }

        ImGuiUtils.populateStatus {
            val formula = getFormula()
            ImGui.text("Formula $formula")

            ImGui.text(" | ")

            val weight = "%.4f".format(getMass())
            ImGui.text("Weight $weight g/mol")

            ImGui.text(" | ")

            ImGui.spacing()

            if (appManager.bulkOperationMode) {
                ImGui.text("File Operations are in Progress ...")
            }
        }

        if (showImGuiAbout) {
            ImGui.showAboutWindow()
        }
        if (showMolAbout) {
            drawMolAbout()
        }
        if (showMetrics) {
            ImGui.showMetricsWindow()
        }
    }

    private fun drawMolAbout() {
        ImGui.begin("About")

        ImGui.text("MolGLide ${MolGLide.VERSION}")

        ImGui.separator()

        ImGui.textWrapped("Libraries used:")

        ImGui.beginTable("About", 2)

        ImGui.tableNextColumn()
        ImGui.bulletText("LWJGL")
        ImGui.tableNextColumn()
        ImGui.bulletText("GLFW")
        ImGui.tableNextColumn()
        ImGui.bulletText("OpenGL")
        ImGui.tableNextColumn()
        ImGui.bulletText("STB Image")
        ImGui.tableNextColumn()
        ImGui.bulletText("JOML")
        ImGui.tableNextColumn()
        ImGui.bulletText("Spair ImGui")
        ImGui.tableNextColumn()
        ImGui.bulletText("NFD Extended")
        ImGui.tableNextColumn()
        ImGui.bulletText("CDK")
        ImGui.tableNextColumn()
        ImGui.bulletText("TinyLog")
        ImGui.tableNextColumn()


        ImGui.endTable()

        if (ImGui.button("Close")) {
            showMolAbout = false
        }

        ImGui.end()
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

        if (ImGui.beginMenu("${Icons.ABOUT_ICON} Help")) {
            drawAboutMenu()
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

        if (ImGui.menuItem("${Icons.SAVE_IMAGE_ICON} Save Document")) {
            val fileOperations: IFileServices = engineManager.getFileServices()
            val file = fileOperations.askUserSaveFile("mgf", "MolGLide Graph File")
            if (file is IFileServices.FileOperation.FileRetrieved) {
                saveProjectAs(file.file)
            }
        }

        if (ImGui.menuItem("${Icons.DATABASE_ICON} Load From Disc")) {
            val fileOperations: IFileServices = engineManager.getFileServices()
            val file= fileOperations.askUserChooseFile("mgf", "MolGLide Graph File")
            if (file is IFileServices.FileOperation.FileRetrieved) {
                openProject(file.file)
            }
        }

        if (ImGui.menuItem("${Icons.CLOSE_ICON} Close Tab")) {
            closeCurrentTab()
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


    private fun drawAboutMenu() {
        if (ImGui.menuItem("${Icons.GITHUB_ICON} Visit Repository")) {
            Desktop.getDesktop().browse(URI(MolGLide.WEBSITE))
        }

        if (ImGui.menuItem("About MolGLide")) {
            showMolAbout = !showMolAbout
        }
        if (ImGui.menuItem("About ImGui")) {
            showImGuiAbout = !showImGuiAbout
        }

        if (ImGui.menuItem("Show Metrics")) {
            showMetrics = !showMetrics
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
        if (ImGui.menuItem("Theme Manager")) {
            drawThemeConfig = true
        }
    }

}