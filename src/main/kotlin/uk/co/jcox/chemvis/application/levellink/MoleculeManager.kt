package uk.co.jcox.chemvis.application.levellink

import org.joml.Vector2f
import org.joml.Vector4f
import org.openscience.cdk.AtomContainer
import org.openscience.cdk.interfaces.IAtom
import org.openscience.cdk.interfaces.IAtomContainer
import java.util.UUID

class MoleculeManager {

    private val molecules: MutableMap<UUID, CDKBridge> = mutableMapOf()

    fun createEmptyMolecule() : UUID {
        val molID = UUID.randomUUID()
        val cdkMol = AtomContainer()
        cdkMol.setProperty(MOLID_PROPERTY, molID)
        val molLink = MoleculeLink(molID)

        val bridge = CDKBridge(
            molID,
            cdkMol,
            molLink
        )

        molecules[molID] = bridge
        return molID;
    }


    fun addAtom(molID: UUID, atom: IAtom) {
        val bridge = getBridge(molID)
        val atomID = UUID.randomUUID()
        atom.setProperty(ATOMID_PROPERTY, atomID)

        bridge.cdk.addAtom(atom)

        bridge.molLink.atomLinks[atomID] = AtomLink(atomID, Vector4f(0.0f, 0.0f, 0.0f, 0.0f))
    }


    fun getMolecules() : List<UUID> {
        return molecules.keys.toList()
    }

    fun getAtoms(molID: UUID) : List<UUID> {
        return getBridge(molID).molLink.atomLinks.keys.toList()
    }

    fun getBridge(molID: UUID) : CDKBridge {
        val bridge = molecules[molID]
        if (bridge == null) {
            throw NullPointerException("Molecule is null")
        } else {
            return bridge
        }
    }

    data class CDKBridge (
        val molID: UUID,
        val cdk: IAtomContainer,
        val molLink: MoleculeLink,
    )


    companion object {
        private val MOLID_PROPERTY = "molid"
        private val ATOMID_PROPERTY = "atomid"
    }
}