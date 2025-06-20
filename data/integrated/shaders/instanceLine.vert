#version 330 core


layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTex;


//These are applied via instance data:
layout (location = 2) in vec3 aOffset1;
layout (location = 3) in vec3 aOffset2;
layout (location = 4) in float aThickness;

uniform mat4 uPerspective;
uniform mat4 uModel;

out float lThickness;
out vec4 lPos1;
out vec4 lPos2;

void main() {
    lPos1 = uPerspective * uModel * vec4(aPos + aOffset1, 1.0f);
    lPos2 = uPerspective * uModel * vec4(aPos + aOffset2, 1.0f);
    lThickness = aThickness;
}