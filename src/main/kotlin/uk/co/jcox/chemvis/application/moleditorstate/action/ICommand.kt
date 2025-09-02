package uk.co.jcox.chemvis.application.moleditorstate.action

interface ICommand {

    fun execute()

    fun undo()
}