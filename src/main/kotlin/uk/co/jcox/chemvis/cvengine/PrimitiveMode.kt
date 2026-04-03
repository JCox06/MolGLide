package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL11


enum class PrimitiveMode (val openGlID: Int) {
    TRIANGLES(GL11.GL_TRIANGLES),
    FAN(GL11.GL_TRIANGLE_FAN),
    LINE(GL11.GL_LINES),
    POINTS(GL11.GL_POINTS),
}