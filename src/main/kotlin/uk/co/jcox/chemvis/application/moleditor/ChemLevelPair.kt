package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class ChemLevelPair (
    val level: EntityLevel,
    val molManager: IMoleculeManager,

) {

    fun clone() : ChemLevelPair {
        val levelCopy = level.clone(null)
        val molCopy = molManager.clone()

        return ChemLevelPair(levelCopy, molCopy)
    }
}