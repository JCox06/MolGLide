package uk.co.jcox.chemvis.cvengine

import org.joml.Vector2f
import org.lwjgl.glfw.GLFW

abstract class InputManager {


    protected val subscribers: MutableList<IInputSubscriber> = mutableListOf()
    private val bindings: MutableMap<String, RawInput> = mutableMapOf()


    abstract fun keyClick(key: RawInput) : Boolean
    abstract fun keyClick(key: String) : Boolean
    abstract fun mouseClick(key: RawInput) : Boolean
    abstract fun mouseClick(key: String) : Boolean
    abstract fun mousePos() : Vector2f

    abstract fun blockInput(block: Boolean)


    fun registerKeybinding(name: String, key: RawInput) {
        this.bindings[name] = key
    }

    fun subscribe(subscriber: IInputSubscriber) {
        this.subscribers.add(subscriber)
    }

    protected fun getBinding(id: String) : RawInput {
        val key = bindings[id]
        return key ?: RawInput.NULL
    }
}


interface IInputSubscriber {

    fun clickEvent(inputManager: InputManager, key: RawInput) {

    }

    fun clickReleaseEvent(inputManager: InputManager, key: RawInput) {

    }

    fun mouseMoveEvent(inputManager: InputManager, xPos: Double, yPos: Double) {

    }

    fun mouseScrollEvent(inputManager: InputManager, xScroll: Double, yScroll: Double) {

    }
}

enum class RawInput(val glfwKey: Int) {

    //Letters
    KEY_A(GLFW.GLFW_KEY_A),
    KEY_B(GLFW.GLFW_KEY_B),
    KEY_C(GLFW.GLFW_KEY_C),
    KEY_D(GLFW.GLFW_KEY_D),
    KEY_E(GLFW.GLFW_KEY_E),
    KEY_F(GLFW.GLFW_KEY_F),
    KEY_G(GLFW.GLFW_KEY_G),
    KEY_H(GLFW.GLFW_KEY_H),
    KEY_I(GLFW.GLFW_KEY_I),
    KEY_J(GLFW.GLFW_KEY_J),
    KEY_K(GLFW.GLFW_KEY_K),
    KEY_L(GLFW.GLFW_KEY_L),
    KEY_M(GLFW.GLFW_KEY_M),
    KEY_N(GLFW.GLFW_KEY_N),
    KEY_O(GLFW.GLFW_KEY_O),
    KEY_P(GLFW.GLFW_KEY_P),
    KEY_Q(GLFW.GLFW_KEY_Q),
    KEY_R(GLFW.GLFW_KEY_R),
    KEY_S(GLFW.GLFW_KEY_S),
    KEY_T(GLFW.GLFW_KEY_T),
    KEY_U(GLFW.GLFW_KEY_U),
    KEY_V(GLFW.GLFW_KEY_V),
    KEY_W(GLFW.GLFW_KEY_W),
    KEY_X(GLFW.GLFW_KEY_X),
    KEY_Y(GLFW.GLFW_KEY_Y),
    KEY_Z(GLFW.GLFW_KEY_Z),


    //Modifiers
    LSHIFT(GLFW.GLFW_KEY_LEFT_SHIFT),
    LCTRL(GLFW.GLFW_KEY_LEFT_CONTROL),
    LSUPER(GLFW.GLFW_KEY_LEFT_SUPER),
    LALT(GLFW.GLFW_KEY_LEFT_ALT),


    MOUSE_1(GLFW.GLFW_MOUSE_BUTTON_1),
    MOUSE_2(GLFW.GLFW_MOUSE_BUTTON_2),
    MOUSE_3(GLFW.GLFW_MOUSE_BUTTON_3),

    NULL(-1),

    ;

    companion object {

        private val glfwCache = entries.associateBy { it.glfwKey }

        fun fromGLFW(id: Int) : RawInput {
            val key = glfwCache[id]
            return key ?: NULL
        }
    }
}