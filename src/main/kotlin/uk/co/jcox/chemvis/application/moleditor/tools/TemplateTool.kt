package uk.co.jcox.chemvis.application.moleditor.tools

import uk.co.jcox.chemvis.application.moleditor.ClickContext
import uk.co.jcox.chemvis.application.moleditor.ToolCreationContext
import uk.co.jcox.chemvis.cvengine.scenegraph.EntityLevel

class TemplateTool(context: ToolCreationContext) : Tool(context) {


    override fun update() {
        
    }

    override fun renderTransientUI(transientUI: EntityLevel) {

    }

    override fun processClick(clickDetails: ClickContext) {

    }

    override fun processClickRelease(clickDetails: ClickContext) {

    }

    override fun inProgress(): Boolean {
        TODO("Not Implemented yet")
    }

}