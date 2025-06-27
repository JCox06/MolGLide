package uk.co.jcox.chemvis.cvengine

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL33
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel
import uk.co.jcox.chemvis.cvengine.scenegraph.LineDrawerComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.ObjComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TextComponent
import uk.co.jcox.chemvis.cvengine.scenegraph.TransformComponent

class LevelRenderer (
    private val batcher: Batch2D,
    private val instancer: InstancedRenderer,
    private val resourceManager: IResourceManager
) {


//    To render a level:
//      1) Group entities by their "renderable" components (TextComponent, LineDrawerComponent, ObjComponent)
//      2) Send these entities off to the methods in this class that renders them
//      3) Each method renders objects differently depending on if they should be batched or instanced

    fun renderLevel(level: EntityLevel, camera2D: Camera2D, viewport: Vector2f) {

        val textEntities: MutableList<EntityLevel> = mutableListOf()
        val objectEntities: MutableList<EntityLevel> = mutableListOf()
        val lineEntities: MutableList<EntityLevel> = mutableListOf()
        traverseAndCollect(level, textEntities, objectEntities, lineEntities)



        //There are so few other objects that at the moment it makes no sense
        //to either batch them or instance them, so they are each rendered with their own GLDrawArrays function
        //However the meshes used are setup for instance rendering, so if more are needed, they can easily be changed
        renderObjects(objectEntities, camera2D)

        //Text objects contain dynamic geometry
        //The meshes are calculated from the Shaper2D each frame and then sent to OpenGL
        //Therefore texts are rendered through Batch Rendering
        renderTexts(textEntities, camera2D)


        //Since there might be a lot of lines, and since all lines have fixed geometry
        //Then they are rendered by the InstancedRenderer
        renderLines(level, lineEntities, camera2D, viewport)


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


    private fun renderTexts(entities: MutableList<EntityLevel>, camera2D: Camera2D) {

        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        val program = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        program.uniform("uPerspective", camera2D.combined())

        entities.forEach {
            renderText(it, program)
        }

        GL11.glDisable(GL11.GL_BLEND)
    }


    private fun renderText(textEntity: EntityLevel, program: ShaderProgram) {
        val textComponent = textEntity.getAbsoluteText()
        val transformComponent = textEntity.getComponent(TransformComponent::class)



        val fontID = textComponent?.bitmapFont
        val colourX = textComponent?.colourX
        val colourY = textComponent?.colourY
        val colourZ = textComponent?.colourZ
        val scale = textComponent?.scale

        if (fontID == null || colourX == null || colourY == null || colourZ == null || scale == null) {
            return
        }

        val fontData = resourceManager.getFont(fontID)

        resourceManager.useTexture(fontID, GL30.GL_TEXTURE0)
        program.uniform("uTexture0", 0)
        program.uniform("uLight", Vector3f(colourX, colourY, colourZ))

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

            val width = glyphMetrics.glyphWidth *  scale /2
            val height = glyphMetrics.glyphHeight * scale /2

            //Does not work!
//            val meshToDraw = Shaper2D.rectangle(renderX + width, renderY + height, width, height, (Text rendering works fine If you use this line instead of the one below) - However then the text is uncentred
            val meshToDraw = Shaper2D.rectangle(renderX, renderY, width, height,
                Vector2f(glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX, glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY),
                Vector2f(glyphMetrics.textureUnitAddX + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, 0.0f - glyphMetrics.textureUnitY),
                Vector2f(0.0f + glyphMetrics.textureUnitX, glyphMetrics.textureUnitAddY - glyphMetrics.textureUnitY)
            )

            batcher.addBatch(meshToDraw.pack(), meshToDraw.indices)
            renderX += width * 2
        }

        batcher.end()
    }


    private fun renderLines(level: EntityLevel, entities: MutableList<EntityLevel>, camera2D: Camera2D, viewport: Vector2f) {
        val lineProgram = resourceManager.useProgram(CVEngine.SHADER_INSTANCED_LINE)
        lineProgram.uniform("uPerspective", camera2D.combined())
        lineProgram.uniform("u_viewport", viewport)
        lineProgram.uniform("uModel", Matrix4f())

        val glMesh = resourceManager.getMesh(CVEngine.MESH_HOLDER_LINE)

        val instanceData = mutableListOf<Float>()

        for (line in entities) {
            if (!line.hasComponent(LineDrawerComponent::class)) {
                continue
            }

            val lineComp = line.getAbsoluteLineDrawer()
            val trans = line.getComponent(TransformComponent::class)

            if (lineComp == null) {
                return
            }

            val startEntity = level.findByID(lineComp.fromCompA)
            val endEntity = level.findByID(lineComp.toCompB)

            val startTrans = startEntity?.getAbsolutePosition()
            val endTrans = endEntity?.getAbsolutePosition()

            if (startTrans == null || endTrans == null) {
                continue
            }

            val width = lineComp.width
            val colourX = lineComp.colourX
            val colourY = lineComp.colourY
            val colourZ = lineComp.colourZ

            if (width == null || colourX == null || colourY == null || colourZ == null) {
                return
            }

            lineProgram.uniform("uLight", Vector3f(colourX, colourY, colourZ))

            val perInstanceData = listOf<Float>(startTrans.x + trans.x, startTrans.y + trans.y, startTrans.z + trans.z, endTrans.x + trans.x, endTrans.y + trans.y, endTrans.z + trans.z, width)

            instanceData.addAll(perInstanceData)
        }
        instancer.drawLines(glMesh, instanceData)
    }


    private fun renderObjects(objects: List<EntityLevel>, camera2D: Camera2D) {

        val objectProgram = resourceManager.useProgram(CVEngine.SHADER_SIMPLE_TEXTURE)
        objectProgram.uniform("uPerspective", camera2D.combined())
        objectProgram.uniform("uIgnoreTextures", 1)

        for (entity in objects) {

            val objComp = entity.getComponent(ObjComponent::class)
            val transComp = entity.getComponent(TransformComponent::class)

            val material = resourceManager.getMaterial(objComp.materialID)
            objectProgram.uniform("uLight", material.colour)
            objectProgram.uniform("uModel", Matrix4f().translate(entity.getAbsolutePosition()).scale(transComp.scale))

            val mesh = resourceManager.getMesh(objComp.modelGeomID)

//            GL15.glDrawElements(mode.openGlID, this.indices.size, GL11.GL_UNSIGNED_INT, 0)

            GL33.glBindVertexArray(mesh.vertexArray)
            GL11.glDrawElements(GL11.GL_TRIANGLE_FAN, mesh.vertices, GL11.GL_UNSIGNED_INT, 0)
            GL33.glBindVertexArray(0)
        }

        objectProgram.uniform("uIgnoreTextures", 0)
    }
}