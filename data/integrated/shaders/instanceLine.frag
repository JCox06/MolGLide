#version 330 core

out vec4 colour;

uniform vec3 uLight = vec3(1.0f, 1.0f, 1.0f);

void main() {
    colour = vec4(uLight, 1.0f);
}