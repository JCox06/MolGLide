#version 330 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec2 aTex;


out vec2 lTexCoord;

uniform mat4 cam;
uniform mat4 per;
uniform mat4 model;

uniform vec2 uTextureOffset;

void main() {
    gl_Position = per * cam * model * vec4(aPos, 1.0f);
    lTexCoord = aTex + uTextureOffset;
}