package uk.co.jcox.chemvis.application.moleditorstate.action

import org.joml.Vector3f
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.AtomInsert
import java.util.UUID

/**
 * Although this class will insert an atom into the world
 * It does not require a position.
 *
 * The position is handled by AtomBondTool when it is in Dragging mode, this is finalised when a key is released
 */
class AtomInsertionAction (
    private val atomInsert: AtomInsert,
    private val srcAtom: ChemAtom,
) : IAction {

    private val srcMol = srcAtom.parent

    private var newStructAtom: UUID? = null
    var newLevelAtom: ChemAtom? = null
    private var newStructBond: UUID? = null
    private var newLevelBond: ChemBond? = null

    private var wasVisibleBefore = false

    override fun execute(levelContainer: LevelContainer) {
        //First turn off hydrogens for the old atom if it is carbon (only for source carbon)
        disableHydrogensForCarbon(levelContainer)

        //Now create the new atom
        val newStructAtom = createNewStructAtom(levelContainer, srcMol.molManagerLink, atomInsert.symbol)

        //Create the new atom level view side
        val newLevelAtom = ChemAtom(Vector3f(0.0f, 0.0f, 0.0f), newStructAtom, srcMol)
        srcMol.atoms.add(newLevelAtom)

        //Create the bonds struct side
        val newStructBond = levelContainer.chemManager.formBond(srcMol.molManagerLink, srcAtom.molManagerLink, newStructAtom, 1)

        //Create the bond level side
        val newLevelBond = ChemBond(srcAtom, newLevelAtom, Vector3f(), newStructBond)
        srcMol.bonds.add(newLevelBond)

        this.newStructAtom = newStructAtom
        this.newLevelAtom = newLevelAtom
        this.newStructBond = newStructBond
        this.newLevelBond = newLevelBond

        updateImplicitHydrogens(levelContainer, srcAtom, srcAtom.molManagerLink, srcMol.molManagerLink)
        updateImplicitHydrogens(levelContainer, newLevelAtom, newStructAtom, srcMol.molManagerLink)

        //And finally, if the atom added was carbon, we do not need to include the hydrogens
        if (atomInsert == AtomInsert.CARBON) {
            newLevelAtom.visible = false
        }
    }

    override fun undo(levelContainer: LevelContainer) {


        //Does the opposite of execute
        srcAtom.visible = wasVisibleBefore

        srcMol.atoms.remove(newLevelAtom)
        srcMol.bonds.remove(newLevelBond)

        val mol = srcMol.molManagerLink

        newStructBond?.let { levelContainer.chemManager.deleteBond(mol, it) }
        newStructAtom?.let { levelContainer.chemManager.deleteAtom(mol, it) }


        updateImplicitHydrogens(levelContainer, srcAtom, srcAtom.molManagerLink, srcMol.molManagerLink)
    }

    private fun disableHydrogensForCarbon(levelContainer: LevelContainer) {
        wasVisibleBefore = srcAtom.visible


        val srcAtomSymbol = levelContainer.chemManager.getAtomInsert(srcAtom.molManagerLink)

        if (srcAtomSymbol == AtomInsert.CARBON) {
            srcAtom.visible = false
        }
    }


    private fun createNewStructAtom(levelContainer: LevelContainer, mol: UUID, symbol: String): UUID {
        val srcStructMolcule = srcMol.molManagerLink

        val newSrcAtom = levelContainer.chemManager.addAtom(srcStructMolcule, atomInsert)
        return newSrcAtom
    }

    private fun updateImplicitHydrogens(levelContainer: LevelContainer, srcLevelAtom: ChemAtom, srcStructAtom: UUID, srcStructMol: UUID) {
        levelContainer.chemManager.recalculate(srcStructMol)

        val newH = levelContainer.chemManager.getImplicitHydrogens(srcStructAtom)

        srcLevelAtom.implicitHydrogenCount = newH
    }
}