package com.jless.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Controller {

  private static Matrix4f viewMatrix;
  private static Vector3f position;

  private static final float EYE_HEIGHT = 1.4f;
  private static float pitch;
  private static float yaw;
  private static float roll;

  private float moveSpeed = 0.1f;
  private static float mouseSens = 0.15f;

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
    viewMatrix.translate(-position.x, -position.y - EYE_HEIGHT, -position.z);

    return viewMatrix;
  }

  //mouse movement
  public static void processMouse(float dx, float dy) {
    yaw += (float)Math.toRadians(dx * mouseSens);
    pitch += (float)Math.toRadians(dy * mouseSens);

    if(pitch > Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
    if(pitch < Math.toRadians(-89.0f)) pitch = (float)Math.toRadians(-89.0f);
  }

  public Vector3f getForward() {
    Vector3f forward = new Vector3f(0, 0, -1);

    Matrix4f viewMatrix = new Matrix4f(getViewMatrix());
    viewMatrix.invert();
    viewMatrix.transformDirection(forward);

    return forward.normalize();
  }

  private Vector3f getRight() {
    Vector3f f = getForward();
    Vector3f up = new Vector3f(0, 1, 0);

    return f.cross(up, new Vector3f()).normalize();
  }

  public void moveF() {
    Vector3f f = getForward();
    position.add(f.mul(moveSpeed));
  }
  public void moveB() {
    Vector3f b = getForward();
    position.sub(b.mul(moveSpeed));
  }

  public void moveL() {
    Vector3f l = getRight();
    position.sub(l.mul(moveSpeed));
  }
  public void moveR() {
    Vector3f r = getRight();
    position.add(r.mul(moveSpeed));
  }

  public void moveU() {position.y += moveSpeed;}
  public void moveD() {position.y -= moveSpeed;}

  public Vector3f getPosition() {return position;}
  public Vector3f getEyePos() {
    return new Vector3f(
      position.x,
      position.y + EYE_HEIGHT,
      position.z
    );
  }
  public float getX() {return position.x;}
  public float getY() {return position.y;}
  public float getZ() {return position.z;}
}
