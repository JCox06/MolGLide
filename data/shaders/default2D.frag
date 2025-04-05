#version 330 core

out vec4 colour;
in vec2 lTexCoord;
in float lTexUnit;

//I believe that the number the spec gaurantees is 8
uniform sampler2D textures[8];
uniform sampler2D myText;

void main() {
    //Cool Colour

    int textureUnitIndex = int(lTexUnit);

    if (textureUnitIndex == 0) {
        colour = texture(textures[0], lTexCoord);
    }

    else if (textureUnitIndex == 1) {
        colour = texture(textures[1], lTexCoord);
    }
}