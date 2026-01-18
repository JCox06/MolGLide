package uk.co.jcox.chemvis.application.data

import org.tinylog.Logger
import uk.co.jcox.chemvis.application.graph.LevelContainer

class LevelSerializer {


    fun getDataLevel(levelContainer: LevelContainer) : DataSaveFile {

        val saveFile: DataSaveFile = DataSaveFile()
        val IDMappings = generateLevelIDs(levelContainer)

        levelContainer.sceneMolecules.forEach { molecule ->

            val dataMolecule = DataMolecule(IDMappings.containers[molecule]!!, molecule.positionOffset)
            saveFile.molecules.add(dataMolecule)

            molecule.atoms.forEach { atom ->
                val id = IDMappings.atoms[atom]!!
                val dataAtom = DataAtom(id, atom.getSymbol(), atom.visible, atom.implicitHydrogenPos, atom.iAtom.point2d, atom.visible)
                dataMolecule.atoms.add(id)
                saveFile.atoms[id] = dataAtom
            }

            molecule.bonds.forEach { bond ->
                println("Saving bond data")
                val id = IDMappings.bonds[bond]!!
                val atomAID = IDMappings.atoms[bond.atomA]!!
                val atomBID = IDMappings.atoms[bond.atomB]!!

                dataMolecule.bonds.add(id)
                val dataBond = DataBond(id, atomAID, atomBID, bond.centredBond, bond.bisectorNudge, bond.flipDoubleBond, bond.bondOffset, bond.iBond.order, bond.getStereo(), bond.iBond.isAromatic)
                saveFile.bonds[id] = dataBond
            }
        }
        return saveFile
    }


    private fun generateLevelIDs(levelContainer: LevelContainer) : DataObjectIDMap {
        Logger.info { "Creating ID Mappings for this level for saving..." }


        var atomID = 0
        var bondID = 0
        var moleID = 0

        val dataMapping = DataObjectIDMap()

        levelContainer.sceneMolecules.forEach { molecule ->
            dataMapping.containers[molecule] = moleID++

            molecule.atoms.forEach { atom ->
                dataMapping.atoms[atom] = atomID++
            }

            molecule.bonds.forEach { bond ->
                dataMapping.bonds[bond] = bondID++
            }
        }

        return dataMapping
    }
}