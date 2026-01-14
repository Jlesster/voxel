#version 120

attribute vec3 position;
attribute vec3 normal;
attribute vec2 texCoord;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 lightSpaceMatrix;

varying vec3 fragPosition;
varying vec3 fragNormal;
varying vec2 fragTexCoord;
varying vec4 fragPosLightSpace;

void main() {
  vec4 worldPos = modelMatrix * vec4(position, 1.0);
  fragPosition = worldPos.xyz;

  fragNormal = normalize((modelMatrix * vec4(normal, 0.0)).xyz);

  fragTexCoord = texCoord;

  fragPosLightSpace = lightSpaceMatrix * worldPos;

  gl_Position = projMatrix * viewMatrix * worldPos;
}
