package com.jless.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Controller {

  private final Vector3f velocity = new Vector3f();
  private static Vector3f position;
  private static Matrix4f viewMatrix;

  private static final float EYE_HEIGHT = 1.4f;
  private static float pitch;
  private static float yaw;
  private static float roll;

  private static final float WIDTH = 0.6f;
  private static final float HEIGHT = 1.0f;

  private static final float GRAVITY = -30.0f;
  private static final float JUMP_VEL = 9.0f;

  private static float mouseSens = 0.15f;
  private float moveSpeed = 0.1f;

  private boolean onGround = false;

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

  public void update(World w, float dt, boolean jumpPressed) {
    velocity.y += GRAVITY * dt;

    if(jumpPressed && onGround) {
      velocity.y = JUMP_VEL;
      onGround = false;
    }
    move(w, velocity.x * dt, velocity.y * dt, velocity.z * dt);
  }

  private void move(World w, float dx, float dy, float dz) {
    if(dx != 0) {
      position.x += dx;
      if(collides(w)) {
        position.x -= dx;
        velocity.x = 0;
      }
    }

    if(dz != 0) {
      position.z += dz;
      if(collides(w)) {
        position.z -= dz;
        velocity.z = 0;
      }
    }

    if(dy != 0) {
      position.y += dy;
      if(collides(w)) {
        position.y -= dy;

        if(dy <= 0) {
          onGround = true;
        }

        velocity.y = 0;
      } else {
        onGround = false;
      }
    }
  }

  private boolean collides(World w) {
    float minX = position.x - WIDTH / 2f;
    float minY = position.y;
    float minZ = position.z - WIDTH / 2f;
    float maxX = position.x + WIDTH / 2f;
    float maxY = position.y + HEIGHT / 2f;
    float maxZ = position.z + WIDTH / 2f;

    int x0 = (int)Math.floor(minX);
    int x1 = (int)Math.floor(maxX);
    int y0 = (int)Math.floor(minY);
    int y1 = (int)Math.floor(maxY);
    int z0 = (int)Math.floor(minZ);
    int z1 = (int)Math.floor(maxZ);

    for(int x = x0; x <= x1; x++) {
      for(int y = y0; y <= y1; y++) {
        for(int z = z0; z <= z1; z++) {
          if(Blocks.SOLID[w.getIfLoaded(x, y, z)]) {
            return true;
          }
        }
      }
    }
    return false;
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
    velocity.x += f.x * moveSpeed;
    velocity.z += f.z * moveSpeed;
  }
  public void moveB() {
    Vector3f b = getForward();
    velocity.x -= b.x * moveSpeed;
    velocity.z -= b.z * moveSpeed;
  }

  public void moveL() {
    Vector3f l = getRight();
    velocity.x -= l.x * moveSpeed;
    velocity.z -= l.z * moveSpeed;
  }
  public void moveR() {
    Vector3f r = getRight();
    velocity.x += r.x * moveSpeed;
    velocity.z += r.z * moveSpeed;
  }

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
