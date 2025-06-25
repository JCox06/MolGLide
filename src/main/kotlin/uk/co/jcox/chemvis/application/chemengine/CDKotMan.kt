package uk.co.jcox.chemvis.application.chemengine

import org.openscience.cdk.Atom
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.Bond
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
import org.tinylog.Logger
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import java.util.UUID

class CDKotMan (
    private val molecules: MutableMap<UUID, IAtomContainer> = mutableMapOf(),
    private val bonds: MutableMap<UUID, IBond> = mutableMapOf(),
    private val atoms: MutableMap<UUID, IAtom> = mutableMapOf(),
) : IMoleculeManager{



    override fun createMolecule(): UUID {
        val cdkContainer: IAtomContainer = AtomContainer()
        val id = UUID.randomUUID()
        molecules[id] = cdkContainer
        cdkContainer.setProperty(MANAGER_ID, id)

        return id
    }

    override fun addAtom(molecule: UUID, element: String): UUID {
        val cdkContainer = molecules[molecule]

        if (cdkContainer == null) {
            throw NoSuchElementException("Unknown molecule with $molecule")
        }

        val atom: IAtom = Atom(element)
        val id = UUID.randomUUID()
        atoms[id] = atom
        atom.setProperty(MANAGER_ID, id)

        cdkContainer.addAtom(atom)

        return id
    }

    override fun formBond(moleculeID: UUID, atom1: UUID, atom2: UUID, bondOrder: Int): UUID {

        val cdkMolecule = molecules[moleculeID]
        val cdkAtom1 = atoms[atom1]
        val cdkAtom2 = atoms[atom2]

        if (cdkMolecule == null || cdkAtom1 == null || cdkAtom2 == null) {
            throw NoSuchElementException("The molecule or atoms were null")
        }

        val bond: IBond = Bond(cdkAtom1, cdkAtom2, IBond.Order.SINGLE)

        val id = UUID.randomUUID()
        bonds[id] = bond
        bond.setProperty(MANAGER_ID, id)

        cdkMolecule.addBond(bond)

        return id
    }

    override fun getMolecularFormula(moleculeID: UUID): String {
        val molecule = molecules[moleculeID]
        if (molecule == null) {
            return ""
        }

        val formula = MolecularFormulaManipulator.getMolecularFormula(molecule)
        return MolecularFormulaManipulator.getString(formula)
    }

    override fun getBonds(moleculeID: UUID, atom: UUID): Int {
        val molecule = molecules[moleculeID]
        val cdkAtom = atoms[atom]

        if (molecule == null || cdkAtom == null) {
            throw NoSuchElementException("Molecule or atom had an unknown ID")
        }

        return molecule.getConnectedBondsCount(cdkAtom) + cdkAtom.implicitHydrogenCount
    }

    override fun isOfElement(moleculeID: UUID, atom: UUID, element: String): Boolean {
        val cdkAtom = atoms[atom]

        if (cdkAtom == null) {
            throw NoSuchElementException("Atom was null")
        }

        return cdkAtom.symbol == element
    }

    override fun recalculate(molecule: UUID) {
        val cdkMolecule = molecules[molecule]
        if (cdkMolecule == null) {
            return
        }

        try {
            val atomMatcher = CDKAtomTypeMatcher.getInstance(cdkMolecule.builder)

            for (atom in cdkMolecule.atoms()) {

                atom.atomTypeName = null
                atom.valency = null
                atom.hybridization = null
                atom.formalNeighbourCount = null
                atom.bondOrderSum = null
                atom.implicitHydrogenCount = null

                val atomType = atomMatcher.findMatchingAtomType(cdkMolecule, atom)
                AtomTypeManipulator.configure(atom, atomType)
            }

            val hydrogenAdder = CDKHydrogenAdder.getInstance(cdkMolecule.builder)
            hydrogenAdder.addImplicitHydrogens(cdkMolecule)

            for (atom in cdkMolecule.atoms()) {
                if (!AtomInsert.fromSymbol(atom.symbol).hydrogenable) {
                    atom.implicitHydrogenCount = 0
                }
            }
        } catch (e: CDKException) {
            Logger.error ("Error when calculating molecule type {}", e)
        }
    }

    override fun getImplicitHydrogens(molecule: UUID, atom: UUID): Int {
        val cdkAtom = atoms[atom]

        val count = cdkAtom?.implicitHydrogenCount

        if (count == null) {
            return 0
        }

        return count
    }

    override fun updateBondOrder(molecule: UUID, bond: UUID, newBondOrder: Int) {
        val cdkBond = bonds[bond]
        if (cdkBond == null) {
            throw NoSuchElementException("Bond was null")
        }

        cdkBond.order = IBond.Order.DOUBLE
    }

    override fun getJoiningBond(molecule: UUID, atomA: UUID, atomB: UUID): UUID? {
        val cdkMolecule = molecules[molecule]
        val cdkAtomA = atoms[atomA]
        val cdkAtomB = atoms[atomB]

        if (cdkMolecule == null || cdkAtomA == null || cdkAtomB == null) {
            throw NoSuchElementException("Molecule or bonds IDs were incorrect")
        }

        val bond = cdkMolecule.getBond(cdkAtomA, cdkAtomB)

        if (bond == null) {
            return null
        }

        val bondID = bond.getProperty<UUID>(MANAGER_ID)

        if (bondID == null) {
            throw NoSuchElementException("Retrieved bond did not have an ID")
        }

        return bondID
    }

    override fun clone(): IMoleculeManager {
        val moleculeCloneMap: MutableMap<UUID, IAtomContainer> = mutableMapOf()
        val moleculeBondMap: MutableMap<UUID, IBond> = mutableMapOf()
        val moleculeAtomMap: MutableMap<UUID, IAtom> = mutableMapOf()

        //Clone all the atom containers in the level
        molecules.forEach { key, value ->
            val molClone = value.clone()

            moleculeCloneMap[key] = molClone

            //Now go through the current molecules bonds
            molClone.bonds().forEach {iBond ->
                val iBondID = iBond.getProperty<UUID>(MANAGER_ID)
                if (iBondID == null) {
                    throw NoSuchElementException("Clone ERROR - Bond ID is missing")
                }

                moleculeBondMap[iBondID] = iBond
            }

            //Now go through the current molecules atoms
            molClone.atoms().forEach {iAtom ->

                val iAtomID = iAtom.getProperty<UUID>(MANAGER_ID)
                if (iAtomID == null) {
                    throw NoSuchElementException("Clone ERROR - Atom ID is missing")
                }
                moleculeAtomMap[iAtomID] = iAtom
            }
        }

        return CDKotMan(moleculeCloneMap, moleculeBondMap, moleculeAtomMap)
    }

    companion object {
        private const val MANAGER_ID = "managerID"
    }
}