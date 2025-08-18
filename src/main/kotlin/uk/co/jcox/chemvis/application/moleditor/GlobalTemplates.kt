package uk.co.jcox.chemvis.application.moleditor

import org.joml.Vector3f
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.cvengine.CVEngine
import uk.co.jcox.chemvis.cvengine.newscene.TextEntity

object GlobalTemplates {

    private val ATOM_TEXT_LABEL = TextEntity(null, MolGLide.FONT, MolGLide.GLOBAL_SCALE, null)

    val OXYGEN_LABEL = ATOM_TEXT_LABEL.template()
    val NITROGEN_LABEL = ATOM_TEXT_LABEL.template()
    val DEFAULT = ATOM_TEXT_LABEL.template()

    init {
        applyDefaultTheme()
    }

    fun applyDefaultTheme() {
        OXYGEN_LABEL.fontColour.set(Vector3f(1.0f, 0.0f, 0.0f))
        NITROGEN_LABEL.fontColour.set(Vector3f(0.0f, 0.0f, 1.0f))
        DEFAULT.fontColour.set(Vector3f(1.0f, 1.0f, 1.0f))
    }
}