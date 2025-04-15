package uk.co.jcox.chemvis.application.levellink

import org.joml.Vector2f
import uk.ac.ebi.beam.Bond
import java.util.UUID

data class MoleculeLink(
    val molId: UUID,
    val bondLinks: MutableMap<UUID, BondLink> = mutableMapOf(),
    val atomLinks: MutableMap<UUID, AtomLink> = mutableMapOf(),
)