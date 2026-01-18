package uk.co.jcox.chemvis.application.data

import org.apache.commons.logging.Log
import org.checkerframework.checker.units.qual.mol
import org.tinylog.Logger
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.action.DirectAtomCreationAction
import uk.co.jcox.chemvis.application.moleditorstate.action.DirectBondCreationAction
import uk.co.jcox.chemvis.application.moleditorstate.action.DirectMoleculeCreationAction
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class LevelLoader {

    private val idChemAtomMap: MutableMap<Int, ChemAtom> = mutableMapOf()

    fun loadLevel(mgf: File) : LevelContainer {
        Logger.info{"Loading MolGLide file and building DataSaveFile ${mgf.absoluteFile}"}
        val saveFile = loadToSaveFile(mgf) ?: return LevelContainer()
        Logger.info { "Data Save File Object successfully built!" }

        Logger.info { "Reconstructing CDK Level and building Actions...." }

        val level = reconstructCDKLevel(saveFile)

        Logger.info { "Level has been reconstructed !" }

        return level
    }

    private fun reconstructCDKLevel(saveFile: DataSaveFile) : LevelContainer {
        val levelContainer = LevelContainer()
        val levelActionBuilder = ActionManager(levelContainer)

        runMoleculeActions(saveFile, levelActionBuilder)

        return levelContainer
    }

    private fun runMoleculeActions(saveFile: DataSaveFile, actionManager: ActionManager) {
        saveFile.molecules.forEach { molecule ->
            val directMoleculeCreationAction = DirectMoleculeCreationAction(molecule.offset.x, molecule.offset.y, molecule.offset.z)
            actionManager.executeAction(directMoleculeCreationAction)

            val chemMolecule = directMoleculeCreationAction.newMolecule

            //Collect all atoms from this molecule
            molecule.atoms.forEach { atomID ->
                val dataAtom = saveFile.atoms[atomID]!!
                val directAtomCreationAction = DirectAtomCreationAction(chemMolecule, dataAtom)
                actionManager.executeAction(directAtomCreationAction)
                idChemAtomMap[dataAtom.id] = directAtomCreationAction.newChemAtom
            }

            //Collect all bonds from this molecule
            molecule.bonds.forEach { bondsID ->
                val dataBond = saveFile.bonds[bondsID]!!
                val atomA = idChemAtomMap[dataBond.atomA]!!
                val atomB = idChemAtomMap[dataBond.atomB]!!
                val directBondCreationAction = DirectBondCreationAction(dataBond, chemMolecule, atomA, atomB)
                actionManager.executeAction(directBondCreationAction)
            }

            //Run CDK calculations on the molecule
            chemMolecule.calculateAtomTypes()
        }
    }


    private fun loadToSaveFile(mgf: File) : DataSaveFile? {
        var dataSaveFile: DataSaveFile? = null

        try {
            val fileInputStream = FileInputStream(mgf)
            val objectInputStream = ObjectInputStream(fileInputStream)
            dataSaveFile = objectInputStream.readObject() as DataSaveFile
        } catch (e: IOException) {
            Logger.error { "Error during file loading to DataSaveFile" }
            e.printStackTrace()
        }
        return dataSaveFile
    }
}