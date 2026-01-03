package uk.co.jcox.chemvis.application.graph

import org.apache.commons.logging.Log
import org.joml.Vector2f
import org.joml.Vector3f
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.CDK
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.silent.MolecularFormula
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
import org.tinylog.Logger
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import java.util.UUID

class ChemMolecule (
    val iContainer: IAtomContainer = AtomContainer()
) {

    val positionOffset = Vector3f(0.0f, 0.0f, OrganicEditorState.ATOM_PLANE)
    val atoms = mutableListOf<ChemAtom>()
    val bonds = mutableListOf<ChemBond>()

    fun addAtom(insert: AtomInsert, innerX: Float, innerY: Float): ChemAtom {
        val chemAtom: ChemAtom = ChemAtom(insert)
        chemAtom.setInnerPosition(innerX, innerY)
        chemAtom.parent = this
        atoms.add(chemAtom)
        iContainer.addAtom(chemAtom.iAtom)

        calculateAtomTypes()
        return chemAtom
    }

    fun addAtom(iAtom: IAtom) : ChemAtom {
        val chemAtom = ChemAtom(AtomInsert.CARBON, iAtom)
        atoms.add(chemAtom)
        chemAtom.parent = this
        iContainer.addAtom(iAtom)
        calculateAtomTypes()
        return chemAtom
    }

    fun addBond(atomA: ChemAtom, atomB: ChemAtom, iBond: IBond) : ChemBond {
        val chemBond = ChemBond(atomA, atomB, iBond)
        bonds.add(chemBond)
        iContainer.addBond(iBond)
        calculateAtomTypes()

        return chemBond
    }


    fun setAromaticity(bond: ChemBond, aromatic: Boolean) {
        bond.iBond.setIsAromatic(aromatic)
        calculateAtomTypes()
    }

    fun removeAtom(chemAtom: ChemAtom) {
        atoms.remove(chemAtom)
        iContainer.removeAtom(chemAtom.iAtom)
        calculateAtomTypes()
    }

    fun removeBond(chemBond: ChemBond) {
        bonds.remove(chemBond)
        iContainer.removeBond(chemBond.iBond)
        calculateAtomTypes()
    }

    fun formBasicConnection(atomA: ChemAtom, atomB: ChemAtom) : ChemBond {
        val chemBond: ChemBond = ChemBond(atomA, atomB)
        bonds.add(chemBond)
        iContainer.addBond(chemBond.iBond)

        calculateAtomTypes()
        return chemBond
    }


    fun updateSymbol(atom: ChemAtom, newSymbol: String) {
        atom.iAtom.symbol = newSymbol
        calculateAtomTypes()
    }

    fun updateBondOrder(bond: ChemBond, newOrder: IBond.Order) {
        bond.iBond.order = newOrder
        calculateAtomTypes()
    }

    fun updateStereoDisplay(bond: ChemBond, newDisplay: IBond.Display) {
        bond.iBond.display = newDisplay
        calculateAtomTypes()
    }

    fun calculateAtomTypes() {
        try {
            val atomMatcher = CDKAtomTypeMatcher.getInstance(iContainer.builder)
            for (atom in atoms) {
                with(atom.iAtom) {
                    atomTypeName = null
                    valency = null
                    hybridization = null
                    formalNeighbourCount = null
                    bondOrderSum = null
                    implicitHydrogenCount = null
                }
                val atomType = atomMatcher.findMatchingAtomType(iContainer, atom.iAtom)
                AtomTypeManipulator.configure(atom.iAtom, atomType)
            }
            val hAdder = CDKHydrogenAdder.getInstance(iContainer.builder)
            hAdder.addImplicitHydrogens(iContainer)

            for (atom in atoms) {
                if (!AtomInsert.fromSymbol(atom.getSymbol()).hydrogenable) {
                    atom.iAtom.implicitHydrogenCount = 0
                }
            }
        } catch (e: CDKException) {
            Logger.error ("Error when calculating molecule type {}", e)
        }

    }

    fun getFormulaString() : String {
        val formula = MolecularFormulaManipulator.getMolecularFormula(iContainer)
        return MolecularFormulaManipulator.getString(formula)
    }

    fun getMolecularWeight() : Double {
        val mass = AtomContainerManipulator.getMass(iContainer)
        return mass
    }
}