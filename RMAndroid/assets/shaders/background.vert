attribute vec4 a_position;
attribute vec2 a_texCoord0;

varying vec2 v_tc;

void main()
{
    gl_Position = a_position;
    v_tc = a_texCoord0;
}