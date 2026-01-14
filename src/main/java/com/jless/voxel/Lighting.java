package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_FUNC;
import static org.lwjgl.opengl.GL14.GL_TEXTURE_COMPARE_MODE;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Lighting {
  private float timeOfDay = 0.0f;
  private final Vector3f sunDir = new Vector3f();
  private final Vector3f sunColor = new Vector3f();
  private final Vector3f ambientColor = new Vector3f();
  private final Vector3f skyColor = new Vector3f();

  private int shadowFrameBuffer = 0;
  private int shadowDepthTex = 0;
  private final Matrix4f lightProj = new Matrix4f();
  private final Matrix4f lightView = new Matrix4f();
  private final Matrix4f lightSpaceMatrix = new Matrix4f();

  private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);
  private final FloatBuffer vec4Buffer = BufferUtils.createFloatBuffer(4);

  private boolean shadowsEnabled = true;

  public Lighting() {
    timeOfDay = 0.05f;
    updateLighting(0.0f);
  }

  public void update(float dt) {
    timeOfDay += dt / WorldConsts.DAY_LENGTH;

    if(timeOfDay >= 1.0f) timeOfDay -= 1.0f;

    updateLighting(dt);
  }

  private void updateLighting(float dt) {
    float angle = timeOfDay * (float)Math.PI * 2.0f; //time to angle conversion
    float sunY = (float)Math.sin(angle);
    float sunX = (float)Math.cos(angle);

    sunDir.set(sunX, -sunY, 0.3f).normalize();

    updateColors();
  }

  private void updateColors() {
    float sunHeight = -sunDir.y;

    if(sunHeight > 0) {
      float brightness = Math.min(1.0f, sunHeight * 1.5f);
      sunColor.set(brightness * 1.0f, brightness * 0.95f, brightness * 0.8f);
    } else {
      float moonBrightness = Math.max(0.0f, -sunHeight * 0.3f);
      sunColor.set(moonBrightness * 0.4f, moonBrightness * 0.5f, moonBrightness * 0.8f);
    }

    float ambientStrength = lerp(WorldConsts.AMBIENT_NIGHT, WorldConsts.AMBIENT_DAY, Math.max(0.0f, sunHeight));
    ambientColor.set(ambientStrength, ambientStrength * 0.9f, ambientStrength * 0.85f);

    if(sunHeight > 0.2f) {
      skyColor.set(0.5f, 0.7f, 1.0f);
    } else if(sunHeight > -0.1f) {
      float t = (sunHeight + 0.1f) / 0.3f;
      skyColor.set(
        lerp(0.8f, 0.5f, t),
        lerp(0.4f, 0.7f, t),
        lerp(0.2f, 1.0f, t)
      );
    } else {
      float nightness = Math.min(1.0f, -sunHeight * 3.0f);
      skyColor.set(0.05f * (1.0f - nightness), 0.05f * (1.0f - nightness), 0.01 * (1.0f - nightness));
    }
  }

  public void applyLighting() {
    vec4Buffer.clear();
    vec4Buffer.put(sunDir.x).put(sunDir.y).put(sunDir.z).put(0.0f);
    vec4Buffer.flip();
    glLightfv(GL_LIGHT0, GL_POSITION, vec4Buffer);

    vec4Buffer.clear();
    vec4Buffer.put(sunColor.x).put(sunColor.y).put(sunColor.z).put(1.0f);
    vec4Buffer.flip();
    glLightfv(GL_LIGHT0, GL_DIFFUSE, vec4Buffer);

    vec4Buffer.clear();
    vec4Buffer.put(ambientColor.x).put(ambientColor.y).put(ambientColor.z).put(1.0f);
    vec4Buffer.flip();
  }

  public void initShadowMapping() {
    if(!shadowsEnabled) return;

    shadowFrameBuffer = glGenFramebuffers();
    glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);

    shadowDepthTex = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, shadowDepthTex);

    glTexImage2D(
      GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT,
      WorldConsts.SHADOW_MAP_SIZE, WorldConsts.SHADOW_MAP_SIZE, 0,
      GL_DEPTH_COMPONENT, GL_FLOAT, (ByteBuffer)null
    );

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_MODE, GL_COMPARE_REF_TO_TEXTURE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_COMPARE_FUNC, GL_LEQUAL);

    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, shadowDepthTex, 0);

    glDrawBuffer(GL_NONE);
    glReadBuffer(GL_NONE);

    int status = glCheckFramebufferStatus(GL_FRAMEBUFFER);
    if(status != GL_FRAMEBUFFER_COMPLETE) {
      System.err.println("Err init shadows FrameBuffer incomplete");
      shadowsEnabled = false;
    }

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glBindTexture(GL_TEXTURE_2D, 0);

    System.out.println("Shadow Mapping initialised");
  }

  public void beginShadowPass(Vector3f playerPos) {
    if(!shadowsEnabled) return;

    glBindFramebuffer(GL_FRAMEBUFFER, shadowFrameBuffer);
    glClear(GL_DEPTH_BUFFER_BIT);

    glViewport(0, 0, WorldConsts.SHADOW_MAP_SIZE, WorldConsts.SHADOW_MAP_SIZE);

    float halfSize = WorldConsts.SHADOW_DISTANCE / 2.0f;
    lightProj.setOrtho(-halfSize, halfSize, -halfSize, halfSize, -100.0f, 200.0f);

    Vector3f lightPos = new Vector3f(sunDir).mul(100.0f).add(playerPos);
    Vector3f lookAt = new Vector3f(playerPos);
    Vector3f up = new Vector3f(0, 1, 0);
    lightView.setLookAt(lightPos, lookAt, up);

    lightSpaceMatrix.set(lightProj).mul(lightView);

    loadMatrixGL(lightProj, GL_PROJECTION);
    loadMatrixGL(lightView, GL_MODELVIEW);
  }

  public void endShadowPass(int w, int h) {
    if(!shadowsEnabled) return;

    glBindFramebuffer(GL_FRAMEBUFFER, 0);
    glViewport(0, 0, w, h);
  }

  public void bindShadowMap(int texUnit) {
    if(!shadowsEnabled) return;
    glActiveTexture(GL_TEXTURE0 + texUnit);
    glBindTexture(GL_TEXTURE_2D, shadowDepthTex);
  }

  public void loadMatrixGL(Matrix4f matrix, int mode) {
    matrixBuffer.clear();
    matrix.get(matrixBuffer);
    glMatrixMode(mode);
    glLoadMatrixf(matrixBuffer);
  }

  private float lerp(float a, float b, float t) {
    return a + (b - a) * Math.max(0.0f, Math.min(1.0f, t));
  }

  public Vector3f getSunDir() { return new Vector3f(sunDir); }
  public Vector3f getSunColor() { return new Vector3f(sunColor); }
  public Vector3f getAmbientColor() { return new Vector3f(ambientColor); }
  public Vector3f getSkyColor() { return new Vector3f(skyColor); }
  public Matrix4f getLightSpaceMatrix() { return new Matrix4f(lightSpaceMatrix); }
  public boolean areShadowsEnabled() { return shadowsEnabled; }
  public float getTimeOfDay() { return timeOfDay; }

  public void setTimeOfDay(float time) {
    this.timeOfDay = Math.max(0.0f, Math.min(1.0f, time));
    updateLighting(0.0f);
  }

  public void setShadowsEnabled(boolean enabled) {
    this.shadowsEnabled = enabled;
  }

  public void cleanup() {
    if(shadowDepthTex != 0) {
      glDeleteTextures(shadowDepthTex);
      shadowDepthTex = 0;
    }
    if(shadowFrameBuffer != 0) {
      glDeleteFramebuffers(shadowFrameBuffer);
      shadowFrameBuffer = 0;
    }
  }
}
