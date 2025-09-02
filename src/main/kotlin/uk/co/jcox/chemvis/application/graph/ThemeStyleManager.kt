package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.Vector4f
import uk.co.jcox.chemvis.application.graph.ThemeStyleManager.Companion.MolGLideEditTheme

class ThemeStyleManager {

    var activeTheme = MolGLideEditTheme
    private set

    fun applyMolGLideEdit() {
        activeTheme = MolGLideEditTheme
    }

    fun applyScreenshotWhite() {
        activeTheme = screenshotWhiteTheme
    }

    fun applyScreenshotMolGLide() {
        activeTheme = screenshotMolGLideTheme
    }

    fun applyCPKTheme() {
        activeTheme = CPKColouringTheme
    }

    companion object {
        val MolGLideEditTheme = ThemeStyle(
            backgroundColour = Vector4f(0.1f, 0.1f, 0.1f, 0.1f),
            hydrogenColour = Vector3f(1.0f, 1.0f, 1.0f),
            carbonColour = Vector3f(1.0f, 1.0f, 1.0f),
            nitrogenColour = Vector3f(1.0f, 1.0f, 1.0f),
            oxygenColour = Vector3f(1.0f, 1.0f, 1.0f),
            sulphurColour = Vector3f(1.0f, 1.0f, 1.0f),
            phosphorusColour = Vector3f(1.0f, 1.0f, 1.0f),
            fluorineColour = Vector3f(1.0f, 1.0f, 1.0f),
            chlorineColour =Vector3f(1.0f, 1.0f, 1.0f),
            bromineColour = Vector3f(1.0f, 1.0f, 1.0f),
            metalColour = Vector3f(1.0f, 1.0f, 1.0f),
            defaultAtomColour = Vector3f(1.0f, 1.0f, 1.0f),
            lineColour = Vector3f(1.0f, 1.0f, 1.0f),
        )

        val screenshotWhiteTheme = ThemeStyle(
            backgroundColour = Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
            hydrogenColour = Vector3f(0.0f, 0.0f, 0.0f),
            carbonColour = Vector3f(0.0f, 0.0f, 0.0f),
            nitrogenColour = Vector3f(0.0f, 0.0f, 0.0f),
            oxygenColour = Vector3f(0.0f, 0.0f, 0.0f),
            sulphurColour = Vector3f(0.0f, 0.0f, 0.0f),
            phosphorusColour = Vector3f(0.0f, 0.0f, 0.0f),
            fluorineColour = Vector3f(0.0f, 0.0f, 0.0f),
            chlorineColour = Vector3f(0.0f, 0.0f, 0.0f),
            bromineColour = Vector3f(0.0f, 0.0f, 0.0f),
            metalColour = Vector3f(0.0f, 0.0f, 0.0f),
            defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f),
            lineColour = Vector3f(0.0f, 0.0f, 0.0f),
        )


        val screenshotMolGLideTheme = ThemeStyle(
            backgroundColour = Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
            hydrogenColour = Vector3f(0.0f, 0.0f, 0.0f),
            carbonColour = Vector3f(0.0f, 0.0f, 0.0f),
            nitrogenColour = Vector3f(0.0f, 0.41176f, 0.67843f),
            oxygenColour = Vector3f(1.0f, 0.0f, 0.0f),
            sulphurColour = Vector3f(0.8f, 0.7f, 0.0f),
            phosphorusColour = Vector3f(0.0f, 0.0f, 0.0f),
            fluorineColour = Vector3f(0.2f, 0.8f, 0.0f),
            chlorineColour = Vector3f(0.3f, 0.7f, 0.0f),
            bromineColour = Vector3f(0.4f, 0.6f, 0.0f),
            metalColour = Vector3f(0.9f, 0.9f, 0.9f),
            defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f),
            lineColour = Vector3f(0.0f, 0.0f, 0.0f),
        )


        val CPKColouringTheme = ThemeStyle(
            backgroundColour = Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
            hydrogenColour = Vector3f(0.0f, 0.0f, 0.0f),
            carbonColour = Vector3f(0.0f, 0.0f, 0.0f),
            nitrogenColour = Vector3f(0.0f, 0.0f, 1.0f),
            oxygenColour = Vector3f(1.0f, 0.0f, 0.0f),
            sulphurColour = Vector3f(1.0f, 1.0f, 0.0f),
            phosphorusColour = Vector3f(1.0f, 0.6f, 0.0f),
            fluorineColour = Vector3f(0.0f, 1.0f, 0.0f),
            chlorineColour = Vector3f(0.0f, 1.0f, 0.0f),
            bromineColour = Vector3f(0.8f, 0.0f, 0.0f),
            metalColour = Vector3f(0.9f, 0.9f, 0.9f),
            defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f),
            lineColour = Vector3f(0.0f, 0.0f, 0.0f),
        )

    }

}