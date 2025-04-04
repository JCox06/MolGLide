#version 330 core

out vec4 colour;

in vec2 lTexCoord;
uniform sampler2D uFontTexture;

void main() {
    //Cool Colour
    colour = texture(uFontTexture, lTexCoord);
}