#version 330 core

out vec4 colour;
in vec2 lTexCoord;

//I believe that the number the spec gaurantees is 8
uniform sampler2D uTexture0;
uniform vec3 uLight = vec3(1.0f, 1.0f, 1.0f);

uniform bool uIgnoreTextures = false;

void main() {
    if (uIgnoreTextures) {
        colour = (vec4(uLight, 1.0f));
    } else {
        colour = (texture(uTexture0, lTexCoord)) * (vec4(uLight, 1.0f));
    }
}