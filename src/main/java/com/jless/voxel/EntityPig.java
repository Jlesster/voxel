package com.jless.voxel;

import org.joml.Vector3f;

public class EntityPig extends Entity {

  private float wanderTimer = 0f;
  private float idleTimer = 0f;

  private final Vector3f wishDir = new Vector3f();

  public EntityPig(float x, float y, float z) {
    pos.set(x, y, z);
  }

  @Override
  public void update(World world, float dt) {
    vel.y -= 20.0f * dt;

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

    pos.x += vel.x * dt;
    pos.y += vel.y * dt;
    pos.z += vel.z * dt;

    int bx = (int)Math.floor(pos.x);
    int by = (int)Math.floor(pos.y);
    int bz = (int)Math.floor(pos.z);

    byte below = world.getBlockWorld(bx, by, bz);
    if(Blocks.SOLID[below] && vel.y < 0) {
      pos.y = (float)by + 1.5f;
      vel.y = 0;
    }

    byte inside = world.getBlockWorld(bx, by, bz);
    if(Blocks.SOLID[inside]) {
      pos.x -= vel.x * dt;
      pos.z -= vel.z * dt;
      wanderTimer = 0;
    }
  }

  @Override
  public void render(VoxelRender render) {
    int px = (int)Math.floor(pos.x);
    int py = (int)Math.floor(pos.y);
    int pz = (int)Math.floor(pos.z);

    System.out.println("pig render at " + pos);

    VoxelRender.debugVoxel(px, py, pz, new float[]{1.0f, 0.6f, 0.8f});
    VoxelRender.debugVoxel(px + 1, py, pz, new float[]{1.0f, 0.6f, 0.8f});
    VoxelRender.debugVoxel(px + 2, py, pz, new float[]{1.0f, 0.5f, 0.7f});
  }
}
