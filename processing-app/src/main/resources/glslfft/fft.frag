#version 410

// This shader is essentially a copy of this project https://github.com/rreusser/glsl-fft
// with some minor adaptions to allow it to run in the processing environment.

#ifdef GL_ES
precision highp float;
precision mediump int;
#endif


const float TWOPI = 6.283185307179586;

uniform vec2 resolution;
uniform sampler2D src;
uniform float subtransformSize;
uniform bool horizontal;
uniform bool forward;
uniform float normalization;

//layout(origin_upper_left) in vec4 gl_FragCoord;
out vec4 glFragColor;

void main() {
    vec2 evenPos, oddPos, twiddle;
    vec4 even, odd;
    float index, evenIndex, twiddleArgument;

    index = (horizontal ? gl_FragCoord.x : gl_FragCoord.y) - 0.5;

    evenIndex = floor(index / subtransformSize) * (subtransformSize * 0.5) + mod(index, subtransformSize * 0.5) + 0.5;

    if (horizontal) {
        evenPos = vec2(evenIndex, gl_FragCoord.y);
        oddPos = vec2(evenIndex, gl_FragCoord.y);
    } else {
        evenPos = vec2(gl_FragCoord.x, evenIndex);
        oddPos = vec2(gl_FragCoord.x, evenIndex);
    }

    evenPos *= resolution;
    oddPos *= resolution;

    if (horizontal) {
        oddPos.x += 0.5;
    } else {
        oddPos.y += 0.5;
    }

    even = texture(src, evenPos);
    odd = texture(src, oddPos);

    twiddleArgument = (forward ? TWOPI : -TWOPI) * (index / subtransformSize);
    twiddle = vec2(cos(twiddleArgument), sin(twiddleArgument));

    glFragColor = (even.rgba + vec4(
        twiddle.x * odd.xz - twiddle.y * odd.yw,
        twiddle.y * odd.xz + twiddle.x * odd.yw
    ).xzyw) * normalization;
}
