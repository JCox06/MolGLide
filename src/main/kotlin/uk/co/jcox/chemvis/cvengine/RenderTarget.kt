package uk.co.jcox.chemvis.cvengine

import org.joml.Vector3f
import org.joml.Vector4f


open class RenderTarget (
    val frameBuffer: Int,
    val colourAttachmentTexture: Int,
    val depthAttachmentRenderBuffer: Int,
    var width: Float,
    var height: Float,
    val clearColour: Vector4f = Vector4f(0.0f, 0.0f, 0.0f, 1.0f)
) {

    open fun getSamplableTextureAttachment() : Int {
        return colourAttachmentTexture
    }
}
