package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Math
import org.joml.Vector3f
import kotlin.math.sin

private val P_RADIUS = 0.5f/ sin(Math.toRadians(36.0f))

enum class TemplateRingInsert(val template: String, val normalAdvance: Float) {

    CYCLOHEXANE("Cyclohexane", 30.0f),
    CYCLOPENANTE("Cyclopentane", 36.0f),
    CYCLOBUTANE("Cyclobutane", 45.0f),
    CYCLOPROPANE("Cyclopropane", 60.0f)
}




