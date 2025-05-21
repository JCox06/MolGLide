package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.IComponent
import java.util.UUID

class MolIDComponent (
    val molID: UUID
) : IComponent {
}

class MolSelectionComponent(
    val selectionEntity: EntityLevel,
): IComponent {

}