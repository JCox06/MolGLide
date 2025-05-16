package uk.co.jcox.chemvis.cvengine

import org.lwjgl.opengl.GL30

class ShaderProgramManager {

    private val shaderPrograms: MutableMap<String, ShaderProgram> = mutableMapOf()

    fun loadShader(shaderID: String, vertSrc: String, fragSrc: String) {
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



        shaderPrograms[shaderID] = ShaderProgram(shaderProgram)
    }

    private fun checkShaderCompilation(shadType: Int) : Boolean {
        return GL30.glGetShaderi(shadType, GL30.GL_COMPILE_STATUS) == 1
    }


    fun getProgramInfoLog(program: Int): String {
        return GL30.glGetProgramInfoLog(program)
    }

    private fun getShaderInfoLog(shadType: Int) : String {
        return GL30.glGetShaderInfoLog(shadType)
    }

    private fun checkProgramLink(programID: Int) : Boolean {
        return GL30.glGetProgrami(programID, GL30.GL_LINK_STATUS) == 1
    }

    fun validateProgram(programID: Int) {
        GL30.glValidateProgram(programID)
        if (GL30.glGetProgrami(programID, GL30.GL_VALIDATE_STATUS) == 0) {
            println("Error")
        }
    }

    fun useProgram(programID: String) : ShaderProgram {
        val program = shaderPrograms[programID]

        if (program != null) {
            program.bind()
            return program
        }

        throw Exception("No such program")
    }

    fun destroyProgram(programID: String) {
        val program = shaderPrograms[programID]
        if (program != null) {
            program.close()
            shaderPrograms.remove(programID)
        }
    }

    fun close() {
        shaderPrograms.values.forEach { program ->
            program.close()
        }
    }

    companion object {
        val SIMPLE_TEXTURE = "data/integrated/simple_texture"
        val LINE: String = "data/integrated/line"
    }
}