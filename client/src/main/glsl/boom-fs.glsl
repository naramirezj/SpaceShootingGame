#version 300 es

precision highp float;

in vec3 normal;
in vec4 texCoord;

out vec4 fragmentColor; //#vec4# A four-element vector [r,g,b,a].; Alpha is opacity, we set it to 1 for opaque.; It will be useful later for transparency.

uniform struct{
  sampler2D colorTexture;
} material;

uniform struct {
  mat4 modelMatrix;
  vec2 shift;
} gameObject;

void main(void) {
  //fragmentColor = vec4 (tex, 0, 1);
  fragmentColor = texture(material.colorTexture, (texCoord.xy * (1.f/6.f) + gameObject.shift) );

}
