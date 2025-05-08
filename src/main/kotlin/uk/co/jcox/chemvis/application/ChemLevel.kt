package uk.co.jcox.chemvis.application

import org.apache.jena.sparql.function.library.uuid
import org.joml.Vector2f
import org.joml.Vector3f
import java.util.UUID

//Manage the physical editor layout of molecules in the editor
class ChemLevel {
    private val atomPositions: MutableMap<UUID, Vector2f> = mutableMapOf()


    fun addAtom(atom: UUID, pos: Vector2f) {
        this.atomPositions[atom] = pos
    }

    fun getPosition(atom: UUID) : Vector2f {
        return this.atomPositions[atom] ?: Vector2f(0.0f, 0.0f)
    }
}