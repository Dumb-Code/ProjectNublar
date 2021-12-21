#version 120

//https://glslsandbox.com/e#74928.26

vec2 size = vec2(158, 115);

uniform float progress;
uniform float seed;
varying vec2 texCoord;


// Permutation polynomial: (34x^2 + x) mod 289
vec4 permute(vec4 x) {
  return mod((34.0 * x + 1.0) * x, 289.0);
}

vec4 dist(vec4 x, vec4 y,  bool manhattanDistance) {
  return manhattanDistance ?  abs(x) + abs(y) :  (x * x + y * y);
}

// Cellular noise, returning F1 and F2 in a vec2.
// Speeded up by using 2x2 search window instead of 3x3,
// at the expense of some strong pattern artifacts.
// F2 is often wrong and has sharp discontinuities.
// If you need a smooth F2, use the slower 3x3 version.
// F1 is sometimes wrong, too, but OK for most purposes.
vec2 worley(vec2 P, float jitter, bool manhattanDistance) {

float K =  0.142857142857;// 1/7
float K2= 0.0714285714285; // K/2
	vec2 Pi = mod(floor(P), 289.0);
 	vec2 Pf = fract(P);
	vec4 Pfx = Pf.x + vec4(-0.5, -1.5, -0.5, -1.5);
	vec4 Pfy = Pf.y + vec4(-0.5, -0.5, -1.5, -1.5);
	vec4 p = permute(Pi.x + vec4(0.0, 1.0, 0.0, 1.0));
	p = permute(p + Pi.y + vec4(0.0, 0.0, 1.0, 1.0));
	vec4 ox = mod(p, 7.0)*K+K2;
	vec4 oy = mod(floor(p*K),7.0)*K+K2;
	vec4 dx = Pfx + jitter*ox;
	vec4 dy = Pfy + jitter*oy;
	vec4 d =  dist(dx, dy, manhattanDistance); // d11, d12, d21 and d22, squared
	// Sort out the two smallest distances

	// Do it right and find both F1 and F2
	d.xy = (d.x < d.y) ? d.xy : d.yx; // Swap if smaller
	d.xz = (d.x < d.z) ? d.xz : d.zx;
	d.xw = (d.x < d.w) ? d.xw : d.wx;
	d.y = min(d.y, d.z);
	d.y = min(d.y, d.w);
	return sqrt(d.xy);
}

//	Classic Perlin 2D Noise
//	by Stefan Gustavson
//
vec2 fade(vec2 t) {return t*t*t*(t*(t*6.0-15.0)+10.0);}

float perlin_noise(vec2 P){
  vec4 Pi = floor(P.xyxy) + vec4(0.0, 0.0, 1.0, 1.0);
  vec4 Pf = fract(P.xyxy) - vec4(0.0, 0.0, 1.0, 1.0);
  Pi = mod(Pi, 289.0); // To avoid truncation effects in permutation
  vec4 ix = Pi.xzxz;
  vec4 iy = Pi.yyww;
  vec4 fx = Pf.xzxz;
  vec4 fy = Pf.yyww;
  vec4 i = permute(permute(ix) + iy);
  vec4 gx = 2.0 * fract(i * 0.0243902439) - 1.0; // 1/41 = 0.024...
  vec4 gy = abs(gx) - 0.5;
  vec4 tx = floor(gx + 0.5);
  gx = gx - tx;
  vec2 g00 = vec2(gx.x,gy.x);
  vec2 g10 = vec2(gx.y,gy.y);
  vec2 g01 = vec2(gx.z,gy.z);
  vec2 g11 = vec2(gx.w,gy.w);
  vec4 norm = 1.79284291400159 - 0.85373472095314 *
    vec4(dot(g00, g00), dot(g01, g01), dot(g10, g10), dot(g11, g11));
  g00 *= norm.x;
  g01 *= norm.y;
  g10 *= norm.z;
  g11 *= norm.w;
  float n00 = dot(g00, vec2(fx.x, fy.x));
  float n10 = dot(g10, vec2(fx.y, fy.y));
  float n01 = dot(g01, vec2(fx.z, fy.z));
  float n11 = dot(g11, vec2(fx.w, fy.w));
  vec2 fade_xy = fade(Pf.xy);
  vec2 n_x = mix(vec2(n00, n01), vec2(n10, n11), fade_xy.x);
  float n_xy = mix(n_x.x, n_x.y, fade_xy.y);
  float ret = 2.3 * n_xy;
  float range = 1.0;
  return (ret + range) / (range*2.0);
}

float noise(vec2 P) {
	vec2 ret = worley(P, 1.0, true);
	return ret.x * ret.y;
}

float step_noise(vec2 p, float steps) {
	return ceil(clamp(noise(p), 0.0001, 1.0) * steps) / steps;
}

float hash(int n) {
    return fract(sin(float(n) / 41.7)*43758.5453);
}



void main(void) {

	vec2 position = floor(texCoord * size) / size;
	vec2 absolutePosition = position * size;

//	float progress = mod(time, 5.0) / 5.0;
//	int seed = int(floor(time / 5.0));

	int seedIn = int(seed);
	position.x += hash(seedIn) * 10000.0;
	position.y += hash(seedIn + 1) * 10000.0;

	vec3 colour;

	float plant_progress = clamp((progress - 0.1) / (1.0 - 0.1), 0.0, 1.0); //35%->100%  0.35 + (1.0 - 0.35) * a = p;; a =  (p - 0.35) / (1.0 - 0.35)
	float leaf_progress_noise = clamp(perlin_noise(position * 4.0) - 0.5 + ((plant_progress - 0.25) / 0.75) * 1.5, 0.0, 1.0);
	float dark_green_progress_noise = clamp(perlin_noise((position + 512.051) * 4.0) - 0.5 + (plant_progress / 0.75) * 1.5, 0.0, 1.0);

	float leaf_noise = worley(position * 20.0, 1.0, true).x;
	if(leaf_noise < 0.6 * leaf_progress_noise * leaf_progress_noise) {
		if(leaf_noise < 0.3) {
			colour = vec3(51.0, 109.0, 39.0) / 255.0; //Middle of leaf spots
		} else {
			colour = vec3(85.0, 128.0, 48.0) / 255.0; //Light edges of the leaf
		}
	} else if(worley(position * 21.5, 1.0, true).y > 2.0 - dark_green_progress_noise) {
		colour = vec3(67.0, 117.0, 48.0) / 255.0 * 0.6 * step_noise(position * 51.0, 3.0); //Dark green
	} else {
		float wooden_progress = clamp((progress + 0.05) / (0.65 - 0.05), 0.0, 1.0); //0%->65%
		float wooden_noise = clamp(((perlin_noise((position - 671.51) * 20.0) - 1.5) + (1.0 - worley((position - 671.51) * 4.0, 1.0, false).x)) + wooden_progress * 2.25, 0.0, 1.0);
		if(wooden_noise < 0.65) {
			discard;
		} else if(worley(position * 15.0, 1.0, true).x < 0.8 || worley(position * 50.0, 1.0, true).x < 0.65) {
			if(noise(position * 52.2) > 0.7) {
				colour = vec3(84.0, 51.0, 24.0) / 255.0; //Dark wood
			} else {
				colour = vec3(54.0, 23.0, 13.0) / 255.0; //Very dark wood
			}
		} else {
			colour = vec3(110.0, 83.0, 36.0) / 255.0; //Base wood colour
		}
	}



	float border = 12.0;
	if(absolutePosition.x < border)
		colour *= absolutePosition.x / border * 0.7 + 0.3;
	if(absolutePosition.y < border)
		colour *= absolutePosition.y / border * 0.7 + 0.3;
	if(absolutePosition.x > size.x - border)
		colour *= (size.x - absolutePosition.x) / border * 0.7 + 0.3;
	if(absolutePosition.y > size.y - border)
		colour *= (size.y - absolutePosition.y) / border * 0.7 + 0.3;

	colour *= noise(position * 20.0) * 0.5 + 0.5;



	gl_FragColor = vec4(colour, 1.0);
}