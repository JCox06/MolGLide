package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.LevelMolLinkUtil
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

class AtomCreationAction(
    private val xPos: Float,
    private val yPos: Float,
    private val insert: AtomInsert,
) : EditorAction() {


    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {
        //1) Create the new molecule to house this new atom on the struct side
        val structMolecule = molManager.createMolecule()
        val structFirstAtom = molManager.addAtom(structMolecule, insert.symbol)

        //2) Update spatial representation on the level side
        val levelMolecule = level.addEntity()
        levelMolecule.addComponent(TransformComponent(xPos, yPos, NewOrganicEditorState.Companion.XY_PLANE, 1.0f))
        LevelMolLinkUtil.linkObject(structMolecule, levelMolecule)

        val levelFirstAtom = LevelViewUtil.createLabel(levelMolecule, insert.symbol, 0.0f, 0.0f)
        LevelMolLinkUtil.linkObject(structFirstAtom, levelFirstAtom)

        LevelViewUtil.tagAsAtom(levelFirstAtom)
        LevelViewUtil.tagAsExplicit(levelFirstAtom)

        //Always add implicit hydrogens
        if (insert.hydrogenable) {
            molManager.recalculate(structMolecule)
            val inserted = molManager.getImplicitHydrogens(structMolecule, structFirstAtom)
            val fakeH = LevelViewUtil.createLabel(levelFirstAtom, "H$inserted", NewOrganicEditorState.INLINE_DIST, 0.0f)
            println(inserted)
            fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
        }

        return structMolecule
    }
}