#version 330 core

//Currently all vertex arrays/meshes are loaded in with these two attributes
//Each line is a VAO that is instanced
//In this shader they don't do anything
layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTex;


//These are applied via instance data:
//aOffset1 = (xyz) for the line at Point A
//aOffset2 = (xyz) for the line at Point B
//thickness allows you to change the thickness of individual lines
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

    //NOTE: This shader is used with the primitive GL_POINTS.
    //The actual data sent over as points isn't needed in the geometry shader
    //The only important stuff is to get the lPos1 and lPos2 out
}