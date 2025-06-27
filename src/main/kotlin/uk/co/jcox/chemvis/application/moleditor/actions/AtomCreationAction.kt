package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil.linkObject
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil.linkParentLevel
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil.tagAsAtom
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil.tagAsExplicit
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent
import java.util.UUID

class AtomCreationAction (
    private val placementX: Float,
    private val placementY: Float,
    private val insert: AtomInsert
) : EditorAction(){

    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {
        //Create chemical struct info
        val structMolecule = molManager.createMolecule()
        val structAtom = molManager.addAtom(structMolecule, insert.symbol)


        //Create level info
        val levelMolecule = level.addEntity()
        levelMolecule.addComponent(TransformComponent(placementX, placementY, NewOrganicEditorState.XY_PLANE, 1.0f))

        val levelAtom = LevelViewUtil.createLabel(levelMolecule, insert.symbol, 0.0f, 0.0f)

        linkParentLevel(levelAtom, levelMolecule)

        //Link level object to struct object
        linkObject(structAtom, levelAtom)
        linkObject(structMolecule, levelMolecule)

        //Add tags
        tagAsAtom(levelAtom)
        tagAsExplicit(levelAtom)

        //Now everything is added calculate the number of hydrogens that are required to make a neutral molecule
        if (insert.hydrogenable) {
            molManager.recalculate(structMolecule)

            val implicitHydrogens = molManager.getImplicitHydrogens(structAtom)

            insertImplicitHydrogenGroup(levelAtom, implicitHydrogens)
        }

        return structMolecule
    }

}