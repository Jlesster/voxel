package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector3f;

public class EntityPenguin extends Entity {
  private static final float WIDTH = 0.8f;
  private static final float HEIGHT = 1.6f;
  private static final float LENGTH = 1.4f;
  private static final float STEP_HEIGHT = 0.6f;
  private static final float FEET_EPS = 0.001f;

  private float getMinX(float x) { return x - (WIDTH * 0.5f); }
  private float getMaxX(float x) { return x + (WIDTH * 0.5f); }
  private float getMinZ(float z) { return z - (LENGTH * 0.5f); }
  private float getMaxZ(float z) { return z + (LENGTH * 0.5f); }

  private float getMinY(float yFeet) { return yFeet; }
  private float getMaxY(float yFeet) { return yFeet + HEIGHT; }

  private float wanderTimer = 0f;
  private float idleTimer = 0f;
  private float animTime = 0f;
  private float yawDeg = 0f;
  private float targetYaw = 0f;

  private final Vector3f wishDir = new Vector3f();

  public EntityPenguin(float x, float y, float z) {
    pos.set(x, y, z);
  }

  private boolean canStepTo(World world, float x, float y, float z) {
    //TODO build this fucker
    return canStepTo(world, x, y, z);
  }

  private boolean resolveCollisions(World world) {
    boolean pushedUp = false;

    float ax0 = getMinX(pos.x);
    float ax1 = getMaxX(pos.x);
    float ay0 = getMinY(pos.y);
    float ay1 = getMaxY(pos.y);
    float az0 = getMinZ(pos.z);
    float az1 = getMaxZ(pos.z);

    int bx0 = (int)Math.floor(ax0);
    int bx1 = (int)Math.floor(ax1 - FEET_EPS);
    int by0 = (int)Math.floor(ay0);
    int by1 = (int)Math.floor(ay1 - FEET_EPS);
    int bz0 = (int)Math.floor(az0);
    int bz1 = (int)Math.floor(az1 - FEET_EPS);

    if(by0 < 0) by0 = 0;
    if(by1 >= WorldConsts.WORLD_HEIGHT) by1 = WorldConsts.WORLD_HEIGHT - 1;

    for(int bx = bx0; bx <= bx1; bx++) {
      for(int by = by0; by <= by1; by++) {
        for(int bz = bz0; bz <= bz1; bz++) {
          byte id = world.get(bx, by, bz);
          if(!Blocks.SOLID[id]) continue;

          float px0 = bx;
          float px1 = bx + 1.0f;
          float py0 = by;
          float py1 = by + 1.0f;
          float pz0 = bz;
          float pz1 = bz + 1.0f;

          if(!aabbOverlap(ax0, ay0, az0, ax1, ay1, az1, px0, py0, pz0, px1, py1, pz1)) continue;

          float overlapX1 = ax1 - px0;
          float overlapX2 = px1 - ax0;
          float pushX = (overlapX1 < overlapX2) ? -overlapX1 : overlapX2;

          float overlapY1 = ay1 - py0;
          float overlapY2 = py1 - ay0;
          float pushY = (overlapY1 < overlapY2) ? -overlapY1 : overlapY2;

          float overlapZ1 = az1 - pz0;
          float overlapZ2 = pz1 - az0;
          float pushZ = (overlapZ1 < overlapZ2) ? -overlapZ1 : overlapZ2;

          float ax = Math.abs(pushX);
          float ay = Math.abs(pushY);
          float az = Math.abs(pushZ);

          if(ax <= ay && ax <= az) {
            pos.x += pushX;
          } else if(az <= ax && az <= ay) {
            pos.z += pushZ;
          } else {
            pos.y += pushY;
            if(pushY > 0) pushedUp = true;
            if(pushY > 0 && vel.y < 0) vel.y = 0;
          }

          ax0 = getMinX(pos.x);
          ax1 = getMaxX(pos.x);
          ay0 = getMinY(pos.y);
          ay1 = getMaxY(pos.y);
          az0 = getMinZ(pos.z);
          az1 = getMaxZ(pos.z);
        }
      }
    }
    return pushedUp;
  }

  private static boolean aabbOverlap(
    float ax0, float ay0, float az0, float ax1, float ay1, float az1,
    float bx0, float by0, float bz0, float bx1, float by1, float bz1
  ) {
    return ax0 < bx1 && ax1 > bx0 &&
           ay0 < by1 && ay1 > by0 &&
           az0 < bz1 && az1 > bz0;
  }

  private boolean hitboxBlocked(World world, float x, float y, float z) {
    float rx = WIDTH * 0.5f;
    float rz = LENGTH * 0.5f;

    float xMin = x - rx;
    float xMax = x + rx;
    float zMin = z - rz;
    float zMax = z + rz;

    int bx0 = (int)Math.floor(xMin);
    int bx1 = (int)Math.floor(xMax);
    int bz0 = (int)Math.floor(zMin);
    int bz1 = (int)Math.floor(zMax);

    int by0 = (int)Math.floor(y + FEET_EPS);
    int by1 = (int)Math.floor(y + HEIGHT - FEET_EPS);

    if(by0 < 0) by0 = 0;
    if(by1 >= WorldConsts.WORLD_HEIGHT) by1 = WorldConsts.WORLD_HEIGHT - 1;

    for(int bx = bx0; bx <= bx1; bx++) {
      for(int by = by0; by <= by1; by++) {
        for(int bz = bz0; bz <= bz1; bz++) {
          byte id = world.get(bx, by, bz);
          if(Blocks.SOLID[id]) return true;
        }
      }
    }
    return false;
  }

  private int getWalkable(World world, int wx, int wz, int startY) {
    if(startY >= WorldConsts.WORLD_HEIGHT) startY = WorldConsts.WORLD_HEIGHT - 1;
    if(startY < 0) startY = 0;

    for(int y = startY; y >= 0; y--) {
      byte id = world.getBlockWorld(wx, y, wz);
      if(id == BlockID.AIR) continue;
      if(id == BlockID.LEAF) continue;
      if(Blocks.SOLID[id]) return y;
    }
    return -1;
  }

  private float getGroundY(World world, float x, float z) {
    float rx = WIDTH * 0.5f;
    float rz = LENGTH * 0.5f;

    int startY = (int)Math.floor(pos.y + 2.0f);
    int x0 = (int)Math.floor(x - rx);
    int x1 = (int)Math.floor(x + rx);
    int z0 = (int)Math.floor(z - rz);
    int z1 = (int)Math.floor(z + rz);

    int y00 = getWalkable(world, x0, z0, startY);
    int y10 = getWalkable(world, x1, z0, startY);
    int y01 = getWalkable(world, x0, z1, startY);
    int y11 = getWalkable(world, x1, z1, startY);

    int y = Math.min(Math.min(y00, y10), Math.min(y01, y11));
    return y + 1.0f;
  }

  @Override
  public void update(World world, float dt) {
    vel.y -= 20.0f * dt;
    animTime += dt * 6.0f;
    pos.y += vel.y * dt;

    //idle adn wander
    if(idleTimer > 0f) {
      idleTimer -= dt;
      wishDir.set(0, 0, 0);
    } else {
      wanderTimer -= dt;

      if(wanderTimer <= 0f) {
        float a  = (float)(Math.random() * Math.PI * 2.0);
        wishDir.set((float)Math.cos(a), 0, (float)Math.sin(a));

        wanderTimer = 1.5f + (float)Math.random() * 2.0f;

        if(Math.random() < 0.30) idleTimer = 0.05f + (float)Math.random();
      }
    }
    float speed = 2.0f;
    vel.x = wishDir.x * speed;
    vel.z = wishDir.z * speed;

    //collision jumping
    float dx = vel.x * dt;
    float dz = vel.z * dt;
    boolean stepped = false;

    float nx = pos.x + dx;
    if(!hitboxBlocked(world, nx, pos.y, pos.z)) {
      pos.x = nx;
    } else {
      float stepY = pos.y + STEP_HEIGHT;
      if(!hitboxBlocked(world, nx, stepY, pos.z)) {
        float groundAfterStep = getGroundY(world, nx, pos.z);

        if(groundAfterStep > pos.y && groundAfterStep <= pos.y + STEP_HEIGHT + 0.01) {
          pos.y = groundAfterStep;
          pos.x = nx;
          vel.y = 0;
          stepped = true;
        }
      }
    }

    float nz = pos.z + dz;
    if(!hitboxBlocked(world, pos.x, pos.y, nz)) {
      pos.z = nz;
    } else {
      float stepY = pos.y + STEP_HEIGHT;
      if(!hitboxBlocked(world, pos.x, stepY, nz)) {
        float groundAfterStep = getGroundY(world, pos.x, nz);

        if(groundAfterStep > pos.y && groundAfterStep <= pos.y + STEP_HEIGHT) {
          pos.y = groundAfterStep;
          pos.z = nz;
          vel.y = 0;
          stepped = true;
        }
      }
    }

    boolean pushedUp = resolveCollisions(world);
    if(pushedUp) stepped = true;

    if(!stepped) {
      float groundY = getGroundY(world, pos.x, pos.z);
      if(pos.y < groundY) {
        pos.y = groundY;
        vel.y = 0;
      }
    }

    float moveSq = (dx * dx) + (dz * dz);
    if(moveSq > 0.0001f) {
      targetYaw = (float)Math.toDegrees(Math.atan2(-dx, -dz));
    }

    float turnSpeed = 180f;
    float diff = wrapAngleDeg(targetYaw - yawDeg);
    float maxTurn = turnSpeed * dt;
    if(diff > maxTurn) diff = maxTurn;
    if(diff < -maxTurn) diff = -maxTurn;
    yawDeg = wrapAngleDeg(yawDeg + diff);
  }

  @Override
  public void render(VoxelRender render) {
    glPushMatrix();
    glTranslatef(pos.x, pos.y, pos.z);
    glRotatef(yawDeg, 0f, 1f, 0f);
    glTranslatef(-0.5f, 0.0f, -0.7f);

    glScalef(0.7f, 0.7f, 0.7f);

    glPushAttrib(GL_ENABLE_BIT | GL_CURRENT_BIT | GL_LIGHTING_BIT | GL_TEXTURE_BIT);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_CULL_FACE);
    glDisable(GL_ALPHA_TEST);
    glDisable(GL_BLEND);
    glDisable(GL_LIGHTING);

    float swing = (float) ((float)Math.sin(animTime) * 0.15);

    float[] black = new float[]{0.10f, 0.10f, 0.10f};
    float[] pinkDark = new float[]{0.75f, 0.75f, 0.00f};
    float[] leg = new float[]{0.9f, 0.5f, 0.7f};

    //body
    VoxelRender.drawBox(
      0.1f, 0.1f, 0.0f,
      0.9f, 1.2f, 0.6f,
      black
    );

    //head
    VoxelRender.drawBox(
      0.25f, 1.05f, -0.15f,
      0.75f, 1.65f, 0.45f,
      black
    );

    //snout
    VoxelRender.drawBox(
      0.40f, 1.20f, -1.25f,
      0.60f, 1.10f, -0.60f,
      pinkDark
    );

    //legs
    float lx0 = 0.10f, lx1= 0.35f;
    float rx0 = 0.65f, rx1 = 0.90f;

    float frontZ0 = 0.10f, frontZ1 = 0.35f;
    float backZ0 = 1.65f, backZ1 = 1.90f;

    float y0 = 0.0f;
    float y1 = 0.9f;

    //Front left
    VoxelRender.drawBox(
      lx0, y0, frontZ0,
      lx1, y1, frontZ1,
      leg
    );

    //front right
    VoxelRender.drawBox(
      rx0, y0, frontZ0,
      rx1, y1, frontZ1,
      leg
    );

    //back left
    VoxelRender.drawBox(
      lx0, y0, backZ0,
      lx1, y1, backZ1,
      leg
    );

    //back right
    VoxelRender.drawBox(
      rx0, y0, backZ0,
      rx1, y1, backZ1,
      leg
    );

    glPopAttrib();
    glPopMatrix();
  }

  private static float wrapAngleDeg(float a) {
    while(a >= 180f) a -= 360f;
    while(a < -180f) a += 360f;
    return a;
  }
}
