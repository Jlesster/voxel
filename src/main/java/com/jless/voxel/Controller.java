package com.jless.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Controller {

  private final Vector3f velocity = new Vector3f();
  private Vector3f position;
  private Matrix4f viewMatrix;

  private static final float EYE_HEIGHT = 1.4f;
  private float pitch;
  private float yaw;
  private float roll;

  private Inventory inventory;

  private static final float WIDTH = 0.6f;
  private static final float HEIGHT = 1.8f;

  private Vector3f wishDir = new Vector3f();

  private static final float MAX_GROUND_SPEED = 6.0f;
  private static final float MAX_AIR_SPEED = 4.0f;
  private static final float GROUND_ACCEL = 40.0f;
  private static final float AIR_ACCEL = 15.0f;
  private static final float GROUND_FRICTION = 20.0f;
  private static final float GRAVITY = -30.0f;
  private static final float JUMP_VEL = 9.0f;

  private static float mouseSens = 0.15f;
  private float moveSpeed = 0.1f;

  private boolean onGround = false;

  public Controller(float x, float y, float z) {
    position = new Vector3f(x, y, z);
    pitch = 0;
    yaw = 0;
    roll = 0;
    viewMatrix = new Matrix4f();
  }

  public Inventory getInventory() {
    return inventory;
  }

  public Matrix4f getViewMatrix() {
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
  public void processMouse(float dx, float dy) {
    yaw += (float)Math.toRadians(dx * mouseSens);
    pitch += (float)Math.toRadians(dy * mouseSens);

    if(pitch > Math.toRadians(89.0f)) pitch = (float)Math.toRadians(89.0f);
    if(pitch < Math.toRadians(-89.0f)) pitch = (float)Math.toRadians(-89.0f);
  }

  public void update(World w, float dt, boolean jumpPressed,
    boolean f,
    boolean b,
    boolean l,
    boolean r
  ) {
    wishDir.set(0, 0, 0);
    velocity.y += GRAVITY * dt;
    Vector3f forwardDir = getForward();
    Vector3f rightDir = new Vector3f(forwardDir.z, 0, -forwardDir.x);

    if(f) wishDir.add(forwardDir.x, 0, forwardDir.z);
    if(b) wishDir.sub(forwardDir.x, 0, forwardDir.z);
    if(r) wishDir.sub(rightDir);
    if(l) wishDir.add(rightDir);

    if(wishDir.lengthSquared() > 0) {
      wishDir.normalize();
    }

    float maxSpeed = onGround ? MAX_GROUND_SPEED : MAX_AIR_SPEED;
    float accel = onGround ? GROUND_ACCEL : AIR_ACCEL;

    Vector3f wishVel = new Vector3f(wishDir).mul(maxSpeed);
    Vector3f horVel = new Vector3f(velocity.x, 0, velocity.z);
    Vector3f delta = wishVel.sub(horVel);

    float maxChange = accel * dt;
    if(delta.length() > maxChange) {
      delta.normalize().mul(maxChange);
    }

    velocity.x += delta.x;
    velocity.z += delta.z;

    if(onGround && wishDir.lengthSquared() == 0) {
      float speed = (float)Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
      if(speed > 0) {
        float drop = GROUND_FRICTION * dt;
        float newSpeed = Math.max(speed - drop, 0);
        float scale = newSpeed / speed;
        velocity.x *= scale;
        velocity.z *= scale;
      }
    }

    float hSpeed = (float)Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z);
    float maxH = onGround ? MAX_GROUND_SPEED : MAX_AIR_SPEED;

    if(hSpeed > maxH) {
      float s = maxH / hSpeed;
      velocity.x *= s;
      velocity.z *= s;
    }

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

  public boolean wouldCollideWithBlock(int bx, int by, int bz) {
    float minX = position.x - WIDTH / 2f;
    float minY = position.y;
    float minZ = position.z - WIDTH / 2f;
    float maxX = position.x + WIDTH / 2f;
    float maxY = position.y + HEIGHT;
    float maxZ = position.z + WIDTH / 2f;

    return bx < maxX && bx + 1 > minX &&
           by < maxY && by + 1 > minY &&
           bz < maxZ && bz + 1 > minZ;
  }
  public boolean collides(World w) {
    float minX = position.x - WIDTH / 2f;
    float minY = position.y;
    float minZ = position.z - WIDTH / 2f;
    float maxX = position.x + WIDTH / 2f;
    float maxY = position.y + HEIGHT;
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
    float cp = (float)Math.cos(pitch);
    float sp = (float)Math.sin(pitch);
    float cy = (float)Math.cos(yaw);
    float sy = (float)Math.sin(yaw);

    return new Vector3f(
      sy * cp,
      -sp,
      -cy * cp
    ).normalize();
  }

  private Vector3f getRight() {
    Vector3f f = getForward();
    Vector3f up = new Vector3f(0, 1, 0);

    return f.cross(up, new Vector3f()).normalize();
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
