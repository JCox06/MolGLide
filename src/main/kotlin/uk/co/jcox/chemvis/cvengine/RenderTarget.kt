package uk.co.jcox.chemvis.cvengine

data class RenderTarget(
    val frameBuffer: Int,
    val colourAttachmentTexture: Int,
    val depthAttachmentRenderBuffer: Int,
    val width: Int,
    val height: Int,
)
