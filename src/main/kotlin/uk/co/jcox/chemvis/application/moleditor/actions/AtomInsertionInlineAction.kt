package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.LevelMolLinkUtil
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import java.util.UUID

class AtomInsertionInlineAction(
    private val levelPlaceholder: EntityLevel,
    private val insert: AtomInsert,

    ) : EditorAction() {
    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {

        val atomOfAnchor = levelPlaceholder.parent
        val molecule = atomOfAnchor?.parent

        if (atomOfAnchor == null) {
            return null
        }

        if (molecule == null) {
            return null
        }

        //"Transform" the anchor/placeholder into an atom
        val levelAtom = LevelViewUtil.placeHolderTransform(molecule, levelPlaceholder, insert.symbol)

        if (levelAtom == null) {
            return null
        }

        LevelViewUtil.tagAsAtom(levelAtom)

        //Struct side
        val structMolecule = molecule.getComponent(MolIDComponent::class).molID
        val structAtom = molManager.addAtom(structMolecule, insert.symbol)
        molManager.formBond(structMolecule, atomOfAnchor.getComponent(MolIDComponent::class).molID, structAtom, 1)

        LevelMolLinkUtil.linkObject(structAtom, levelAtom)

        return structMolecule
    }
}