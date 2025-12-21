package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.Vector4f

class ThemeStyleManager {

    var backgroundColour = Vector4f(0.15f, 0.16f, 0.16f, 1.0f)
    var defaultAtomColour = Vector3f(1.0f, 1.0f, 1.0f)
    var lineColour = Vector3f(1.0f, 1.0f, 1.0f)
    var lineThickness = 1.25f
    var symbolColours = mapOf(
        "H" to Vector3f(1.0f, 1.0f, 1.0f),
        "N" to Vector3f(1.0f, 1.0f, 1.0f),
        "O" to Vector3f(1.0f, 1.0f, 1.0f),
        "S" to Vector3f(1.0f, 1.0f, 1.0f),
        "F" to Vector3f(1.0f, 1.0f, 1.0f),
        "Cl" to Vector3f(1.0f, 1.0f, 1.0f),
        "Br" to Vector3f(1.0f, 1.0f, 1.0f),
    )

    fun applyMolGLideEdit() {
        backgroundColour = Vector4f(0.15f, 0.16f, 0.16f, 1.0f)
        defaultAtomColour = Vector3f(1.0f, 1.0f, 1.0f)
        symbolColours = mapOf(
            "H" to Vector3f(1.0f, 1.0f, 1.0f),
            "N" to Vector3f(1.0f, 1.0f, 1.0f),
            "O" to Vector3f(1.0f, 1.0f, 1.0f),
            "S" to Vector3f(1.0f, 1.0f, 1.0f),
            "F" to Vector3f(1.0f, 1.0f, 1.0f),
            "Cl" to Vector3f(1.0f, 1.0f, 1.0f),
            "Br" to Vector3f(1.0f, 1.0f, 1.0f),
        )
        lineColour = Vector3f(1.0f, 1.0f, 1.0f)
        lineThickness = 1.25f
    }

    fun applyScreenshotWhite() {
        backgroundColour = Vector4f(0.0f, 0.0f, 0.0f, 0.0f)
        symbolColours = mapOf(
            "H" to Vector3f(0.0f, 0.0f, 0.0f),
            "N" to Vector3f(0.0f, 0.0f, 0.0f),
            "O" to Vector3f(0.0f, 0.0f, 0.0f),
            "S" to Vector3f(0.0f, 0.0f, 0.0f),
            "F" to Vector3f(0.0f, 0.0f, 0.0f),
            "Cl" to Vector3f(0.0f, 0.0f, 0.0f),
            "Br" to Vector3f(0.0f, 0.0f, 0.0f),
        )
        defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f)

        lineColour = Vector3f(0.0f, 0.0f, 0.0f)
        lineThickness = 1.25f
    }

    fun applyScreenshotMolGLide() {
        backgroundColour = Vector4f(0.0f, 0.0f, 0.0f, 0.0f)


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
            )

        defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f)
        lineColour = Vector3f(0.0f, 0.0f, 0.0f)
        lineThickness = 1.25f
    }

    fun applyCPKTheme() {
        backgroundColour = Vector4f(1.0f, 1.0f, 1.0f, 1.0f)

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
        )

        defaultAtomColour = Vector3f(0.0f, 0.0f, 0.0f)
        lineColour = Vector3f(0.0f, 0.0f, 0.0f)
        lineThickness = 1.25f
    }

}