package uk.co.jcox.chemvis.application

import uk.co.jcox.chemvis.application.moleditorstate.OrganicEditorState
import uk.co.jcox.chemvis.application.moleditorstate.tool.Tool
import uk.co.jcox.chemvis.application.ui.tool.ToolViewUI

class ToolRegistry {


    private val registeredTools = mutableMapOf<String, ToolSetup>()


    fun registerTool(toolID: String, friendlyName: String, toolViewUI: ToolViewUI, toolCreator: (editor: OrganicEditorState) -> Tool<out ToolViewUI>) {
        registeredTools[toolID] = ToolSetup(friendlyName, toolViewUI, toolCreator)
    }


    fun getEntries() : Map<String, ToolSetup> {
        return registeredTools
    }

    class ToolSetup (
        val name: String,
        val toolViewUI: ToolViewUI,
        val toolCreator: (editor: OrganicEditorState) -> Tool<out ToolViewUI>
    )
}