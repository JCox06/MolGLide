package uk.co.jcox.chemvis.application.moleditor

import org.apache.jena.sparql.function.library.bnode
import uk.co.jcox.chemvis.cvengine.scenegraph.IComponent
import java.util.UUID

class MolIDComponent (
    val molID: UUID
) : IComponent {
    override fun clone(): IComponent {
        return MolIDComponent(molID)
    }
}

class LevelParentComponent(
    val levelParentID: UUID
): IComponent {
    override fun clone(): IComponent {
        return LevelParentComponent(levelParentID)
    }
}


class AtomComponent(

    val bondsTo: MutableList<UUID> = mutableListOf()

) : IComponent {
    override fun clone(): IComponent {
        return AtomComponent(ArrayList(bondsTo))
    }
}


//The remaining classes are used to "tag" entities in the level for quick retrieval/identification


class GhostImplicitHydrogenGroupComponent(

) : IComponent {
    override fun clone() : IComponent {
        return this
    }
}

class AlwaysExplicit(

) : IComponent {
    override fun clone(): IComponent {
        return this
    }
}