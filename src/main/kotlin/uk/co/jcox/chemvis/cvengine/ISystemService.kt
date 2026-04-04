package uk.co.jcox.chemvis.cvengine

import java.io.File

interface ISystemService {

    fun init(windowHandle: Long)

    fun askUserSaveFile(extension: String, description: String) : FileOperation
    fun askUserChooseFile(extension: String, description: String) : FileOperation
    fun setClipboardContent(content: String)
    fun getClipboardContent() : String?
    fun openResource(resourceLocation: String)

    sealed class FileOperation {
        object Error : FileOperation()
        class FileRetrieved(val file: File) : FileOperation()
    }
}