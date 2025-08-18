package uk.co.jcox.chemvis.cvengine.newscene

import org.joml.Vector3f

class TextEntity(text: String?, fontID: String?, fontScaleMod: Float?, fontColour: Vector3f?) : Entity() {

    val text: Templated<String> = Templated(text)
    val fontID: Templated<String> = Templated(fontID)
    val fontScaleMod: Templated<Float> = Templated(fontScaleMod)
    val fontColour: Templated<Vector3f> = Templated(fontColour)

    override fun template(): TextEntity {
        val derived = TextEntity(null, null, null, null)
        derived.text.reference = this.text
        derived.fontID.reference = this.fontID
        derived.fontScaleMod.reference = this.fontScaleMod
        derived.fontColour.reference = this.fontColour

        return derived
    }
}