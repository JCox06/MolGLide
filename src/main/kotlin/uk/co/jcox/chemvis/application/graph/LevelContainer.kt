package uk.co.jcox.chemvis.application.graph

import uk.co.jcox.chemvis.application.chemengine.CDKotMan
import uk.co.jcox.chemvis.application.chemengine.IMoleculeManager

class LevelContainer {

    val sceneMolecules = mutableListOf<ChemMolecule>()
    val chemManager: IMoleculeManager = CDKotMan()
}