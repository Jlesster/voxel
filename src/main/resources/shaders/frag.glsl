#version 120

varying vec2 fragTexCoord;
varying vec3 fragNormal;
varying vec3 fragPosition;
uniform vec3 sunDir;
uniform vec3 sunColor;
uniform vec3 ambientColor;
varying vec4 fragPosLightSpace;

uniform sampler2D textureSampler;
uniform sampler2DShadow shadowMap;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;
uniform mat4 modelMatrix;
uniform mat4 lightSpaceMatrix;

uniform vec3 solidColor;
uniform int useSolidColor;

uniform float shadowBias;

void main() {
  vec4 texColor = texture2D(textureSampler, fragTexCoord);
  if(useSolidColor == 1) {
    texColor = vec4(solidColor, 1.0);
  }
  vec3 N = normalize(fragNormal);
  vec3 L = normalize(-sunDir);

  float diff = max(dot(N, L), 0.15);

  vec3 projCoords = fragPosLightSpace.xyz / fragPosLightSpace.w;
  projCoords = projCoords * 0.5 + 0.5;

  float shadowFactor = 1.0;

  if(projCoords.x < 0.0 || projCoords.x > 1.0 ||
     projCoords.y < 0.0 || projCoords.y > 1.0 ||
     projCoords.z < 0.0 || projCoords.z > 1.0) {
    shadowFactor = shadow2D(shadowMap, vec3(projCoords.xy, projCoords.z - shadowBias)).r;
  }

  vec3 light = ambientColor + sunColor * diff * shadowFactor;
  vec3 finalColor = texColor.rgb * light;

  gl_FragColor = vec4(finalColor, texColor.a);
}
