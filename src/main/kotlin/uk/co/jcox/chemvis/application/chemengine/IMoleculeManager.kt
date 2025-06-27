package uk.co.jcox.chemvis.application.chemengine

import java.util.*


interface IMoleculeManager {
    fun createMolecule(): UUID

    fun addAtom(molecule: UUID, element: String): UUID

    fun formBond(moleculeID: UUID, atom1: UUID, atom2: UUID, bondOrder: Int): UUID

    fun getMolecularFormula(moleculeID: UUID): String

    fun getBonds(moleculeID: UUID, atom: UUID): Int

    fun isOfElement(moleculeID: UUID, Atom: UUID, element: String): Boolean


    fun clone(): IMoleculeManager

    fun recalculate(molecule: UUID)

    fun getImplicitHydrogens(molecule: UUID, atom: UUID): Int

    fun updateBondOrder(molecule: UUID, bond: UUID, newBondOrder: Int)

    fun getJoiningBond(molecule: UUID, atomA: UUID, atomB: UUID): UUID?

    fun replace(atom: UUID, element: String)
}
