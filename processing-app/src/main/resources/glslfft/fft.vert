#version 150

//#ifdef GL_ES
precision mediump float;
precision mediump int;
//#endif


void main(){
    int x = ((gl_VertexID<<1) & 2) - 1;
    int y = ((gl_VertexID) & 2) - 1;
    gl_Position = vec4(x, y, 0, 1);
}
