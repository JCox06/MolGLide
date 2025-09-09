package uk.co.jcox.chemvis.application.moleditorstate.tool

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.minus
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.ActionManager
import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.SelectionManager
import uk.co.jcox.chemvis.cvengine.CVEngine
import uk.co.jcox.chemvis.cvengine.Camera2D
import uk.co.jcox.chemvis.cvengine.IRenderTargetContext
import uk.co.jcox.chemvis.cvengine.IResourceManager
import uk.co.jcox.chemvis.cvengine.InputManager
import uk.co.jcox.chemvis.cvengine.ResourceManager
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

abstract class Tool(
    protected val toolboxContext: ToolboxContext,
    private val renderingContext: IRenderTargetContext,
    private val inputManager: InputManager,
    private val camera2D: Camera2D,
    protected val levelContainer: LevelContainer,
    protected val selectionManager: SelectionManager,
    protected val actionManager: ActionManager,
) {


    abstract fun onClick(clickX: Float, clickY: Float)

    abstract fun onRelease(clickX: Float, clickY: Float)

    abstract fun renderTransients(resourceManager: IResourceManager)


    protected fun renderTransientSelectionMarker(resourceManager: IResourceManager) {

        val selType = selectionManager.primarySelection

        if (selType is SelectionManager.Type.Active) {
            val objectProgram = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
            objectProgram.uniform("uPerspective", camera2D.combined())
            objectProgram.uniform("uIgnoreTextures", 1)

            val mesh = resourceManager.getMesh(MolGLide.SELECTION_MARKER_MESH)
            val material = resourceManager.getMaterial(MolGLide.SELECTION_MARKER_MATERIAL)

            objectProgram.uniform("uLight", material.colour)
            val atomPos = selType.atom.getWorldPosition()
            objectProgram.uniform("uModel", Matrix4f().translate(atomPos.x, atomPos.y, OrganicEditorState.MARKER_PLANE).scale(MolGLide.FONT_SIZE * MolGLide.GLOBAL_SCALE * 0.70f))

            GL30.glBindVertexArray(mesh.vertexArray)
            GL11.glDrawElements(GL11.GL_TRIANGLE_FAN, mesh.vertices, GL11.GL_UNSIGNED_INT, 0)
            GL30.glBindVertexArray(0)

            objectProgram.uniform("uIgnoreTextures", 0)
        }
    }

    abstract fun update()


    //Mathematically quantising angles was wrong, this new implementation just takes a list of commonly used angles
    //And then snaps to the closest angle where the mouse is. This is simple than what was here before at v0.0.3
    protected fun closestPointToCircleCircumference(circleCentre: Vector2f, randomPoint: Vector2f, radius: Float) : Vector2f {
        val directionVec = randomPoint - circleCentre
        val angle = Vector2f(1.0f, 0.0f).angle(directionVec)

        val refinedAngle: Float = OrganicEditorState.COMMON_ANGLES.minBy { abs(Math.toRadians(it.toDouble()) - angle) }

        val refinedAngeRad = Math.toRadians(refinedAngle.toDouble())

        val x = circleCentre.x + radius * cos(refinedAngeRad)
        val y = circleCentre.y + radius * sin(refinedAngeRad)

        return Vector2f(x.toFloat(), y.toFloat())
    }

    protected fun mouseWorld(): Vector2f {
        val mousePos = renderingContext.getMousePos(inputManager)
        return camera2D.screenToWorld(mousePos)
    }
}