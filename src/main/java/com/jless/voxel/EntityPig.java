package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector3f;

public class EntityPig extends Entity {
  private static final float WIDTH = 0.8f;
  private static final float HEIGHT = 1.6f;
  private static final float LENGTH = 1.4f;
  private static final float STEP_HEIGHT = 1.1f;
  private static final float FEET_EPS = 0.001f;

  private float wanderTimer = 0f;
  private float idleTimer = 0f;
  private float animTime = 0f;
  private float yawDeg = 0f;
  private float targetYaw = 0f;

  private final Vector3f wishDir = new Vector3f();

  public EntityPig(float x, float y, float z) {
    pos.set(x, y, z);
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

  private int getWalkable(World world, int wx, int wz) {
    for(int y = WorldConsts.WORLD_HEIGHT - 1; y >= 0; y--) {
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

    int x0 = (int)Math.floor(x - rx);
    int x1 = (int)Math.floor(x + rx);
    int z0 = (int)Math.floor(z - rz);
    int z1 = (int)Math.floor(z + rz);

    int y00 = getWalkable(world, x0, z0);
    int y10 = getWalkable(world, x1, z0);
    int y01 = getWalkable(world, x0, z1);
    int y11 = getWalkable(world, x1, z1);

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
    float nx = pos.x + dx;
    float nz = pos.z + dz;
    boolean stepped = false;

    if(!hitboxBlocked(world, nx, pos.y, nz)) {
      pos.x = nx;
      pos.z = nz;
    } else {
      float stepY = pos.y + STEP_HEIGHT;
      if(!hitboxBlocked(world, nx, stepY, nz)) {
        float groundAfterStep = getGroundY(world, nx, pos.z);

        if(groundAfterStep > pos.y && groundAfterStep <= pos.y + STEP_HEIGHT + 0.01f) {
          pos.y = groundAfterStep;
          pos.x = nx;
          pos.z = nz;
          vel.y = 0;
          stepped = true;
        } else {
          vel.x = 0;
          vel.z = 0;
        }
      } else {
        vel.x = 0;
        vel.z = 0;
      }
    }
    if(!hitboxBlocked(world, pos.x, pos.y, nz)) {
      pos.z = nz;
    } else {
      float stepY = pos.y + STEP_HEIGHT;
      if(!hitboxBlocked(world, pos.x, stepY, nz)) {
        float groundAfterStep = getGroundY(world, pos.x, nz);
        if(groundAfterStep > pos.y && groundAfterStep <= pos.y + STEP_HEIGHT + 0.01f) {
          pos.y = groundAfterStep;
          pos.z = nz;
          vel.y = 0;
        }
      }
    }

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
    glTranslatef(-1.0f, 0.0f, -0.5f);
    glRotatef(yawDeg, 0f, 1f, 0f);

    glScalef(0.7f, 0.7f, 0.7f);

    glPushAttrib(GL_ENABLE_BIT | GL_CURRENT_BIT | GL_LIGHTING_BIT | GL_TEXTURE_BIT);
    glDisable(GL_TEXTURE_2D);
    glDisable(GL_CULL_FACE);
    glDisable(GL_ALPHA_TEST);
    glDisable(GL_BLEND);
    glDisable(GL_LIGHTING);

    float swing = (float) ((float)Math.sin(animTime) * 0.15);

    float[] pink = new float[]{1.0f, 0.65f, 0.80f};
    float[] pinkDark = new float[]{0.95f, 0.55f, 0.75f};
    float[] leg = new float[]{0.9f, 0.5f, 0.7f};

    //body
    VoxelRender.drawBox(
      0.0f, 0.9f, 0.0f,
      1.0f, 1.6f, 2.0f,
      pink
    );

    //head
    VoxelRender.drawBox(
      0.05f, 1.05f, -0.85f,
      0.95f, 1.75f, 0.05f,
      pink
    );

    //snout
    VoxelRender.drawBox(
      0.20f, 1.20f, -1.10f,
      0.80f, 1.50f, -0.85f,
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
      lx0, y0, frontZ0 + swing,
      lx1, y1, frontZ1 + swing,
      leg
    );

    //front right
    VoxelRender.drawBox(
      rx0, y0, frontZ0 + swing,
      rx1, y1, frontZ1 + swing,
      leg
    );

    //back left
    VoxelRender.drawBox(
      lx0, y0, backZ0 - swing,
      lx1, y1, backZ1 - swing,
      leg
    );

    //back right
    VoxelRender.drawBox(
      rx0, y0, backZ0 - swing,
      rx1, y1, backZ1 - swing,
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
