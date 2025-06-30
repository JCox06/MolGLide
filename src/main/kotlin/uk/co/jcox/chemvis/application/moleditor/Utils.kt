package uk.co.jcox.chemvis.application.moleditor

import org.apache.commons.io.IOUtils.buffer
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.tinylog.Logger
import uk.co.jcox.chemvis.cvengine.RenderTarget
import java.awt.Desktop
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.IntBuffer
import javax.imageio.ImageIO
import kotlin.experimental.and

object Utils {

    fun saveBufferToImg(file: File, imgBuff: IntBuffer, width: Int, height: Int) {


        val bufferedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        for (y in 0..<height) {
            for (x in 0..<width) {
                val i = 4 * (x + y * width)
                val r: Int = imgBuff.get(i) and  0xFF
                val g: Int = imgBuff.get(i + 1) and 0xFF
                val b: Int = imgBuff.get(i + 2) and 0xFF
                val a: Int = imgBuff.get(i + 3) and 0xFF
                // Flip y axis
                val flippedY = height - y - 1
                val argb = (a shl 24) or (r shl 16) or (g shl 8) or b
                bufferedImage.setRGB(x, flippedY, argb)
            }
        }
        try {
            ImageIO.write(bufferedImage, "png", file)
        } catch (e: IOException) {
            Logger.error { "Error when saving image ${e.message}"}
        }
        Desktop.getDesktop().open(file)
    }
}