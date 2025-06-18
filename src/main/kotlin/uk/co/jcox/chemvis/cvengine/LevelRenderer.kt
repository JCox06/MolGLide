package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.application.MolGLide
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class LevelRenderer (
    private val batcher: Batch2D,
    private val resourceManager: IResourceManager
) {

    //MUCH BETTER CLASS NOW - ALTHOUGH IT WILL NEED TO BE EDITED TO SUPPORT MULTIPLE OBJECT TYPES
    //I think its probably best to edit this when as needed


//    To render a level:
//      1) Group entities by their "renderable" components (TextComponent, LineDrawerComponent, ObjComponent)
//      2) Send these entities off to the methods in this class that renders them
//      3) Each method renders objects differently depending on if they should be batched or instanced

    fun renderLevel(level: EntityLevel, camera2D: Camera2D, viewport: Vector2f) {

        val textEntities: MutableList<EntityLevel> = mutableListOf()
        val objectEntities: MutableList<EntityLevel> = mutableListOf()
        val lineEntities: MutableList<EntityLevel> = mutableListOf()
        traverseAndCollect(level, textEntities, objectEntities, lineEntities)


        renderTexts(textEntities, batcher, resourceManager, camera2D)
        renderLines(lineEntities, batcher, resourceManager, camera2D, viewport)
    }


    private fun traverseAndCollect(level: EntityLevel, texts: MutableList<EntityLevel>, objects: MutableList<EntityLevel>, lines: MutableList<EntityLevel>) {
        level.traverseFunc {

            if (!it.hasComponent(TransformComponent::class)) {
                return@traverseFunc
            }

            val transform = it.getComponent(TransformComponent::class)
            if (!transform.visible) {
                return@traverseFunc
            }

            if (it.hasComponent(TextComponent::class)) {
               texts.add(it)
            }

            if(it.hasComponent(ObjComponent::class)) {
                objects.add(it)
            }

            if (it.hasComponent(LineDrawerComponent::class)) {
                lines.add(it)
            }
        }
    }


    private fun renderTexts(entities: MutableList<EntityLevel>, batch2D: Batch2D, resourceManager: IResourceManager, camera2D: Camera2D) {

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val program = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        program.uniform("uPerspective", camera2D.combined())

        entities.forEach {
            renderText(it, batch2D, resourceManager, program)
        }

        GL11.glDisable(GL11.GL_BLEND)
    }


    private fun renderText(textEntity: EntityLevel, batcher: Batch2D, resourceManager: IResourceManager, program: ShaderProgram) {
        val textComponent = textEntity.getComponent(TextComponent::class)
        val transformComponent = textEntity.getComponent(TransformComponent::class)

        val fontID = textComponent.bitmapFont
        val fontData = resourceManager.getFont(fontID)

        resourceManager.useTexture(fontID, GL30.GL_TEXTURE0)
        program.uniform("uTexture0", 0)
        program.uniform("uLight", Vector3f(textComponent.colourX, textComponent.colourY, textComponent.colourZ))

        val localCentre = textEntity.getAbsoluteTranslation()
        localCentre.add(Vector3f())
        program.uniform("uModel", Matrix4f().translate(localCentre).scale(transformComponent.scale))
        val position = Vector3f(transformComponent.x, transformComponent.y, transformComponent.z)

        //Currently only supports text in the XY plane
        var renderX = position.x
        var renderY = position.y

        batcher.begin(Batch2D.Mode.TRIANGLES)

        for (c in textComponent.text) {
            var character = c

            if (! fontData.glyphs.keys.contains(c)) {
                character = fontData.glyphs.keys.first()
            }

            val glyphMetrics = fontData.glyphs[character]

            if (glyphMetrics == null) {
                return
            }

            val width = glyphMetrics.glyphWidth * textComponent.scale /2
            val height = glyphMetrics.glyphHeight * textComponent.scale /2

            //Dont understand how I fixed it but it works
            val meshToDraw = Shaper2D.rectangle(renderX + width, renderY + height, width, height,
                Vector2f(glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX, glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY),
                Vector2f(glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY)
            )

            batcher.addBatch(meshToDraw.pack(), meshToDraw.indices)
            renderX += width * 2f
        }

        batcher.end()
    }


    private fun renderLines(entities: MutableList<EntityLevel>, batch2D: Batch2D, resourceManager: IResourceManager, camera2D: Camera2D, viewport: Vector2f) {
        val lineProgram = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_LINE)
        lineProgram.uniform("uPerspective", camera2D.combined())
        lineProgram.uniform("u_viewport", viewport)
        lineProgram.uniform("uModel", Matrix4f())

        val glMesh = resourceManager.getMesh(CVEngine.MESH_UNIT_LINE)

        GL30.glBindVertexArray(glMesh.vertexArray)

    }

}