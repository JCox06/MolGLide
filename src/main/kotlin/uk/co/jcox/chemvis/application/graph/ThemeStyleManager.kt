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
            backgroundColour = Vector4f(0.15f, 0.16f, 0.16f, 1.0f),

            defaultAtomColour = Vector3f(1.0f, 1.0f, 1.0f),

            symbolColours = mapOf(
                "H" to Vector3f(1.0f, 1.0f, 1.0f),
                "N" to Vector3f(1.0f, 1.0f, 1.0f),
                "O" to Vector3f(1.0f, 1.0f, 1.0f),
                "S" to Vector3f(1.0f, 1.0f, 1.0f),
                "F" to Vector3f(1.0f, 1.0f, 1.0f),
                "Cl" to Vector3f(1.0f, 1.0f, 1.0f),
                "Br" to Vector3f(1.0f, 1.0f, 1.0f),
            ) ,

            lineColour = Vector3f(1.0f, 1.0f, 1.0f),
        )

        val screenshotWhiteTheme = ThemeStyle(
            backgroundColour = Vector4f(1.0f, 1.0f, 1.0f, 1.0f),
            symbolColours = mapOf(
                "H" to Vector3f(0.0f, 0.0f, 0.0f),
                "N" to Vector3f(0.0f, 0.0f, 0.0f),
                "O" to Vector3f(0.0f, 0.0f, 0.0f),
                "S" to Vector3f(0.0f, 0.0f, 0.0f),
                "F" to Vector3f(0.0f, 0.0f, 0.0f),
                "Cl" to Vector3f(0.0f, 0.0f, 0.0f),
                "Br" to Vector3f(0.0f, 0.0f, 0.0f),
            ) ,
            defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f),



            lineColour = Vector3f(0.0f, 0.0f, 0.0f),
        )


        val screenshotMolGLideTheme = ThemeStyle(
            backgroundColour = Vector4f(1.0f, 1.0f, 1.0f, 1.0f),



            symbolColours = mapOf(
                "H" to Vector3f(0.0f, 0.0f, 0.0f),
                "C" to Vector3f(0.0f, 0.0f, 0.0f),
                "N" to Vector3f(0.0f, 0.41176f, 0.67843f),
                "O" to Vector3f(1.0f, 0.0f, 0.0f),
                "S" to Vector3f(0.8f, 0.7f, 0.0f),
                "P" to Vector3f(0.0f, 0.0f, 0.0f),
                "F" to Vector3f(0.2f, 0.8f, 0.0f),
                "Cl" to Vector3f(0.3f, 0.7f, 0.0f),
                "Br" to Vector3f(0.4f, 0.6f, 0.0f),

            ),

            defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f),
            lineColour = Vector3f(0.0f, 0.0f, 0.0f),
        )


        val CPKColouringTheme = ThemeStyle(
            backgroundColour = Vector4f(1.0f, 1.0f, 1.0f, 1.0f),

            symbolColours = mapOf(
                "H" to Vector3f(0.0f, 0.0f, 0.0f),
                "C" to Vector3f(0.0f, 0.0f, 0.0f),
                "N" to Vector3f(0.0f, 0.0f, 1.0f),
                "O" to Vector3f(1.0f, 0.0f, 0.0f),
                "S" to Vector3f(1.0f, 1.0f, 0.0f),
                "P" to Vector3f(1.0f, 0.6f, 0.0f),
                "F" to Vector3f(0.0f, 1.0f, 0.0f),
                "Cl" to Vector3f(0.0f, 1.0f, 0.0f),
                "Br" to Vector3f(0.8f, 0.0f, 0.0f),
            ),

            defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f),
            lineColour = Vector3f(0.0f, 0.0f, 0.0f),
        )

    }

}