package uk.co.jcox.chemvis.application.moleditor.actions

import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.p
import org.openscience.cdk.smiles.smarts.parser.SMARTSParserConstants.v
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.application.moleditor.AtomInsert
import uk.co.jcox.chemvis.application.moleditor.LevelMolLinkUtil
import uk.co.jcox.chemvis.application.moleditor.LevelViewUtil
import uk.co.jcox.chemvis.application.moleditor.MolIDComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import java.util.UUID

class ElementalEditAction (
    private val atomLevel: EntityLevel,
    private val replacement: AtomInsert
) : EditorAction() {



    override fun execute(molManager: IMoleculeManager, level: EntityLevel): UUID? {

        if (! atomLevel.hasComponent(MolIDComponent::class)) {
            return null
        }

        if (! atomLevel.hasComponent(TextComponent::class)) {
            return null
        }

        //Update the struct:
        val molIDComp = atomLevel.getComponent(MolIDComponent::class)

        molManager.replace(molIDComp.molID, replacement.symbol)

        //Update the level
        val textComp = atomLevel.getComponent(TextComponent::class)
        textComp.text = replacement.symbol


        val parentMol = LevelViewUtil.getLvlMolFromLvlAtom(atomLevel)

        if (parentMol != null) {
            val levelMol = level.findByID(parentMol)

            if (levelMol != null) {
                val comp = levelMol.getComponent(MolIDComponent::class)

                return comp.molID
            }
        }

        return null
    }
}