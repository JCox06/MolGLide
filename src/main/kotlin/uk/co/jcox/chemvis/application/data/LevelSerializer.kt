package uk.co.jcox.chemvis.application.data

import org.tinylog.Logger
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer

class LevelSerializer {


    fun getDataLevel(levelContainer: LevelContainer) : DataSaveFile {

        val saveFile: DataSaveFile = DataSaveFile()
        val IDMappings = generateLevelIDs(levelContainer)

        levelContainer.sceneMolecules.forEach { molecule ->
            serializeMolecule(saveFile, IDMappings, molecule)
        }
        return saveFile
    }

    fun serializeSpecificMoleculeData(molecule: ChemMolecule , selectedAtoms: List<ChemAtom>? = null, selectedBonds: List<ChemBond>? = null) : DataSaveFile {
        val tempContainer = LevelContainer()
        tempContainer.sceneMolecules.add(molecule)

        val saveFile: DataSaveFile = DataSaveFile()
        val IDMappings = generateLevelIDs(tempContainer)
        serializeMolecule(saveFile, IDMappings, molecule, selectedAtoms, selectedBonds)

        return saveFile
    }

    private fun serializeMolecule(saveFile: DataSaveFile, idMappings: DataObjectIDMap, molecule: ChemMolecule, selectedAtoms: List<ChemAtom>? = null, selectedBonds: List<ChemBond>? = null) {
        val dataMolecule = DataMolecule(idMappings.containers[molecule]!!, molecule.positionOffset)
        saveFile.molecules.add(dataMolecule)

        molecule.atoms.forEach { atom ->
            if (selectedAtoms == null || selectedAtoms.contains(atom)) {
                val id = idMappings.atoms[atom]!!
                val dataAtom = DataAtom(id, atom.getSymbol(), atom.visible, atom.implicitHydrogenPos, atom.iAtom.point2d, atom.visible)
                dataMolecule.atoms.add(id)
                saveFile.atoms[id] = dataAtom
            }
        }

        molecule.bonds.forEach { bond ->
            if (selectedBonds == null || selectedBonds.contains(bond)) {
                val id = idMappings.bonds[bond]!!
                val atomAID = idMappings.atoms[bond.atomA]!!
                val atomBID = idMappings.atoms[bond.atomB]!!

                dataMolecule.bonds.add(id)
                val dataBond = DataBond(id, atomAID, atomBID, bond.centredBond, bond.bisectorNudge, bond.flipDoubleBond, bond.bondOffset, bond.iBond.order, bond.getStereo(), bond.iBond.isAromatic)
                saveFile.bonds[id] = dataBond
            }
        }
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