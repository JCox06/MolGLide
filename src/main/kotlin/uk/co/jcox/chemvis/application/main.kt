package uk.co.jcox.chemvis.application

import uk.co.jcox.chemvis.cvengine.CVEngine
import uk.co.jcox.chemvis.cvengine.IApplication


fun main() {
    val engine = CVEngine("ChemVis")
    val cvApp: IApplication = ChemVis()
    engine.run(cvApp)
    engine.close()
}
