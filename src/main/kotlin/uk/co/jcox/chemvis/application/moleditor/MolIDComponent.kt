package uk.co.jcox.chemvis.application.moleditor

import org.checkerframework.checker.units.qual.mol
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
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