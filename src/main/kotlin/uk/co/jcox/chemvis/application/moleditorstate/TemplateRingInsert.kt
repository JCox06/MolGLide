package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Math
import org.joml.Vector3f
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val P_RADIUS = 0.5f/ sin(Math.toRadians(36.0f))

enum class TemplateRingInsert(val template: String, val insertSequence: List<Seq>) {

    BENZENE("Benzene",
        listOf(
            Seq(AtomInsert.CARBON, Vector3f(0.0f, 1.0f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(-sqrt(3f) / 2f, 0.5f, 0.0f), BondOrder.DOUBLE),
            Seq(AtomInsert.CARBON, Vector3f(-sqrt(3f) / 2f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(0.0f, -1.0f, 0.0f), BondOrder.DOUBLE),
            Seq(AtomInsert.CARBON, Vector3f(sqrt(3f) / 2f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(sqrt(3f) / 2f, 0.5f, 0.0f), BondOrder.DOUBLE),
        )),



    CYCLOHEXANE("Cyclohexane",
        listOf(
            Seq(AtomInsert.CARBON, Vector3f(0.0f, 1.0f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(-sqrt(3f) / 2f, 0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(-sqrt(3f) / 2f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(0.0f, -1.0f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(sqrt(3f) / 2f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(sqrt(3f) / 2f, 0.5f, 0.0f), BondOrder.SINGLE),
        )),

    CYCLOPENATNE("Cyclopentane",
        listOf(
            Seq(AtomInsert.CARBON, Vector3f(0.0f, P_RADIUS, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(-P_RADIUS * sin(Math.toRadians(72.0f)), P_RADIUS * cos(Math.toRadians(72.0f)), 0.0f),
                BondOrder.SINGLE
            ),
            Seq(AtomInsert.CARBON, Vector3f(-P_RADIUS * sin(Math.toRadians(36.0f)), -P_RADIUS * cos(Math.toRadians(36.0f)), 0.0f),
                BondOrder.SINGLE
            ),
            Seq(AtomInsert.CARBON, Vector3f(P_RADIUS * sin(Math.toRadians(36.0f)), -P_RADIUS * cos(Math.toRadians(36.0f)), 0.0f),
                BondOrder.SINGLE
            ),
            Seq(AtomInsert.CARBON, Vector3f(P_RADIUS * sin(Math.toRadians(72.0f)), P_RADIUS * cos(Math.toRadians(72.0f)), 0.0f),
                BondOrder.SINGLE
            ),

        )),

    CYCLOBUTANE("Cyclobutane",
        listOf(
            Seq(AtomInsert.CARBON, Vector3f(0.5f, 0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(0.5f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(-0.5f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(-0.5f, 0.5f, 0.0f), BondOrder.SINGLE),
            )),

    CYCLOPROPANE("Cyclopropane",
        listOf(
            Seq(AtomInsert.CARBON, Vector3f(-0.5f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(0.5f, -0.5f, 0.0f), BondOrder.SINGLE),
            Seq(AtomInsert.CARBON, Vector3f(0.0f, -0.5f + (sqrt(3f) / 2f), 0.0f), BondOrder.SINGLE),
        )),
    ;
    data class Seq(
        val insert: AtomInsert,
        val pos: Vector3f,
        val startingOrder: BondOrder
    )

}




