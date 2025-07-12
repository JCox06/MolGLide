package uk.co.jcox.chemvis.application

import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.ui.ApplicationUI
import uk.co.jcox.chemvis.cvengine.*
import java.io.File

class MolGLide : IApplication, IInputSubscriber {
    private lateinit var services: ICVServices

    private lateinit var mainState: GlobalAppState

    private lateinit var mainApplicationUI: ApplicationUI

    override fun init(engineServices: ICVServices) {
        this.services = engineServices

        GL11.glClearColor(0.22f, 0.22f, 0.26f, 1.0f)
        loadCoreAssets()
        services.inputs().subscribe(this)
        newState()
    }


    private fun newState() {
        val windowContext = WindowRenderingContext(services)

        mainState = GlobalAppState(services, windowContext)

        services.setApplicationState(mainState, null)

        mainApplicationUI = ApplicationUI(mainState, services)
        mainApplicationUI.setup()
    }

    override fun loop() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val wm = services.windowMetrics()
        services.setViewport(0, 0, wm.x, wm.y)

        mainApplicationUI.drawApplicationUI()
    }


    private fun loadCoreAssets() {
        val instancer = services.instancedRenderer()

        services.resourceManager().loadFontFromDisc(FONT, File("data/chemvis/fonts/ubuntu.ttf"), CVEngine.STD_CHARACTER_SET, FONT_SIZE)

        val selectionMarkerMesh = Shaper2D.circle(0.0f, 0.0f, 1.0f)
        services.resourceManager().manageMesh(SELECTION_MARKER_MESH, selectionMarkerMesh, instancer)

        val markerMaterial = Material(Vector3f(0.11f, 0.11f, 0.11f))
        services.resourceManager().manageMaterial(SELECTION_MARKER_MATERIAL, markerMaterial)
    }

    override fun cleanup() {

    }

    companion object {
        const val FONT: String = "APP_FONT"
        const val FONT_SIZE = 140
        const val GLOBAL_SCALE: Float = 0.1f
        const val SELECTION_MARKER_MESH: String = "SELECTION_MARKER_MESH"
        const val SELECTION_MARKER_MATERIAL: String = "SELECTION_MARKER_MATERIAL"
        const val VERSION = "v0.0.5"
        const val WEBSITE = "https://github.com/JCox06/MolGLide/tree/master"
    }
}