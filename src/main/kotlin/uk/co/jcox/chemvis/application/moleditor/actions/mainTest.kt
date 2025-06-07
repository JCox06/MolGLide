package uk.co.jcox.chemvis.application.moleditor.actions


import uk.co.jcox.chemvis.application.chemengine.CDKManager
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

fun main() {
    val molMan = CDKManager()

    val molecule = molMan.createMolecule()
    val carbon1 = molMan.addAtom(molecule, "C")

    molMan.recalculate(molecule)
//    println("Implicit Hydrogens After: ${molMan.getImplicitHydrogens(molecule, carbon1)}")

    val carbon2 = molMan.addAtom(molecule, "C")

    val bond = molMan.formBond(molecule, carbon1, carbon2, 1)

    molMan.recalculate(molecule)
    println("Implicit Hydrogens Carbon1: ${molMan.getImplicitHydrogens(molecule, carbon1)}")
    println("Implicit Hydrogens Carbon2: ${molMan.getImplicitHydrogens(molecule, carbon2)}")
}