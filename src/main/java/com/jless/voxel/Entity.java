package com.jless.voxel;

import org.joml.Vector3f;

public abstract class Entity {

  public final Vector3f pos = new Vector3f();
  public final Vector3f vel = new Vector3f();

  public boolean removed = false;

  public abstract void update(World world, float dt);
  public abstract void render(VoxelRender render);
}
