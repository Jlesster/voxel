package com.jless.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Controller {

  private static Matrix4f viewMatrix;
  private static Vector3f position;

  private static float pitch;
  private static float yaw;
  private static float roll;

  private float moveSpeed = 0.1f;
  private float mouseSens = 0.15f;

  public Controller(float x, float y, float z) {
    Controller.position = new Vector3f(x, y, z);
    Controller.pitch = 0;
    Controller.yaw = 0;
    Controller.roll = 0;
    Controller.viewMatrix = new Matrix4f();
  }

  public static Matrix4f getViewMatrix() {
    viewMatrix.identity();

    //apply roations
    viewMatrix.rotateX(pitch);
    viewMatrix.rotateY(yaw);
    viewMatrix.rotateZ(roll);

    //apply translation
    viewMatrix.translate(-position.x, -position.y, -position.z);

    return viewMatrix;
  }

  //mouse movement
  public void processMouse(float dx, float dy) {
    yaw += (float)Math.toRadians(dx * mouseSens);
    pitch += (float)Math.toRadians(dy * mouseSens);

    if(pitch > Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
    if(pitch < Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
  }

  public void moveF() {
    position.x += (float)Math.sin(yaw) * moveSpeed;
    position.z -= (float)Math.cos(yaw) * moveSpeed;
  }
  public void moveB() {
    position.x -= (float)Math.sin(yaw) * moveSpeed;
    position.z += (float)Math.cos(yaw) * moveSpeed;
  }

  public void moveL() {
    position.x += (float)Math.sin(yaw - Math.PI / 2) * moveSpeed;
    position.y -= (float)Math.sin(yaw - Math.PI / 2) * moveSpeed;
  }
  public void moveR() {
    position.x -= (float)Math.sin(yaw - Math.PI / 2) * moveSpeed;
    position.y += (float)Math.sin(yaw - Math.PI / 2) * moveSpeed;
  }

  public void moveU() {position.y += moveSpeed;}
  public void moveD() {position.y -= moveSpeed;}

  public Vector3f getPosition() {return position;}
  public float getX() {return position.x;}
  public float getY() {return position.y;}
  public float getZ() {return position.z;}
}
