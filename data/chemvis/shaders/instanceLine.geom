#version 330 core

//Ignore the point data
layout(points) in;

//Send a thick line out
layout(triangle_strip, max_vertices = 4) out;

//Due to primitive asesembly being GL_POINTS, for one vertex shader, there is one fragment shader, each linked variable
//will have a fixed array size of 1
in float lThickness[1];
in vec4 lPos1[1];
in vec4 lPos2[1];

out float lLineDist;

uniform vec2 u_viewport;

uniform int uWidthMod;

void main() {

    //Get the line start and line end positions - Which after running through the
    //projection matrix are in clip space
    vec4 cs_lineStart = lPos1[0];
    vec4 cs_lineEnd = lPos2[0];

    //Convert to NDC by taking clip space and dividing by w comp (Perspective Division)
    //This gives us a 2D scene to work with, allowing the z component to be dropped.
    vec2 ndc_lineStart = cs_lineStart.xy / cs_lineStart.w;
    vec2 ndc_lineEnd = cs_lineEnd.xy / cs_lineEnd.w;
    vec2 ndc_direction = normalize(ndc_lineEnd - ndc_lineStart);

    //Have to take into account the scale of the viewport - Comes in later
    float ndc_lineScale = lThickness[0];

    vec2 ndc_dir_perp = vec2(ndc_direction.y, -ndc_direction.x);

    vec2 vectorthickness = (ndc_dir_perp / u_viewport) * ndc_lineScale * 2;


    //Step 3 - Send the vertices into the next stage of the pipeline (and times back to remove perspective division)
    //(As openGL will do it again right after this finishes)

    vec2 startThickness = vectorthickness;
    vec2 endThickness = vectorthickness;
    if (uWidthMod == 1) {
        endThickness = vectorthickness * 4;
    }

    //The line distance parameter is passed to every framgnet after linear interpolation

    gl_Position = vec4((ndc_lineStart - startThickness) * cs_lineStart.w, cs_lineStart.z, cs_lineStart.w);
    lLineDist = 0.0f;
    EmitVertex();


    gl_Position = vec4((ndc_lineStart + startThickness) * cs_lineStart.w, cs_lineStart.z, cs_lineStart.w);
    lLineDist = 0.0f;
    EmitVertex();


    gl_Position = vec4((ndc_lineEnd - endThickness) * cs_lineEnd.w, cs_lineEnd.z, cs_lineEnd.w);
    lLineDist = 1.0f;
    EmitVertex();


    gl_Position = vec4((ndc_lineEnd + endThickness) * cs_lineEnd.w, cs_lineEnd.z, cs_lineEnd.w);
    lLineDist = 1.0f;
    EmitVertex();


    EndPrimitive();
}

