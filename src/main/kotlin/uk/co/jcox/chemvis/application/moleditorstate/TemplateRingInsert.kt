package uk.co.jcox.chemvis.application.moleditorstate

import org.joml.Vector3f
import kotlin.math.sqrt

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

    ;


    data class Seq(
        val insert: AtomInsert,
        val pos: Vector3f,
        val startingOrder: BondOrder
    )
}


