#version 410

#ifdef GL_ES
precision highp float;
precision mediump int;
#endif

const float PI = 3.14159265358979;

uniform vec2 resolution;

out vec4 glFragColor;

float distanceSq(vec2 a, vec2 b) {
    vec2 c = a - b;
    return dot(c, c);
}

void main() {
    float radius = 100;
    float radiusSq = radius * radius;
//    float area = PI * radius * radius;

    float d1 = distanceSq(gl_FragCoord.xy, vec2(0, 0));
    float d2 = distanceSq(gl_FragCoord.xy, resolution);
    float d3 = distanceSq(gl_FragCoord.xy, vec2(0, resolution.y));
    float d4 = distanceSq(gl_FragCoord.xy, vec2(resolution.x, 0));

    vec4 vs = 1-step(radiusSq, vec4(d1, d2, d3, d4));
    float sum = vs.x + vs.y + vs.z + vs.w;

    glFragColor = vec4(vec3(sum), 1.0);
}
