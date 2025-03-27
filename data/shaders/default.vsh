#version 330 core

layout (location = 0) in vec3 aPos;

uniform mat4 cam;
uniform mat4 per;
uniform mat4 model;

void main() {

    gl_Position = per * cam * model * vec4(aPos, 1.0f);
}