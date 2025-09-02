package uk.co.jcox.chemvis.application.graph

import uk.co.jcox.chemvis.application.chemengine.CDKotMan
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager

class LevelContainer {

    val levelMolecules = mutableListOf<ChemMolecule>()
    val structManager: IMoleculeManager = CDKotMan()
}