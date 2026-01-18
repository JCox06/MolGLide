package uk.co.jcox.chemvis.cvengine

import org.apache.commons.logging.Log
import org.lwjgl.PointerBuffer
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWNativeCocoa
import org.lwjgl.glfw.GLFWNativeWin32
import org.lwjgl.glfw.GLFWNativeX11
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.Platform
import org.lwjgl.util.nfd.NFDFilterItem
import org.lwjgl.util.nfd.NFDSaveDialogArgs
import org.lwjgl.util.nfd.NFDWindowHandle
import org.lwjgl.util.nfd.NativeFileDialog
import org.tinylog.Logger
import java.io.File
import java.nio.Buffer
import java.nio.ByteBuffer

class NativeFIleService (
) : IFileServices {

    private var windowHandle: Long = 0

    override fun init(windowHandle: Long) {
        NativeFileDialog.NFD_Init()
        this.windowHandle = windowHandle
    }

    override fun askUserSaveFile(extension: String, description: String): IFileServices.FileOperation {

        MemoryStack.stackPush().use { stack ->
            val pointerToFile = stack.mallocPointer(1)

            val filterBuffer = NFDFilterItem.calloc(1, stack)
            filterBuffer.get(0).set(stack.UTF8(description), stack.UTF8(extension))

            Logger.info { "Asking NFD for save use file dialogue" }

            val args = NFDSaveDialogArgs.create()
            args.set(filterBuffer, null, null, getWindowHandle())


            val result = NativeFileDialog.NFD_SaveDialog_With(pointerToFile, args)

            return validateResult(result, pointerToFile.get())

        }
    }


    override fun askUserChooseFile(extension: String, description: String): IFileServices.FileOperation {
        MemoryStack.stackPush().use { stack ->

            val pointerToFile = stack.mallocPointer(1)
            val filterBuffer = NFDFilterItem.calloc(1, stack)
            filterBuffer.get(0).set(stack.UTF8(description), stack.UTF8(extension))

            Logger.info { "Asking NFD for choose file dialogue" }

            val result = NativeFileDialog.NFD_OpenDialog(pointerToFile, filterBuffer, null as ByteBuffer?)

            return validateResult(result, pointerToFile.get())
        }
    }

    private fun validateResult(result: Int, resource: Long) : IFileServices.FileOperation {
        if (result == NativeFileDialog.NFD_OKAY) {
            Logger.info { "Success - NFD returned Okay" }
            val path = MemoryUtil.memUTF8(resource)
            NativeFileDialog.NFD_FreePath(resource)

            return IFileServices.FileOperation.FileRetrieved(File(path))
        }

        Logger.error { "NFD input failed" }
        return IFileServices.FileOperation.Error
    }

    private fun getWindowHandle() : NFDWindowHandle {
        val platform = when (Platform.get()) {
            Platform.FREEBSD -> NativeFileDialog.NFD_WINDOW_HANDLE_TYPE_X11
            Platform.LINUX -> NativeFileDialog.NFD_WINDOW_HANDLE_TYPE_X11
            Platform.MACOSX -> NativeFileDialog.NFD_WINDOW_HANDLE_TYPE_COCOA
            Platform.WINDOWS -> NativeFileDialog.NFD_WINDOW_HANDLE_TYPE_WINDOWS
        }

        val handle = when (Platform.get()) {
            Platform.FREEBSD -> GLFWNativeX11.glfwGetX11Window(windowHandle)
            Platform.LINUX -> GLFWNativeX11.glfwGetX11Window(windowHandle)
            Platform.MACOSX -> GLFWNativeCocoa.glfwGetCocoaWindow(windowHandle)
            Platform.WINDOWS -> GLFWNativeWin32.glfwGetWin32Window(windowHandle)
        }

        val win =  NFDWindowHandle.create()
        win.handle(handle )
        win.type(platform.toLong())

        return win
    }
}