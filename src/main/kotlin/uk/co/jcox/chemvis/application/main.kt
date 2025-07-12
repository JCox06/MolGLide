package uk.co.jcox.chemvis.application

import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import uk.co.jcox.chemvis.cvengine.CVEngine
import uk.co.jcox.chemvis.cvengine.IApplication


fun main() {
    val engine = CVEngine("MolGLide ${MolGLide.VERSION} - ${LocalDate.now()}")
    val cvApp: IApplication = MolGLide()
    engine.run(cvApp)
    engine.close()
}
