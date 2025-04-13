package uk.co.jcox.chemvis.application.levellink

import org.joml.Vector2f
import java.util.UUID

data class MoleculeLink(
    val molId: UUID,
    val bondLinks: MutableList<BondLink> = mutableListOf(),
    val atomLinks: MutableList<AtomLink> = mutableListOf(),
)