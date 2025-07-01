package uk.co.jcox.chemvis.application.moleditor

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

    //Essentially the entities this atom is connected by through bonds
    val connectedEntities: MutableList<UUID> = mutableListOf()

) : IComponent {
    override fun clone(): IComponent {
        return AtomComponent(ArrayList(connectedEntities))
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