#version 410

#ifdef GL_ES
precision highp float;
precision mediump int;
#endif

uniform vec2 resolution;
uniform sampler2D src;

out vec4 glFragColor;

void main() {
    vec2 pos = gl_FragCoord.xy / resolution;
    glFragColor = texture(src, pos);
}
