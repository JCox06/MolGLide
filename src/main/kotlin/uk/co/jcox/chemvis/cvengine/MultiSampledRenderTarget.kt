package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL32


/**
 * Represents a custom render target with antialiasing abilities
 * Drawing is done to the frame buffers below, and then blitted to the frame buffers in the superclass on postDraw
 */
class MultiSampledRenderTarget(
    frameBuffer: Int,
    colourAttachmentTexture: Int,
    depthAttachmentRenderBuffer: Int,
    width: Float,
    height: Float,

    val sampledFrameBuffer: Int,
    val sampledColourAttachmentTexture: Int,
    val sampledDepthAttachmentRenderBuffer: Int,
    val samples: Int
)
    : RenderTarget(frameBuffer, colourAttachmentTexture, depthAttachmentRenderBuffer, width, height) {
        

    override fun resize(newWidth: Float, newHeight: Float) {
        //Resize the resolved
        super.resize(newWidth, newHeight)
        
        //Now resize the sampled
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, sampledFrameBuffer)
        
        //Texture:
        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, sampledColourAttachmentTexture)
        GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL32.GL_RGBA, newWidth.toInt(), newHeight.toInt(), true)
        
        //Render Buffer
        GL32.glBindRenderbuffer(GL32.GL_RENDERBUFFER, sampledDepthAttachmentRenderBuffer)
        GL32.glRenderbufferStorageMultisample(GL32.GL_RENDERBUFFER, samples, GL32.GL_DEPTH24_STENCIL8, newWidth.toInt(), newHeight.toInt())
        
        
    }

    override fun bindForDrawing() {
        //Bind to the sampled buffer for drawing
        GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, sampledFrameBuffer)
    }


    /**
     * After drawing has taken place into the READ framebuffer for the multi-sampled frame buffer
     * Calling this function will blit everything from the multi-sampled frame buffer into the single sampled frame buffer
     * After calling this, you can safely use the resolved frame buffer and its attachments
     */
    override fun postDraw() {
        GL32.glBindFramebuffer(GL32.GL_READ_FRAMEBUFFER, sampledFrameBuffer)
        GL32.glBindFramebuffer(GL32.GL_DRAW_FRAMEBUFFER, resolvedFrameBuffer)

        GL32.glBlitFramebuffer(0, 0, width.toInt(), height.toInt(), 0, 0, width.toInt(), height.toInt(), GL11.GL_COLOR_BUFFER_BIT, GL11.GL_NEAREST)
    }

    override fun close() {
        super.close()
        GL32.glDeleteFramebuffers(sampledFrameBuffer)
        GL11.glDeleteTextures(sampledColourAttachmentTexture)
        GL32.glDeleteRenderbuffers(sampledDepthAttachmentRenderBuffer)
    }
}