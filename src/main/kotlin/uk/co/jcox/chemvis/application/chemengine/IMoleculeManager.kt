package uk.co.jcox.chemvis.application.chemengine

import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import java.util.*


interface IMoleculeManager {

    /**
     * Create an empty molecule that can be referenced by a UUID
     * @return the uuid of the molecule
     */
    fun createMolecule(): UUID


    fun deleteMolecule(uuid: UUID)

    fun deleteAtom(mol: UUID, atom: UUID)

    fun deleteBond(mol: UUID, bond: UUID)

    /**
     * Add an atom to an already existing molecule
     * @param molecule the uuid to reference an already existing molecule
     * @param insert a symbol string of the element to add
     * @return the uuid of the atom
     */
    fun addAtom(molecule: UUID, insert: AtomInsert): UUID


    /**
     * Form a bond between two atoms in the same molecule
     * @param moleculeID the common molecule ID of the two other atoms
     * @param atom1 form the bond between this atom and
     * @param atom2
     */
    fun formBond(moleculeID: UUID, atom1: UUID, atom2: UUID, bondOrder: BondOrder): UUID


    fun getMolecularFormula(moleculeID: UUID): String

    fun getBondCount(moleculeID: UUID, atom: UUID): Int

    /**
     * Perform calculations on a given molecule
     * This involves identifying the implicit hydrogens
     * @param molecule the molecule ID to run the calculation on
     */
    fun recalculate(molecule: UUID)

    fun getImplicitHydrogens(atom: UUID): Int

    fun updateBondOrder(molecule: UUID, bond: UUID, newBondOrder: BondOrder)

    fun getJoiningBond(molecule: UUID, atomA: UUID, atomB: UUID): UUID?

    fun replace(atom: UUID, insert: AtomInsert)

    fun getAtomInsert(atom: UUID) : AtomInsert

    fun getBondOrder(bond: UUID) : BondOrder
}
