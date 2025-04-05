#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTex;
layout (location = 2) in float aTexUnit;

out vec2 lTexCoord;
out float lTexUnit;

uniform mat4 uPerspective;
uniform mat4 uModel;

void main() {
    gl_Position = uPerspective  * uModel * vec4(aPos, 1.0f);
    lTexCoord = aTex;
    lTexUnit = aTexUnit;
}