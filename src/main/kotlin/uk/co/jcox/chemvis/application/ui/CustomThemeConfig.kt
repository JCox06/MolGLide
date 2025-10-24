package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import uk.co.jcox.chemvis.application.mainstate.MainState

class CustomThemeConfig (
    private val appManager: MainState
) {

    fun drawUI() {
        ImGui.begin("Theme Config")

        val lineThickness = floatArrayOf(appManager.themeStyleManager.lineThickness)
        ImGui.sliderFloat("Line Width", lineThickness, 0.0f, 10.0f)
        appManager.themeStyleManager.lineThickness = lineThickness.get(0)

        ImGui.end()
    }
}