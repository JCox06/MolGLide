package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class LevelRenderer (
    private val batcher: Batch2D,
    private val resourceManager: IResourceManager
) {

    private val lastFontColour: Vector3f = Vector3f(1.0f, 1.0f, 1.0f)

    fun renderLevel(level: EntityLevel, camera2D: Camera2D) {
        val texts: MutableList<EntityLevel> = mutableListOf()
        val objects: MutableList<EntityLevel> = mutableListOf()
        val lines: MutableList<EntityLevel> = mutableListOf()
        traverseAndCollect(level, texts, objects, lines)

        var program = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        program.uniform("uPerspective", camera2D.combined())
        program.uniform("uModel", Matrix4f())

        batcher.begin(Batch2D.Mode.FAN)
        //Render other objects
        objects.forEach { objectEntity ->
            renderObject(objectEntity)
        }
        batcher.end()


        program = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_COLOUR)
        program.uniform("uPerspective", camera2D.combined())
        program.uniform("uModel", Matrix4f())

        batcher.begin(Batch2D.Mode.LINE)
        //Render lines (aka bonds)
        lines.forEach { lineEntity ->
            renderLine(lineEntity)
        }

        batcher.end()


        //Render the text (do this last as text is transparent and this is main thing)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        program = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        program.uniform("uPerspective", camera2D.combined())
        program.uniform("uModel", Matrix4f())

        batcher.begin(Batch2D.Mode.TRIANGLES)
        texts.forEach { textEntity ->
            renderText(textEntity, program)
        }
        batcher.end()
        GL11.glDisable(GL11.GL_BLEND)

    }


    private fun renderLine(lineEntity: EntityLevel) {
        if (!lineEntity.hasComponent(LineDrawerComponent::class) || !lineEntity.hasComponent(TransformComponent::class)) {
            return
        }

        val lineComponent = lineEntity.getComponent(LineDrawerComponent::class)
        val transformComp = lineEntity.getComponent(TransformComponent::class)

        if (! transformComp.visible) {
            return;
        }

        val absPos = lineEntity.getAbsolutePosition()

        val mesh = Shaper2D.line(absPos.x, absPos.y, absPos.z, lineComponent.lineTo.x, lineComponent.lineTo.y, lineComponent.lineTo.z)

        batcher.addBatch(mesh.pack(), mesh.indices)
    }



    private fun renderObject(objectEntity: EntityLevel) {
        if (! objectEntity.hasComponent(ObjComponent::class) || !objectEntity.hasComponent(TransformComponent::class)) {
            return
        }

        val objectComponent = objectEntity.getComponent(ObjComponent::class)
        val transformComponent = objectEntity.getComponent(TransformComponent::class)


        if (! transformComponent.visible) {
            return
        }

        val mesh = resourceManager.getMesh(objectComponent.modelGeomID)

        val absPos = getAbsPosition(objectEntity)

        val transform = Matrix4f()
            .translate(absPos.x, absPos.y, absPos.z)
            .scale(transformComponent.scale)

        val newMesh = mesh.apply(transform)
        batcher.addBatch(newMesh.pack(), newMesh.indices)
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

            batcher.addBatch(mesh.pack(), mesh.indices)
            renderX += glyphData.glyphWidth * textComponent.scale
        }
    }

    private fun traverseAndCollect(entity: EntityLevel, texts: MutableList<EntityLevel>, objects: MutableList<EntityLevel>, lines: MutableList<EntityLevel>) {
        if (entity.hasComponent(TextComponent::class)) {
            texts.add(entity)
        }

        if (entity.hasComponent(ObjComponent::class)) {
            objects.add(entity)
        }

        if (entity.hasComponent(LineDrawerComponent::class)) {
            lines.add(entity)
        }

        entity.getChildren().forEach { child ->
            traverseAndCollect(child, texts, objects, lines)
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