package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import java.util.UUID

object LevelMolLinkUtil {
    
    fun linkObject(structMolecule: UUID, levelMolecule: EntityLevel) {
        levelMolecule.addComponent(MolIDComponent(structMolecule))
    }
}