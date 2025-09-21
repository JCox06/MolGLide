package uk.co.jcox.chemvis.application.chemengine

import org.openscience.cdk.Atom
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.Bond
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher
import org.openscience.cdk.exception.CDKException
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.io.SMILESWriter
import org.openscience.cdk.smiles.SmilesGenerator
import org.openscience.cdk.tools.CDKHydrogenAdder
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator
import org.tinylog.Logger
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import uk.co.jcox.chemvis.application.moleditorstate.StereoChem
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


    override fun deleteMolecule(uuid: UUID) {
        molecules.remove(uuid)
    }

    override fun deleteAtom(mol: UUID, atom: UUID) {
        val cdkAtom = atoms.remove(atom)
        val container = molecules[mol]

        container?.removeAtom(cdkAtom)
    }

    override fun deleteBond(mol: UUID, bond: UUID) {
        val bond = bonds.remove(bond)
        val container = molecules[mol]
        container?.removeBond(bond)
    }

    override fun addAtom(molecule: UUID, insert: AtomInsert): UUID {
        val cdkContainer = molecules[molecule]

        if (cdkContainer == null) {
            throw NoSuchElementException("Unknown molecule with $molecule")
        }

        val atom: IAtom = Atom(insert.symbol)
        val id = UUID.randomUUID()
        atoms[id] = atom
        atom.setProperty(MANAGER_ID, id)

        cdkContainer.addAtom(atom)

        return id
    }

    override fun formBond(moleculeID: UUID, atom1: UUID, atom2: UUID, bondOrder: BondOrder, stereoChem: StereoChem): UUID {

        val cdkMolecule = molecules[moleculeID]
        val cdkAtom1 = atoms[atom1]
        val cdkAtom2 = atoms[atom2]

        if (cdkMolecule == null || cdkAtom1 == null || cdkAtom2 == null) {
            throw NoSuchElementException("The molecule or atoms were null")
        }

        val cdkBondOrder = getCDKBondOrder(bondOrder)
        val cdkStereoChem = getCDKStereochem(stereoChem)

        val bond: IBond = Bond(cdkAtom1, cdkAtom2, cdkBondOrder, cdkStereoChem)

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

    override fun getBondCount(moleculeID: UUID, atom: UUID): Int {
        val molecule = molecules[moleculeID]
        val cdkAtom = atoms[atom]

        if (molecule == null || cdkAtom == null) {
            throw NoSuchElementException("Molecule or atom had an unknown ID")
        }

        return molecule.getConnectedBondsCount(cdkAtom) + cdkAtom.implicitHydrogenCount
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

    override fun getImplicitHydrogens(atom: UUID): Int {
        val cdkAtom = atoms[atom]

        val count = cdkAtom?.implicitHydrogenCount

        if (count == null) {
            return 0
        }

        return count
    }

    override fun updateBondOrder(molecule: UUID, bond: UUID, newBondOrder: BondOrder) {
        val cdkBond = bonds[bond]
        if (cdkBond == null) {
            throw NoSuchElementException("Bond was null")
        }

        cdkBond.order = getCDKBondOrder(newBondOrder)
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


    override fun getAtomInsert(atom: UUID): AtomInsert {
        val cdkAtom = atoms[atom]

        if (cdkAtom == null) {
            return AtomInsert.UNKNOWN
        }
        return AtomInsert.fromSymbol(cdkAtom.symbol)
    }

    override fun replace(atom: UUID, insert: AtomInsert) {
        val cdkAtom = atoms[atom]

        if (cdkAtom == null) {
            throw NoSuchElementException("Atom is null")
        }
        cdkAtom.symbol = insert.symbol
    }

    override fun getBondOrder(bond: UUID): BondOrder {
        val bond = bonds[bond]

        if (bond == null) {
            throw RuntimeException("Could not get the bond specified to calculate bond order")
        }

        val order = getMolGLideBondOrder(bond.order)

        return order
    }

    private fun getMolGLideBondOrder(bondOrder: IBond.Order) : BondOrder {
        val proposed = BondOrder.entries.find { it.number == bondOrder.numeric() }

        if (proposed == null) {
            throw RuntimeException("Could not translate a CDK bond order to a MolGLide bond order")
        }

        return proposed
    }

    private fun getCDKBondOrder(bondOrder: BondOrder) : IBond.Order {
        val proposed = IBond.Order.entries.find { it.numeric() == bondOrder.number}

        if (proposed == null) {
            throw RuntimeException("Could not translate a MolGLide bond order to a CDK bond order: $bondOrder")
        }
        return proposed
    }

    private fun getCDKStereochem(stereoChem: StereoChem): IBond.Stereo {
        return when (stereoChem) {
            StereoChem.IN_PLANE -> IBond.Stereo.NONE
            StereoChem.FACING_VIEW -> IBond.Stereo.UP
            StereoChem.FACING_PAPER -> IBond.Stereo.DOWN
        }
    }

    private fun getMolGLideStereoChem(stereo: IBond.Stereo) : StereoChem {
        return when (stereo) {
            IBond.Stereo.NONE -> StereoChem.IN_PLANE
            IBond.Stereo.UP -> StereoChem.FACING_VIEW
            IBond.Stereo.DOWN -> StereoChem.FACING_PAPER
            else -> StereoChem.IN_PLANE
        }
    }

    override fun getStereoChem(bond: UUID): StereoChem {
        val cdkBond = bonds[bond]

        if (cdkBond == null) {
            throw RuntimeException("Could not get the bond specified to calculate the Stereo Chemistry")
        }

        return getMolGLideStereoChem(cdkBond.stereo)
    }


    override fun updateStereoChem(bond: UUID, stereoChem: StereoChem) {
        val cdkBond = bonds[bond]

        if (cdkBond == null) {
            throw RuntimeException("Could not get the bond specified to update the stereochemistry")
        }

        cdkBond.stereo = getCDKStereochem(stereoChem)
    }

    companion object {
        private const val MANAGER_ID = "managerID"
    }
}