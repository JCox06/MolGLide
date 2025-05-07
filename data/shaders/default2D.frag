#version 330 core

out vec4 colour;
in vec2 lTexCoord;

//I believe that the number the spec gaurantees is 8
uniform sampler2D mainTexture;
uniform vec3 fontColour = vec3(1.0f, 1.0f, 1.0f);
uniform bool textureMode = true;

void main() {
    //Cool Colour

    //todo - Get font colour working!
    if (textureMode) {
        colour = (texture(mainTexture, lTexCoord)) * (vec4(fontColour, 1.0f));
    } else {
        colour = vec4(fontColour, 1.0f);
    }

}