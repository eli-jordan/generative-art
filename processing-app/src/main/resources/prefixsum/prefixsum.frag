#version 150

#ifdef GL_ES
precision highp float;
precision highp int;
#endif

uniform sampler2D src;
uniform vec2 resolution;
uniform int stride;

out float glFragColor;

void main() {
    vec2 p0 = (gl_FragCoord.xy - vec2(stride, 0)) * resolution;
    vec2 p1 = gl_FragCoord.xy * resolution;

    float v0 = texture(src, p0).x;
    float v1 = texture(src, p1).x;

    glFragColor = v0 + v1;
}
