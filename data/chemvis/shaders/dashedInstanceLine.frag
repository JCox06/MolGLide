#version 330 core

out vec4 colour;

in float lLineDist;

uniform vec3 uLight = vec3(1.0f, 1.0f, 1.0f);

void main() {

    float intensity = sin(25 * lLineDist);
    if (intensity < -0.5f) {
        colour = vec4(uLight, 1.0f);
    } else {
        discard;
    }
}