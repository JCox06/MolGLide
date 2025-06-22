package uk.co.jcox.chemvis.application.moleditor

import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.WindowRenderingContext

data class ToolCreationContext(
     val levelStack: WorkState,
     val inputManager: InputManager,
     val renderingContext: IRenderTargetContext,
     val selectionManager: SelectionManager,
     val camera2D: Camera2D,
)
