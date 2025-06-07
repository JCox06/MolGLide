package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.GhostImplicitHydrogenGroupComponent
import uk.co.jcox.chemvis.application.moleditor.LevelMolLinkUtil
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.OrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

class AtomCreationAction(
    private val xPos: Float,
    private val yPos: Float,
    private val insert: AtomInsert,
    private val insertImplicitHydrogens: Boolean,
) : EditorAction() {


    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {
        //1) Create the new molecule to house this new atom on the struct side
        val structMolecule = molManager.createMolecule()
        val structFirstAtom = molManager.addAtom(structMolecule, insert.symbol)



        //2) Update spatial representation on the level side
        val levelMolecule = level.addEntity()
        levelMolecule.addComponent(TransformComponent(xPos, yPos, OrganicEditorState.Companion.XY_PLANE, 1.0f))
        LevelMolLinkUtil.linkObject(structMolecule, levelMolecule)

        val levelFirstAtom = LevelViewUtil.createLabel(levelMolecule, insert.symbol, 0.0f, 0.0f)
        LevelMolLinkUtil.linkObject(structFirstAtom, levelFirstAtom)

        //3) Create selection marker and anchors
        LevelViewUtil.createSelectionMarker(levelFirstAtom)

        LevelViewUtil.createInlinePlaceholder(levelFirstAtom, OrganicEditorState.INLINE_DIST, 0.0f, 0.0f)
        LevelViewUtil.createInlinePlaceholder(levelFirstAtom, -OrganicEditorState.INLINE_DIST, 0.0f, 0.0f)
        LevelViewUtil.createInlinePlaceholder(levelFirstAtom, 0.0f , OrganicEditorState.INLINE_DIST, 0.0f)
        LevelViewUtil.createInlinePlaceholder(levelFirstAtom, 0.0f , -OrganicEditorState.INLINE_DIST, 0.0f)


        LevelViewUtil.tagAsAtom(levelFirstAtom)
        LevelViewUtil.tagAsExplicit(levelFirstAtom)



        //4) Check if we need to insert the implicit hydrogens
        //eg - Make methane if just C was inserted
        if (insertImplicitHydrogens && insert.hydrogenable) {
            molManager.recalculate(structMolecule)
            val inserted = molManager.getImplicitHydrogens(structMolecule, structFirstAtom)
            //These implicit hydrogens that have been inserted need to be shown
            val fakeH = LevelViewUtil.createLabel(levelFirstAtom, "H$inserted", OrganicEditorState.INLINE_DIST, 0.0f)
            fakeH.addComponent(GhostImplicitHydrogenGroupComponent())
        }

        return structMolecule
    }
}