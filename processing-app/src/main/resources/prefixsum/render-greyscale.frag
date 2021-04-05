#version 150

#ifdef GL_ES
precision highp float;
precision highp int;
#endif

#define MAX_SCALES 4

uniform sampler2D grid;
uniform vec2 resolution;

out vec4 glFragColor;

float map(
float value,
float start1, float stop1,
float start2, float stop2) {
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}


void main() {
    vec2 texCoord = gl_FragCoord.xy * resolution;
    float value = map(texture(grid, texCoord).x, -1, 1, 0, 1);
    glFragColor = vec4(vec3(value), 1.0);
}
