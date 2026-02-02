package uk.co.jcox.chemvis.application.moleditorstate.action

import org.openscience.cdk.Atom
import org.openscience.cdk.Element
import org.openscience.cdk.Isotope
import org.openscience.cdk.config.Elements
import org.openscience.cdk.config.IsotopeFactory
import org.openscience.cdk.interfaces.IAtom
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class CustomInputInsertionAction (
    private val customInput: String,
    private val atom: ChemAtom,
) : IAction {

    override fun execute(levelContainer: LevelContainer) {

    }

    override fun undo(levelContainer: LevelContainer) {

    }

}