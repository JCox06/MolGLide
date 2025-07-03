package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.*
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.tinylog.Logger
import uk.co.jcox.chemvis.cvengine.BitmapFont.GlyphData
import java.awt.Color
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.sqrt

class ResourceManager : IResourceManager{
    private val shaderPrograms: MutableMap<String, ShaderProgram> = mutableMapOf()
    private val textures: MutableMap<String, Int> = mutableMapOf()
    private val fonts: MutableMap<String, BitmapFont> = mutableMapOf()
    private val meshes: MutableMap<String, GLMesh> = mutableMapOf()
    private val materials: MutableMap<String, Material> = mutableMapOf()
    private val renderTargets: MutableMap<String, RenderTarget> = mutableMapOf()

    init {
        STBImage.stbi_set_flip_vertically_on_load(true)
    }

    //Shader Management

    override fun loadShadersFromDisc(id: String, vertSrc: File, fragSrc: File, geomSrc: File?) {
        Logger.info {"Loading shader program $id"}
        checkFile(vertSrc)
        checkFile(fragSrc)

        try {
            val vertShader = Files.readString(vertSrc.toPath())
            val fragShader = Files.readString(fragSrc.toPath())

            if (geomSrc == null) {
                val shaderProgram = loadShadersToOpenGL(vertShader, fragShader, "")
                shaderPrograms[id] = ShaderProgram(shaderProgram)
            } else {
                val geomShader = Files.readString(geomSrc.toPath())
                val shaderProgram = loadShadersToOpenGL(vertShader, fragShader, geomShader)
                shaderPrograms[id] = ShaderProgram(shaderProgram)
            }

        } catch (e: IOException) {
            Logger.error ("Error when reading shader data from file {}", e)
        }
    }

    override fun useProgram(programID: String) : ShaderProgram {
        val program = shaderPrograms[programID]

        if (program != null) {
            program.bind()
            return program
        }
        throw RuntimeException("No such program found with ID $programID")
    }

    override fun destroyProgram(programID: String) {
        Logger.info { "Destroying program $programID" }
        val program = shaderPrograms[programID]
        if (program != null) {
            program.close()
            shaderPrograms.remove(programID)
        }
    }

    override fun manageMesh(id: String, mesh: Mesh, instancedRenderer: InstancedRenderer) {
        Logger.info { "Managing external mesh and loading into OpenGL $id" }

        //Meshes that are sent to the ResourceManager need to be first loaded into OpenGL
        //Once loaded they can be retrieved as a VAO
        meshes[id] = loadMeshIntoOpenGL(instancedRenderer, mesh)
    }

    override fun destroyMesh(id: String) {
        Logger.info { "Destroying mesh $id" }

        val mesh = meshes[id]

        mesh?.let { buff ->
            buff.buffers.forEach { GL15.glDeleteBuffers(it) }

            GL30.glDeleteVertexArrays(buff.vertexArray)
        }
        meshes.remove(id)
    }

    override fun getMesh(id: String): GLMesh {
        val mesh = meshes[id]
        if (mesh == null) {
            throw NullPointerException("No such model exists $id")
        }

        return mesh
    }


    //Texture Management

    override fun loadTextureFromDisc(id: String, texture: File) {
        Logger.info { "Loading texture $id" }

        checkFile(texture)

        MemoryStack.stackPush().use { memoryStack ->
            val widthB = memoryStack.mallocInt(1)
            val heightB = memoryStack.mallocInt(1)
            val nrChannelsB = memoryStack.mallocInt(1)

            val dataB = STBImage.stbi_load(texture.path, widthB, heightB, nrChannelsB, 4)

            if (dataB == null) {
                throw RuntimeException("Image failed to load")
            }

            val textureLoaded = loadTextureToOpenGL(dataB, widthB.get(), heightB.get())
            textures[id] = textureLoaded
        }
    }

    override fun destroyTexture(id: String) {
        Logger.info { "Destroying texture $id" }
        val textureToDelete = textures[id]
        if (textureToDelete != null) {
            GL11.glDeleteTextures(textureToDelete)
        }
        textures.remove(id)
    }

    override fun destroyFont(id: String) {
        Logger.info { "Destroying font $id" }
        destroyTexture(id)
        fonts.remove(id)
    }

    override fun destroy() {
        Logger.info { "Shutting down all resources and ResourceManager" }

        val keysToRemove = mutableListOf<String>()

        shaderPrograms.keys.forEach { keysToRemove.add(it) }
        keysToRemove.forEach { destroyProgram(it) }
        keysToRemove.clear()

        textures.keys.forEach {keysToRemove.add(it)}
        keysToRemove.forEach { destroyTexture(it) }
        keysToRemove.clear()

        fonts.keys.forEach {keysToRemove.add(it)}
        keysToRemove.forEach { destroyFont(it) }
        keysToRemove.clear()

        meshes.keys.forEach { keysToRemove.add(it) }
        keysToRemove.forEach { destroyMesh(it) }
        keysToRemove.clear()


        renderTargets.keys.forEach { keysToRemove.add(it)}
        keysToRemove.forEach { destroyRenderTarget(it) }
        keysToRemove.clear()
    }


    override fun loadFontFromDisc(id: String, font: File, glyphs: String, size: Int) {
        Logger.info { "Loading font $id" }
        checkFile(font)


        //Get the font metrics so we can get glyph size, etc
        val font = Font(font.path, Font.PLAIN, size)
        val fontMetrics: FontMetrics = getFontMetrics(font)


        //Each char has a different width, but same height
        //To avoid a runtime error, assume they are all the largest size
        var maxCharWidth = 0 //need to calculate
        val charHeight = (fontMetrics.height) //They have the same height

        for (c in glyphs.toCharArray()) {
            maxCharWidth = max(maxCharWidth, fontMetrics.charWidth(c))
        }


        //Now create texture dimensions
        val textureUnit = sqrt(glyphs.length.toDouble()).toInt() + 1
        val proposedWidth = textureUnit * maxCharWidth

        val proposedHeight = textureUnit * charHeight
        val squareTextureSize = max(proposedHeight, proposedWidth)


        //Create the actual image
        val fontAtlas: MutableMap<Char, GlyphData> = HashMap()
        val atlasImage = BufferedImage(squareTextureSize, squareTextureSize, BufferedImage.TYPE_INT_ARGB)
        val g2d = atlasImage.createGraphics()
        g2d.font = font
        g2d.color = Color.WHITE

        var glyphXPlacement = 0
        var glyphYPlacement = charHeight

        for (c in glyphs.toCharArray()) {
            if (glyphXPlacement + maxCharWidth >= squareTextureSize) {
                glyphXPlacement = 0
                glyphYPlacement += charHeight
            }

            g2d.drawString(c.toString(), glyphXPlacement, glyphYPlacement)

            val glyphData = GlyphData(
                fontMetrics.charWidth(c).toFloat(),
                charHeight.toFloat(),
                glyphXPlacement.toFloat() / squareTextureSize,
                (glyphYPlacement + (0.30f * size)) / squareTextureSize,
                fontMetrics.charWidth(c).toFloat() / squareTextureSize,
                charHeight.toFloat() / squareTextureSize
            )


            fontAtlas.put(c, glyphData)

            glyphXPlacement += maxCharWidth
        }

        saveBitmapFont(atlasImage)


        //Now load this as an image into OpenGL
        val textureData: ByteBuffer = convertImageData(atlasImage)
        val glTextureObject: Int = loadTextureToOpenGL(textureData, squareTextureSize, squareTextureSize, GL11.GL_NEAREST, GL11.GL_NEAREST, false)

        textures[id] = glTextureObject

        val bitmapFont = BitmapFont(size, id, fontAtlas)

        fonts[id] = bitmapFont

        g2d.dispose()

    }


    private fun loadShadersToOpenGL(vertSrc: String, fragSrc: String, geomSrc: String) : Int {
        val vertexShader = GL30.glCreateShader(GL30.GL_VERTEX_SHADER)
        GL30.glShaderSource(vertexShader, vertSrc)
        GL30.glCompileShader(vertexShader)

        if (! checkShaderCompilation(vertexShader)) {
            Logger.error{getShaderInfoLog(vertexShader)}
        }

        val fragmentShader = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER)
        GL30.glShaderSource(fragmentShader, fragSrc)
        GL30.glCompileShader(fragmentShader)

        if (! checkShaderCompilation(fragmentShader)) {
            Logger.error{getShaderInfoLog(fragmentShader)}
        }
        val shaderProgram = GL30.glCreateProgram()

        GL30.glAttachShader(shaderProgram, vertexShader)
        GL30.glAttachShader(shaderProgram, fragmentShader)

        val geomShader = GL30.glCreateShader(GL33.GL_GEOMETRY_SHADER)

        if (! geomSrc.isEmpty()) {
            GL33.glShaderSource(geomShader, geomSrc)
            GL33.glCompileShader(geomShader)

            if (! checkShaderCompilation(geomShader)) {
                Logger.error { getShaderInfoLog(geomShader) }
            }

            GL30.glAttachShader(shaderProgram, geomShader)

        }


        GL30.glLinkProgram(shaderProgram)

        if (! checkProgramLink(shaderProgram)) {
            Logger.error{(getProgramInfoLog(shaderProgram))}
        }

        validateProgram(shaderProgram)

        GL30.glDeleteShader(geomShader)
        GL30.glDeleteShader(vertexShader)
        GL30.glDeleteShader(fragmentShader)

        return shaderProgram
    }

    private fun checkShaderCompilation(shadType: Int) : Boolean {
        return GL30.glGetShaderi(shadType, GL30.GL_COMPILE_STATUS) == 1
    }


    private fun getProgramInfoLog(program: Int): String {
        return GL30.glGetProgramInfoLog(program)
    }

    private fun getShaderInfoLog(shadType: Int) : String {
        return GL30.glGetShaderInfoLog(shadType)
    }

    private fun checkProgramLink(programID: Int) : Boolean {
        return GL30.glGetProgrami(programID, GL30.GL_LINK_STATUS) == 1
    }

    private fun validateProgram(programID: Int) {
        GL30.glValidateProgram(programID)
        if (GL30.glGetProgrami(programID, GL30.GL_VALIDATE_STATUS) == 0) {
            Logger.warn{ "Shader program validation failed: ${getProgramInfoLog(programID)}" }
        }
    }

    private fun loadTextureToOpenGL(data: ByteBuffer?, width: Int, height: Int, minFilter: Int = GL11.GL_LINEAR_MIPMAP_LINEAR, magFilter: Int = GL11.GL_LINEAR, stbLoaded: Boolean = true) : Int {
        val glTexture = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTexture)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

        if (stbLoaded && data != null) {
            STBImage.stbi_image_free(data)
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        return glTexture
    }

    private fun convertImageData(image: BufferedImage): ByteBuffer {
        val pixelDataInt = image.getRGB(0, 0, image.width, image.height, null, 0, image.width)
        val buffer = ByteBuffer.allocateDirect(image.width * image.height * 4)

        for (y in image.height - 1 downTo 0) {
            for (x in 0..<image.width) {
                val pixel = pixelDataInt[image.width * y + x]
                buffer.put(((pixel shr 16) and 0xFF).toByte())
                buffer.put(((pixel shr 8) and 0xFF).toByte())
                buffer.put((pixel and 0xFF).toByte())
                buffer.put(((pixel shr 24) and 0xFF).toByte())
            }
        }

        buffer.flip()

        return buffer
    }

    private fun saveBitmapFont(bufferedImage: BufferedImage) {
        val outputFile = File("data/integrated/temp/font-generated.png")
        Logger.debug { "Saving generated font bitmap to $outputFile" }
        try {
            ImageIO.write(bufferedImage, "png", outputFile)
        } catch (e: IOException) {
            Logger.error("Error when saving image {}", e)
        }
    }

    private fun getFontMetrics(font: Font?): FontMetrics {
        val tempImage = BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR)
        val g2d: Graphics = tempImage.createGraphics()
        g2d.font = font
        g2d.color = Color.WHITE
        val metrics = g2d.fontMetrics
        g2d.dispose()
        return metrics
    }

    private fun checkFile(file: File) {
        if (!file.isFile) {
            Logger.error { "A shader program specified a shader file that did not exist" }
            throw RuntimeException("Can't find: ${file.absoluteFile}")
        }
    }


    override fun getFont(id: String): BitmapFont {
        val font = fonts[id]

        if (font != null) {
            return font
        }

        throw RuntimeException("Font is null")
    }

    override fun useTexture(id: String, textureUnit: Int): Boolean {
        GL15.glActiveTexture(textureUnit)
        val textureToBind = this.textures[id]
        if (textureToBind != null) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureToBind)
            return true
        }
        return false
    }


    override fun manageMaterial(id: String, material: Material) {
        Logger.info { "Managing external material $id" }
        materials[id] = material
    }

    override fun destroyMaterial(id: String) {
        this.materials.remove(id)
    }

    override fun getMaterial(id: String): Material {
        val material = materials[id]

        if (material == null) {
            Logger.info { "Material requested was null: $id" }
            throw NullPointerException("Material not found")
        }

        return material
    }


    private fun loadMeshIntoOpenGL(instancedRenderer: InstancedRenderer, mesh: Mesh) : GLMesh {

        //Currently only supports meshes that should be lines

        val vertexSet = mesh.pack()
        val vertexArray = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(vertexArray)


        val vertexBuffer = GL15.glGenBuffers()
        println(vertexBuffer)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vertexBuffer)
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexSet.toFloatArray(), GL15.GL_STATIC_DRAW)

        val elementBuffer = GL15.glGenBuffers()
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer)
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, mesh.indices.toIntArray(), GL15.GL_STATIC_DRAW)

        //Map OpenGL attribute Objects
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, CVEngine.VERTEX_SIZE_BYTES, 0)
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, CVEngine.VERTEX_SIZE_BYTES, 3L * Float.SIZE_BYTES)
        GL20.glEnableVertexAttribArray(0)
        GL20.glEnableVertexAttribArray(1)

        //Instanced OpenGL attribute objects
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, instancedRenderer.lineInstancedBuffer)
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 7 * Float.SIZE_BYTES, 0L)
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 7 * Float.SIZE_BYTES, 3L * Float.SIZE_BYTES)
        GL20.glVertexAttribPointer(4, 1, GL11.GL_FLOAT, false, 7 * Float.SIZE_BYTES, 6L * Float.SIZE_BYTES)
        GL20.glEnableVertexAttribArray(2)
        GL20.glEnableVertexAttribArray(3)
        GL20.glEnableVertexAttribArray(4)
        GL33.glVertexAttribDivisor(2, 1)
        GL33.glVertexAttribDivisor(3, 1)
        GL33.glVertexAttribDivisor(4, 1)

        val bufferList = listOf(vertexBuffer, elementBuffer)

        val gpuSideMesh = GLMesh(vertexArray, bufferList, mesh.indices.size)

        GL30.glBindVertexArray(0)

        return gpuSideMesh
    }


    override fun createMultiSampledRenderTarget(id: String, samples: Int) {

        if (samples <= 1) {
            throw IllegalArgumentException("Cannot use 1 sample for a multi sample frame buffer")
        }

        //Create the sampled frame buffer:
        Logger.info { "Creating a multi sampled render target for $id with $samples samples" }
        val initialWidth = 1
        val initialHeight = 1

        val sampledFrameBuffer = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, sampledFrameBuffer)

        val sampledColour = GL11.glGenTextures()
        GL11.glBindTexture(GL32.GL_TEXTURE_2D_MULTISAMPLE, sampledColour)
        GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, samples, GL11.GL_RGBA, initialWidth, initialHeight, true)
        GL32.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL32.GL_TEXTURE_2D_MULTISAMPLE, sampledColour, 0)

        val sampledDepth = GL30.glGenRenderbuffers()
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, sampledDepth)
        GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, samples, GL30.GL_DEPTH24_STENCIL8, initialWidth, initialHeight)
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, sampledDepth)

        logFrameBufferStatus(sampledFrameBuffer, id, "SAMPLED")


        //Create the resolved frame buffer:
        val resolvedFrameBuffer = GL30.glGenFramebuffers()
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, resolvedFrameBuffer)

        val resolvedColour = loadTextureToOpenGL(null, initialWidth, initialHeight, GL11.GL_NEAREST, GL11.GL_NEAREST, false)
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, resolvedColour, 0)

        logFrameBufferStatus(sampledFrameBuffer, id, "RESOLVED")

        val target: RenderTarget = ResolvedRenderTarget(sampledFrameBuffer, sampledColour, sampledDepth, initialWidth.toFloat(), initialHeight.toFloat(), resolvedFrameBuffer, resolvedColour, samples)
        renderTargets[id] = target
    }


    override fun resizeRenderTarget(id: String, proposedWidth: Float, proposedHeight: Float) {
        val target = renderTargets[id]

        if (target == null) {
            return
        }

        val width = max(proposedWidth, 1.0f)
        val height = max(proposedHeight, 1.0f)

        if (width == target.width && height == target.height) {
            return
        }

        if (target !is ResolvedRenderTarget) {
            return
        }

        //Resize Sampled Frame Attachments:
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.frameBuffer)

        val sampledTexture = target.colourAttachmentTexture
        GL11.glBindTexture(GL33.GL_TEXTURE_2D_MULTISAMPLE, sampledTexture)
        GL32.glTexImage2DMultisample(GL32.GL_TEXTURE_2D_MULTISAMPLE, target.samples, GL11.GL_RGBA, width.toInt(), height.toInt(), true)

        val sampledDepth = target.depthAttachmentRenderBuffer
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, sampledDepth)
        GL30.glRenderbufferStorageMultisample(GL30.GL_RENDERBUFFER, target.samples, GL30.GL_DEPTH24_STENCIL8, width.toInt(), height.toInt())

        //Resize the resolved texture
        val reserve: ByteBuffer? = null
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, target.resolvedBuffer)
        GL11.glBindTexture(GL33.GL_TEXTURE_2D, target.resolvedColour)
        GL30.glTexImage2D(GL30.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.toInt(), height.toInt(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, reserve)

        target.width = width
        target.height = height
    }

    private fun logFrameBufferStatus(frameBuffer: Int, id: String, type: String) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, frameBuffer)
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            Logger.error {"Frame Buffer setup failed for render target $id of type $type"}
        } else {
            Logger.info { "Frame buffer setup successful for render target $id of type $type" }
        }
    }

    override fun destroyRenderTarget(id: String) {

        Logger.info { "Destroying render target $id" }

        val target = renderTargets[id]

        if (target == null) {
            return
        }

        GL30.glDeleteFramebuffers(target.frameBuffer)
        GL30.glDeleteTextures(target.colourAttachmentTexture)
        GL30.glDeleteRenderbuffers(target.depthAttachmentRenderBuffer)

        renderTargets.remove(id)
    }

    override fun getRenderTarget(id: String): RenderTarget {
        val target = renderTargets[id]

        if (target == null) {
            throw NullPointerException("Render target could not be found")
        }

        return target
    }

}