package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class LevelRenderer (
    private val batcher: Batch2D,
    private val resourceManager: IResourceManager
) {

    private val lastFontColour: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)

    fun renderLevel(level: EntityLevel, camera2D: Camera2D) {
        val texts: MutableList<EntityLevel> = mutableListOf()
        traverseAndCollect(level, texts)

        //Render the text
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        val program = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        program.uniform("uPerspective", camera2D.combined())
        program.uniform("uModel", Matrix4f())

        batcher.begin(Batch2D.Mode.TRIANGLES)
        texts.forEach { textEntity ->
            renderText(textEntity, program)
        }
        batcher.end()
        GL11.glDisable(GL11.GL_BLEND)
    }

    private fun renderText(textEntity: EntityLevel, program: ShaderProgram) {
        if (!textEntity.hasComponent(TextComponent::class) || !textEntity.hasComponent(TransformComponent::class)) {
            return
        }

        val textComponent = textEntity.getComponent(TextComponent::class)
        val transformComponent = textEntity.getComponent(TransformComponent::class)

        val bitMapFontData = resourceManager.getFont(textComponent.bitmapFont)


        //Check if the colour currently being rendered is the same as last time
        if (textComponent.colourX != lastFontColour.x || textComponent.colourY != lastFontColour.y || textComponent.colourZ != lastFontColour.z) {
            //Then flush the buffer now
            val modeToRestore = batcher.end()
            //Restart the buffer
            lastFontColour.x = textComponent.colourX
            lastFontColour.y = textComponent.colourY
            lastFontColour.z = textComponent.colourZ
            program.uniform("uLight", lastFontColour)
            batcher.begin(modeToRestore)
        }


        resourceManager.useTexture(textComponent.bitmapFont, GL30.GL_TEXTURE0)
        program.uniform("uTexture0", 0)


        val totalPosition = getAbsPosition(textEntity)

        var renderX = totalPosition.x
        var renderY = totalPosition.y
        //(Only support 2D text currently. Text is stuck in the XY plane)

        for (c in textComponent.text) {
            var toDraw = c
            if (! bitMapFontData.glyphs.keys.contains(c)) {
                toDraw = bitMapFontData.glyphs.keys.first()
            }

            val glyphData = bitMapFontData.glyphs[toDraw]

            if (glyphData == null) {
                return
            }

            val mesh = Shaper2D.rectangle(renderX, renderY, glyphData.glyphWidth * textComponent.scale, glyphData.glyphHeight * textComponent.scale,
                Vector2f(
                    glyphData.textureUnitAddX + glyphData.textureUnitX,
                    glyphData.textureUnitAddY - glyphData.textureUnitY
                ),
                Vector2f(glyphData.textureUnitAddX + glyphData.textureUnitX, 0.0f - glyphData.textureUnitY),
                Vector2f(0.0f + glyphData.textureUnitX, 0.0f - glyphData.textureUnitY),
                Vector2f(0.0f + glyphData.textureUnitX, glyphData.textureUnitAddY - glyphData.textureUnitY)
            )

            batcher.addBatch(mesh)
            renderX += glyphData.glyphWidth * textComponent.scale
        }
    }

    private fun traverseAndCollect(entity: EntityLevel, texts: MutableList<EntityLevel>) {
        if (entity.hasComponent(TextComponent::class)) {
            texts.add(entity)
        }
        entity.getChildren().forEach { child ->
            traverseAndCollect(child, texts)
        }
    }

    private fun getAbsPosition(entityLevel: EntityLevel): Vector3f {

        val absPosition = Vector3f(0.0f, 0.0f, 0.0f)
        var currentParent = entityLevel

        while (true) {
            if (currentParent.hasComponent(TransformComponent::class)) {
                val transformComp = currentParent.getComponent(TransformComponent::class)
                absPosition.x += transformComp.x
                absPosition.y += transformComp.y
                absPosition.z += transformComp.z
            }
            if (currentParent.parent != null) {
                currentParent = currentParent.parent
            } else {
                return absPosition
            }
        }
    }
}