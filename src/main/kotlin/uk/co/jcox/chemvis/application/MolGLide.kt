package uk.co.jcox.chemvis.application

import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.mainstate.MainState
import uk.co.jcox.chemvis.application.ui.ApplicationUI
import uk.co.jcox.chemvis.cvengine.*
import java.io.File

class MolGLide : IApplication, IInputSubscriber {
    private lateinit var services: ICVServices
    private lateinit var ui: ApplicationUI


    override fun init(engineServices: ICVServices) {
        this.services = engineServices

        GL11.glClearColor(0.22f, 0.22f, 0.26f, 1.0f)
        loadCoreAssets()
        services.inputs().subscribe(this)


        //Setup the main application state.
        //This application is bound to the main GLFW window - so has a render target of null
        //This main state is always running and manages the smaller mol editor states that are bound to the GLFW target windows
        //IMPORTANT*** - The UI sits above the main state
        val mainState = MainState(services, WindowRenderingContext(services))
        services.setApplicationState(mainState, null)

        ui = ApplicationUI(mainState, services)
        ui.setup()
    }



    override fun loop() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val wm = services.windowMetrics()
        services.setViewport(0, 0, wm.x, wm.y)


        ui.drawApplicationUI()
    }


    private fun loadCoreAssets() {
        val instancer = services.instancedRenderer()

        services.resourceManager().loadFontFromDisc(FONT, File("data/chemvis/fonts/ubuntu.ttf"), CVEngine.STD_CHARACTER_SET, FONT_SIZE)

        val selectionMarkerMesh = Shaper2D.circle(0.0f, 0.0f, 1.0f)
        services.resourceManager().manageMesh(SELECTION_MARKER_MESH, selectionMarkerMesh, instancer)

        val bondMarkerMesh = Shaper2D.rectangle(0.0f, 0.0f, 1.0f, 1.0f)
        services.resourceManager().manageMesh(BOND_MARKER_MESH, bondMarkerMesh, instancer)

        val markerMaterial = Material(Vector3f(0.11f, 0.11f, 0.11f))
        services.resourceManager().manageMaterial(SELECTION_MARKER_MATERIAL, markerMaterial)


        //Shaders for dashed and wedged line
        services.resourceManager().loadShadersFromDisc(SHADER_LINE, File("data/chemvis/shaders/instanceLine.vert"), File("data/chemvis/shaders/instanceLine.frag"), File("data/chemvis/shaders/instanceLine.geom"))
    }

    override fun cleanup() {

    }

    companion object {
        const val FONT: String = "APP_FONT"
        const val FONT_SIZE = 150
        const val GLOBAL_SCALE: Float = 0.1f
        const val SELECTION_MARKER_MESH: String = "SELECTION_MARKER_MESH"
        const val BOND_MARKER_MESH: String = "BOND_MARKER_MESH"
        const val SELECTION_MARKER_MATERIAL: String = "SELECTION_MARKER_MATERIAL"
        const val VERSION = "v0.2.0"
        const val WEBSITE = "https://github.com/JCox06/MolGLide/tree/master"

       const val SHADER_LINE = "SHADER_LINE"
    }
}