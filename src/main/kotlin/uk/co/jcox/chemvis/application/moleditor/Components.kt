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

class MolSelectionComponent(
    val selectionEntity: UUID,
): IComponent {
    override fun clone(): IComponent {
        return MolSelectionComponent(selectionEntity)
    }

}

//The remaining classes are used to "tag" entities in the level for quick retrieval/identification

class AnchorComponent(

) : IComponent {
    override fun clone(): IComponent {
        return this
    }
}

class GhostImplicitHydrogenGroupComponent(

) : IComponent {
    override fun clone() : IComponent {
        return this
    }
}

class AtomComponent(

) : IComponent {
    override fun clone(): IComponent {
        return this
    }
}

class AlwaysExplicit(

) : IComponent {
    override fun clone(): IComponent {
        return this
    }
}