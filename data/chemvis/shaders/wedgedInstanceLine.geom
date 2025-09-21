/*

How to create line:
1) From the vertex shader get the positions of the line START and line END in clip space

2) Convert this into NDC by doing a "fake" perspective division
    This also means that all geometry is now in the plane of the screen ranging from the NDC square (-1 to 1)

3) Work back to also convert the line Thickness in pixels into NDC. Work out how many NDC units per pixel

4) Now everything is in the screen plane, find the perpendicular vector of the NDC line START and LINE end positions

5) Multply by the recpricol of the viewport, along with the line thickness, and multiply by 2 (-1 to 1) so double the space

6) Perform the line thickness addition operation

7) Compensate for the next stage of the graphics pipeline, by remultiplying by w. This is so perspective division does not happen twice
*/

#version 330 core

layout(points) in;
layout(triangle_strip, max_vertices = 4) out;

in float lThickness[1];

in vec4 lPos1[1];
in vec4 lPos2[1];

uniform vec2 u_viewport;

void main() {
    //gl_in = Built in variable (has access to vertex output)

    //Step 1 - Get the two vertices that correspond to the start of the line and end of the line
    vec4 cs_lineStart = lPos1[0];
    vec4 cs_lineEnd = lPos2[0];

    //Step 2 - Convert to NDC by taking clip space and dividing by w comp
    //This gives us a 2D scene to work with, allowing the z component to be dropped.
    vec2 ndc_lineStart = cs_lineStart.xy / cs_lineStart.w;
    vec2 ndc_lineEnd = cs_lineEnd.xy / cs_lineEnd.w;
    vec2 ndc_direction = normalize(ndc_lineEnd - ndc_lineStart);

    float ndc_lineScale = lThickness[0];

    vec2 ndc_dir_perp = vec2(ndc_direction.y, -ndc_direction.x);

    vec2 vectorthickness = (ndc_dir_perp / u_viewport) * ndc_lineScale * 2;

    vec2 vectorThicknessBig = vectorthickness * 8;


    //Step 3 - Send the vertices into the next stage of the pipeline
    gl_Position = vec4((ndc_lineStart - vectorthickness) * cs_lineStart.w, cs_lineStart.z, cs_lineStart.w);
    EmitVertex();


    gl_Position = vec4((ndc_lineStart + vectorthickness) * cs_lineStart.w, cs_lineStart.z, cs_lineStart.w);
    EmitVertex();


    gl_Position = vec4((ndc_lineEnd - vectorThicknessBig) * cs_lineEnd.w, cs_lineEnd.z, cs_lineEnd.w);
    EmitVertex();


    gl_Position = vec4((ndc_lineEnd + vectorThicknessBig) * cs_lineEnd.w, cs_lineEnd.z, cs_lineEnd.w);
    EmitVertex();


    EndPrimitive();
}

