package uk.co.jcox.chemvis.application.moleditor.actions

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import java.util.UUID

class ElementalEditAction (
    private val levelAtom: EntityLevel,
    private val replacement: AtomInsert
) : EditorAction() {

    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {

        if (!levelAtom.hasComponent(MolIDComponent::class) || !levelAtom.hasComponent(TextComponent::class)) {
            return null
        }

        val structAtom = levelAtom.getComponent(MolIDComponent::class)

        molManager.replace(structAtom.molID, replacement.symbol)

        val textComp = levelAtom.getComponent(TextComponent::class)
        textComp.text = replacement.symbol

        val parentMolID = LevelViewUtil.getLvlMolFromLvlAtom(levelAtom)

        if (parentMolID == null) {
            return null
        }

        val parentMol = level.findByID(parentMolID)

        if (parentMol == null) {
            return null
        }

        val structMol = parentMol.getComponent(MolIDComponent::class)

        molManager.recalculate(structMol.molID)

        removeImplicitHydrogenGroup(levelAtom)

        val hydrogenCount = molManager.getImplicitHydrogens(structAtom.molID)

        insertImplicitHydrogenGroup(levelAtom, hydrogenCount)

        return structMol.molID
    }
}