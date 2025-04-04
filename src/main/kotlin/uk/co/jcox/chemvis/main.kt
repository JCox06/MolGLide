package uk.co.jcox.chemvis

import org.openscience.cdk.Atom
import org.openscience.cdk.interfaces.IAtom
import java.io.File


fun main() {
    
    val application = ChemVis()
    application.run()
    application.destroy()

}