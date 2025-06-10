package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.InputManager

data class ToolCreationContext(
     val levelStack: WorkState,
     val inputManager: InputManager,
     val selectionManager: SelectionManager,
     val camera2D: Camera2D,
)
