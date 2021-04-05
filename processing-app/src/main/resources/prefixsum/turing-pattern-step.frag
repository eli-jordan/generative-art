#version 150

#ifdef GL_ES
precision highp float;
precision highp int;
#endif

#define MAX_SCALES 4

uniform sampler2D grid;

uniform sampler2D activator[MAX_SCALES];
uniform sampler2D inhibitor[MAX_SCALES];
uniform float bumpAmount[MAX_SCALES];

uniform int scaleCount;

uniform vec2 resolution;

out float glFragColor;

float map(
    float value,
    float start1, float stop1,
    float start2, float stop2) {
    return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
}

void main() {
    vec2 texCoord = gl_FragCoord.xy * resolution;
    float minVariation = 999.0;
    float step = 0.0f;
    for(int i = 0; i < scaleCount; i++) {
        float activatorValue = texture(activator[i], texCoord).x;
        float inhibitorValue = texture(inhibitor[i], texCoord).x;
        float variation = abs(activatorValue - inhibitorValue);
        if(variation < minVariation) {
            minVariation = variation;
            step = activatorValue > inhibitorValue ? bumpAmount[i] : -bumpAmount[i];
        }
    }

    float value = texture(grid, texCoord).x + step;

    glFragColor = map(value, -1 - step, 1 + step, -1, 1);
}
