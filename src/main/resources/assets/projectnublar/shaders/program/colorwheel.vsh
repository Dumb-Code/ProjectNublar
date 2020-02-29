#version 120

attribute vec4 Position;

varying vec2 texCoord;

void main(){
    gl_Position = gl_ModelViewProjectionMatrix*gl_Vertex;
    texCoord = gl_MultiTexCoord0.xy;
    texCoord.y = 1.0 - texCoord.y;
}
