#version 300 es

in vec4 vertexPosition; //#vec4# A four-element vector [x,y,z,w].; We leave z and w alone.; They will be useful later for 3D graphics and transformations. #vertexPosition# attribute fetched from vertex buffer according to input layout spec
in vec3 vertexNormal;
in vec4 vertexTexCoord;
out vec3 normal;
out vec4 texCoord;

uniform struct {
  mat4 modelMatrix;
  vec2 shift;
} gameObject;

uniform struct{
  mat4 viewProjMatrix; 
} camera;

uniform struct {
  float time;
} scene;


void main(void) {
  normal = vertexNormal;
  gl_Position = vertexPosition * gameObject.modelMatrix * camera.viewProjMatrix;
  texCoord = vertexTexCoord;
}
