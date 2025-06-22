package uk.co.jcox.chemvis.application

import org.joml.Vector3f
import org.lwjgl.opengl.GL11
import uk.co.jcox.chemvis.application.moleditor.NewOrganicEditorState
import uk.co.jcox.chemvis.cvengine.*
import java.io.File

class MolGLide : IApplication, IInputSubscriber {

    private lateinit var camera: Camera2D


    private lateinit var services: ICVServices


    override fun init(engineServices: ICVServices) {

        this.services = engineServices
        services.resourceManager().loadFontFromDisc(FONT, File("data/chemvis/fonts/ubuntu.ttf"), CVEngine.STD_CHARACTER_SET, FONT_SIZE)
        val wm = services.windowMetrics()
        camera = Camera2D(wm.x, wm.y)


        GL11.glClearColor(0.22f, 0.22f, 0.26f, 1.0f)
//        GL11.glClearColor(254/128f, 250/128f, 224/128f, 1.0f)


        loadCoreAssets()

        services.inputs().subscribe(this)
        newState()
    }


    private fun newState() {

        val windowContext = WindowRenderingContext(services)

        val state = GlobalAppState(services, windowContext)
//        val state = NewOrganicEditorState(services, services.levelRenderer(), windowContext)

        services.setApplicationState(state, null)
//        services.inputs().subscribe(state)
    }

    override fun loop() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        val wm = services.windowMetrics()
        services.setViewport(0, 0, wm.x, wm.y)
        camera.update(wm.x, wm.y)

    }



    private fun loadCoreAssets() {

        val instancer = services.instancedRenderer()

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
        const val VERSION = "MolGLide 1.0-SNAPSHOT"
    }
}