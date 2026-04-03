#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTex;

//Vertex Attributes from the instance
layout (location = 2) in vec3 aPosMod;
layout (location = 3) in float aScaleMod;

out vec2 lTexCoord;

uniform mat4 uPerspective;
uniform mat4 uModel;

void main() {

    float newScale = max(1, aScaleMod);
    vec4 newPos = vec4((aPos + aPosMod) * newScale, 1.0f);
    gl_Position = uPerspective  * uModel * newPos;
    lTexCoord = aTex;
}