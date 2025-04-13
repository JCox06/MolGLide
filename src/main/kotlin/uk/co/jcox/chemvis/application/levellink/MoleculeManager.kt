package uk.co.jcox.chemvis.application.levellink

import org.openscience.cdk.AtomContainer
import org.openscience.cdk.interfaces.IAtomContainer
import java.util.UUID

class MoleculeManager {

    private val molecules: MutableMap<UUID, CDKBridge> = mutableMapOf()

    fun createEmptyMolecule() {
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
    }


    data class CDKBridge (
        val molID: UUID,
        val cdk: IAtomContainer,
        val molLink: MoleculeLink,
    )


    companion object {
        private val MOLID_PROPERTY = "molid"
    }
}