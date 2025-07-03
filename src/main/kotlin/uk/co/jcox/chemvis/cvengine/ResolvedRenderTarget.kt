package uk.co.jcox.chemvis.cvengine

class ResolvedRenderTarget(
    frameBuffer: Int,
    colourAttachmentTexture: Int,
    depthAttachmentRenderBuffer: Int,
    width: Float,
    height: Float,

    val resolvedBuffer: Int,
    val resolvedColour: Int,
    val samples: Int
)
    : RenderTarget(frameBuffer, colourAttachmentTexture, depthAttachmentRenderBuffer, width, height) {


    override fun getSamplableTextureAttachment(): Int {
        return resolvedColour
    }

    override fun getSamplableFrameBuffer(): Int {
        return resolvedBuffer
    }
}