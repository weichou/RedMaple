#ifdef GL_ES
precision mediump float;
#endif

uniform float u_hype;
varying vec2 v_tc;

const vec4 c_orange = vec4(1.0, 0.435, 0, 1);
const vec4 c_yellow = vec4(1, 0.9, 0.03, 1);

const vec4 c_oranget = vec4(177.0/255.0, 62.0/255.0, 15.0/255.0, 1);
const vec4 c_bluet = vec4(39.0/255.0, 26.0/255.0, 54.0/255.0, 1);

void main()
{

    vec4 mixed = mix(c_bluet, c_oranget, v_tc.y * 0.7);
    mixed.a = 1.0;

    gl_FragColor = mixed;
}