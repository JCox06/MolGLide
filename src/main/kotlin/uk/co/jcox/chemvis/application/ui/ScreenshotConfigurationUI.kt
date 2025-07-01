package uk.co.jcox.chemvis.application.ui

import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.flag.ImGuiColorEditFlags
import imgui.type.ImFloat
import imgui.type.ImInt
import org.joml.Vector3f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditor.Utils
import uk.co.jcox.chemvis.cvengine.RenderTarget
import java.io.File
import javax.swing.JFileChooser

class ScreenshotConfigurationUI (
    private val stateID: String,
    private val state: OrganicEditorState,
    private val renderTarget: RenderTarget,
) {

    val backgroundColour = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    val textColour = floatArrayOf(1.0f, 0.0f, 0.0f)
    val bondColour = floatArrayOf(0.0f, 0.0f, 0.0f)
    val thickness = floatArrayOf(2.5f)

    fun draw() {
        ImGui.begin("Screenshot Config")

        ImGui.text("Configuring for $stateID")

        ImGui.colorPicker4("Background Colour", backgroundColour)
        ImGui.colorPicker3("Text Colour", textColour)
        ImGui.colorPicker3("Bond Colour", bondColour)

        ImGui.sliderFloat("Bond Thickness", thickness, 0.0f, 10.0f)

        state.setThemeStyle(
            colourText = Vector3f(textColour[0], textColour[1], textColour[2]),
            colourLine = Vector3f(bondColour[0], bondColour[1], bondColour[2]),
            width = thickness[0]
        )

        renderTarget.clearColour.x = backgroundColour[0]
        renderTarget.clearColour.y = backgroundColour[1]
        renderTarget.clearColour.z = backgroundColour[2]
        renderTarget.clearColour.w = backgroundColour[3]


        if (ImGui.button("${ApplicationUI.SAVE_IMAGE_ICON} Save Image")) {

            showExplorer()
        }

        ImGui.end()
    }


    private fun showExplorer() {
        //Bind Target
        val width = renderTarget.width.toInt()
        val height = renderTarget.height.toInt()

        val imgBuff = BufferUtils.createIntBuffer(4 * width * height)
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, renderTarget.frameBuffer)
        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_INT, imgBuff)

        val saveImgThread = Runnable {
            val chooser = JFileChooser()
            val result = chooser.showOpenDialog(null)
            if (result == JFileChooser.APPROVE_OPTION) {
                val filePath = File(chooser.selectedFile.toString() + ".png")

                Utils.saveBufferToImg(filePath, imgBuff, width, height)
            }
        }
        val thread = Thread(saveImgThread)
        thread.start()
    }
}