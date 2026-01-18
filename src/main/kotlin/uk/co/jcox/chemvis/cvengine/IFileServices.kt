package uk.co.jcox.chemvis.cvengine

import java.io.File

interface IFileServices {

    fun init(windowHandle: Long)

    fun askUserSaveFile(extension: String, description: String) : FileOperation
    fun askUserChooseFile(extension: String, description: String) : FileOperation



    sealed class FileOperation {
        object Error : FileOperation()
        class FileRetrieved(val file: File) : FileOperation()
    }
}