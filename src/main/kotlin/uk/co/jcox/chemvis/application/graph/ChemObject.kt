package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import java.util.UUID

abstract class ChemObject (
    val localPos: Vector3f,
    val molManagerLink: UUID,
    val themeStyle: ThemeStyle,
)