package uk.co.jcox.chemvis.cvengine


data class RenderTarget(
    val frameBuffer: Int,
    val colourAttachmentTexture: Int,
    val depthAttachmentRenderBuffer: Int,
    var width: Float,
    var height: Float,
)
