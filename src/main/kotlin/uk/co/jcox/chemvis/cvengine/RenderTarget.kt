package uk.co.jcox.chemvis.cvengine

import org.joml.Vector4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL32


/**
 * Render target represents a custom frame buffer along with several attachments
 * The main render target is not managed by a render target because it was set up by GLFW
 *
 */
open class RenderTarget (
    protected val resolvedFrameBuffer: Int,
    private val resolvedColourAttachmentTexture: Int,
    val resolvedDepthAttachmentRenderBuffer: Int,
    var width: Float,
    var height: Float,
    val clearColour: Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
) : AutoCloseable {

    /**
     * @return an ID which is shader-friendly.
     *
     */
    open fun getSamplableTextureAttachment() : Int {
        return resolvedColourAttachmentTexture
    }

    open fun getSamplableFrameBuffer() : Int {
        return resolvedFrameBuffer
    }

    open fun bindForDrawing() {
        GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, resolvedFrameBuffer)
    }

    /**
     * An optional method that can be overridden and is meant to be run after drawing has taken place
     */
    open fun postDraw() {
        
    }


    open fun resize(newWidth: Float, newHeight: Float) {
        //Resize the main samplable targets:
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, resolvedFrameBuffer)

        //Texture:
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, resolvedColourAttachmentTexture)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, newWidth.toInt(), newHeight.toInt(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0)

        //Depth:
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, resolvedDepthAttachmentRenderBuffer)
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH24_STENCIL8, newWidth.toInt(), newHeight.toInt())

        width = newWidth
        height = newHeight
    }


    override fun close() {
        GL32.glDeleteFramebuffers(resolvedFrameBuffer)
        GL32.glDeleteRenderbuffers(resolvedDepthAttachmentRenderBuffer)
        GL11.glDeleteTextures(resolvedColourAttachmentTexture)
    }
}
