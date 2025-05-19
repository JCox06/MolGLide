package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL30
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
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
    private val meshes: MutableMap<String, Mesh> = mutableMapOf()

    init {
        STBImage.stbi_set_flip_vertically_on_load(true)
    }

    //Shader Management

    override fun loadShadersFromDisc(id: String, vertSrc: File, fragSrc: File) {
        checkFile(vertSrc)
        checkFile(fragSrc)

        try {
            val vertShader = Files.readString(vertSrc.toPath())
            val fragShader = Files.readString(fragSrc.toPath())

            val shaderProgram = loadShadersToOpenGL(vertShader, fragShader)
            shaderPrograms[id] = ShaderProgram(shaderProgram)
        } catch (e: IOException) {
            println("Error when loading shader from disc: ${e.message}")
        }
    }

    override fun useProgram(programID: String) : ShaderProgram {
        val program = shaderPrograms[programID]

        if (program != null) {
            program.bind()
            return program
        }
        throw RuntimeException("No such program found with ID ${programID}")
    }

    override fun destroyProgram(programID: String) {
        val program = shaderPrograms[programID]
        if (program != null) {
            program.close()
            shaderPrograms.remove(programID)
        }
    }

    override fun manageMesh(id: String, mesh: Mesh) {
        meshes[id] = mesh
    }

    override fun destroyMesh(id: String) {
        meshes.remove(id)
    }

    override fun getMesh(id: String): Mesh {
        val mesh = meshes[id]
        if (mesh == null) {
            throw NullPointerException("No such model exists ${id}")
        }

        return mesh
    }


    //Texture Management

    override fun loadTextureFromDisc(id: String, texture: File) {
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
        val textureToDelete = textures[id]
        if (textureToDelete != null) {
            GL11.glDeleteTextures(textureToDelete)
        }
        textures.remove(id)
    }

    override fun destroyFont(id: String) {
        destroyTexture(id)
        fonts.remove(id)
    }

    override fun destroy() {
        shaderPrograms.keys.forEach { destroyProgram(it) }
        textures.keys.forEach {destroyTexture(it)}
        fonts.keys.forEach {destroyFont(it)}
        meshes.keys.forEach { destroyMesh(it) } //Not really required (no external resources associated)
    }


    override fun loadFontFromDisc(id: String, font: File, glyphs: String, size: Int) {
        checkFile(font)


        //Get the font metrics so we can get glyph size, etc
        val font = Font(font.getPath(), Font.PLAIN, size)
        val fontMetrics: FontMetrics = getFontMetrics(font)


        //Each char has a different width, but same height
        //To avoid a runtime error, assume they are all the largest size
        var maxCharWidth = 0 //need to calculate
        val charHeight = (fontMetrics.getHeight()) //They have the same height

        for (c in glyphs.toCharArray()) {
            maxCharWidth = max(maxCharWidth, fontMetrics.charWidth(c))
        }


        //Now create texture dimensions
        val textureUnit = sqrt(glyphs.length.toDouble()).toInt() + 1
        val proposedWidth = textureUnit * maxCharWidth

        val charHeightPadding: Int = charHeight + size

        val proposedHeight = textureUnit * charHeight
        val squareTextureSize = max(proposedHeight, proposedWidth)


        //Create the actual image
        val fontAtlas: MutableMap<Char, GlyphData> = HashMap<Char, GlyphData>()
        val atlasImage = BufferedImage(squareTextureSize, squareTextureSize, BufferedImage.TYPE_INT_ARGB)
        val g2d = atlasImage.createGraphics()
        g2d.setFont(font)
        g2d.setColor(Color.WHITE)

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
                (glyphYPlacement + (0.30f * size)) as Float / squareTextureSize,
                fontMetrics.charWidth(c).toFloat() / squareTextureSize,
                charHeight.toFloat() / squareTextureSize
            )


            fontAtlas.put(c, glyphData)

            glyphXPlacement += maxCharWidth
        }

        saveBitmapFont(atlasImage)


        //Now load this as an image into OpenGL
        val textureData: ByteBuffer = convertImageData(atlasImage)
        val glTextureObject: Int = loadTextureToOpenGL(textureData, squareTextureSize, squareTextureSize, GL11.GL_LINEAR, GL11.GL_LINEAR, false)

        textures[id] = glTextureObject

        val bitmapFont = BitmapFont(size, id, fontAtlas)

        fonts[id] = bitmapFont

        g2d.dispose()

    }


    private fun loadShadersToOpenGL(vertSrc: String, fragSrc: String) : Int {
        val vertexShader = GL30.glCreateShader(GL30.GL_VERTEX_SHADER)
        GL30.glShaderSource(vertexShader, vertSrc)
        GL30.glCompileShader(vertexShader)

        if (! checkShaderCompilation(vertexShader)) {
            println(getShaderInfoLog(vertexShader))
        }

        val fragmentShader = GL30.glCreateShader(GL30.GL_FRAGMENT_SHADER)
        GL30.glShaderSource(fragmentShader, fragSrc)
        GL30.glCompileShader(fragmentShader)

        if (! checkShaderCompilation(fragmentShader)) {
            println(getShaderInfoLog(fragmentShader))
        }


        val shaderProgram = GL30.glCreateProgram()

        GL30.glAttachShader(shaderProgram, vertexShader)
        GL30.glAttachShader(shaderProgram, fragmentShader)
        GL30.glLinkProgram(shaderProgram)

        if (! checkProgramLink(shaderProgram)) {
            println(getProgramInfoLog(shaderProgram))
        }

        validateProgram(shaderProgram)

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
            println("Error")
        }
    }

    private fun loadTextureToOpenGL(data: ByteBuffer, width: Int, height: Int, minFilter: Int = GL11.GL_LINEAR_MIPMAP_LINEAR, magFilter: Int = GL11.GL_LINEAR, stbLoaded: Boolean = true) : Int {
        val glTexture = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, glTexture)
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, data)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, minFilter)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, magFilter)
        GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D)

        if (stbLoaded) {
            STBImage.stbi_image_free(data)
        }

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)
        return glTexture
    }

    private fun convertImageData(image: BufferedImage): ByteBuffer {
        val pixelDataInt = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth())
        val buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4)

        for (y in image.getHeight() - 1 downTo 0) {
            for (x in 0..<image.getWidth()) {
                val pixel = pixelDataInt[image.getWidth() * y + x]
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
        try {
            ImageIO.write(bufferedImage, "png", outputFile)
        } catch (e: IOException) {
            throw java.lang.RuntimeException()
        }
    }

    private fun getFontMetrics(font: Font?): FontMetrics {
        val tempImage = BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR)
        val g2d: Graphics = tempImage.createGraphics()
        g2d.setFont(font)
        g2d.setColor(Color.WHITE)
        val metrics = g2d.getFontMetrics()
        g2d.dispose()
        return metrics
    }

    private fun checkFile(file: File) {
        if (!file.isFile) {
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
}