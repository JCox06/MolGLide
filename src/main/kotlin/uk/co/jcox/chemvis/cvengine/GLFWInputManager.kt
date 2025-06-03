package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f
import org.lwjgl.glfw.GLFW

class GLFWInputManager (
    val windowHandle: Long
) : InputManager() {

    private var inputBlock = false


    init {
        GLFW.glfwSetKeyCallback(this.windowHandle) { win, key, scancode, action, mods ->
            subscribers.forEach { subscriber ->
                if (action == GLFW.GLFW_PRESS && !inputBlock) {
                    subscriber.clickEvent(this, RawInput.fromGLFW(key))
                }
                if (action == GLFW.GLFW_RELEASE && !inputBlock) {
                    subscriber.clickReleaseEvent(this, RawInput.fromGLFW(key))
                }
            }
        }

        GLFW.glfwSetMouseButtonCallback(this.windowHandle) {win, button, action, mods ->
            subscribers.forEach { subscriber ->
                if (action == GLFW.GLFW_PRESS && !inputBlock) {
                    subscriber.clickEvent(this, RawInput.fromGLFW(button))
                }
                if (action == GLFW.GLFW_RELEASE && !inputBlock) {
                    subscriber.clickReleaseEvent(this, RawInput.fromGLFW(button))
                }
            }
        }

        GLFW.glfwSetCursorPosCallback(this.windowHandle) {win, xpos, ypos ->
            subscribers.forEach { subscriber ->
                subscriber.mouseMoveEvent(this, xpos, ypos)
            }
        }

        GLFW.glfwSetScrollCallback(this.windowHandle) {win, xScroll, yScroll ->
            subscribers.forEach { subscriber ->
                subscriber.mouseScrollEvent(this, xScroll, yScroll)
            }
        }

    }


    override fun keyClick(key: RawInput): Boolean {
        return GLFW.glfwGetKey(this.windowHandle, key.glfwKey) == GLFW.GLFW_PRESS
    }

    override fun mouseClick(key: RawInput): Boolean {
        return GLFW.glfwGetMouseButton(this.windowHandle, key.glfwKey) == GLFW.GLFW_PRESS
    }


    override fun keyClick(key: String): Boolean {
        val key = this.getBinding(key)
        return keyClick(key)
    }

    override fun mouseClick(key: String): Boolean {
        val key = this.getBinding(key)
        return mouseClick(key)
    }

    override fun mousePos(): Vector2f {
        val mouseX = DoubleArray(1)
        val mouseY = DoubleArray(1)

        GLFW.glfwGetCursorPos(this.windowHandle, mouseX, mouseY)

        return Vector2f(mouseX[0].toFloat(), mouseY[0].toFloat())
    }

    override fun blockInput(block: Boolean) {
        this.inputBlock = block
    }

}