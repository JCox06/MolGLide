#version 330 core

out vec4 colour;

in float lLineDist;

uniform vec3 uLight = vec3(1.0f, 1.0f, 1.0f);
uniform int uDashed;

void main() {

    if (uDashed == 1) {
        float intensity = sin(25 * lLineDist);
        if (intensity < -0.5f) {
            colour = vec4(uLight, 1.0f);
        } else {
            discard;
        }
    } else {
        colour = vec4(uLight, 1.0f);
    }
}