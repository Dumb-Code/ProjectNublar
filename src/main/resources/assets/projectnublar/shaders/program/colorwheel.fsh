#version 120

//The shader used to draw color wheels
varying vec2 texCoord;
const float PI = 3.14159265;

uniform float lightness;

vec3 hsv2rgb(vec3 c) {
	vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
	vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
	return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main(void) {
	vec2 pos = texCoord - 0.5;
	float dist = pos.x*pos.x + pos.y*pos.y;
	if(dist < 0.25) {
		float angle = atan(pos.y, pos.x) - PI / 2.0;
		vec3 hsv = vec3(angle / (PI * 2.0), sqrt(dist) / 0.5, lightness);
		vec3 color = hsv2rgb(hsv);
		gl_FragColor = vec4(color, 1.0);
	} else {
		gl_FragColor = vec4(1.0, 1.0, 1.0, 0.0);
	}
}
