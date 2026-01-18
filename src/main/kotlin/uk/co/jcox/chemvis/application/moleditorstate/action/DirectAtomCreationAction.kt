package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.Atom
import uk.co.jcox.chemvis.application.data.DataAtom
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class DirectAtomCreationAction (
    private val molecule: ChemMolecule,
    private val dataAtom: DataAtom,
) : IAction{


    lateinit var newChemAtom: ChemAtom

    override fun execute(levelContainer: LevelContainer) {

        val atom = Atom(dataAtom.symbol)
        atom.point2d = dataAtom.internalPosition


        val chemAtom = molecule.addAtom(atom)
        chemAtom.parent = molecule
        chemAtom.implicitHydrogenPos = dataAtom.labelPosition
        chemAtom.visible = dataAtom.visible

        newChemAtom = chemAtom
    }

    override fun undo(levelContainer: LevelContainer) {
        molecule.removeAtom(newChemAtom)
    }
}