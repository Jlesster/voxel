package com.jless.voxel;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public abstract class Entity {

  public final Vector3f pos = new Vector3f();
  public final Vector3f vel = new Vector3f();

  public boolean removed = false;

  public abstract void update(World world, float dt);
  public abstract void render(VoxelRender render);

  public static Matrix4f withPivotRotationX(Matrix4f base, float px, float py, float pz, float angleRad) {
    return new Matrix4f(base)
      .translate(px, py, pz)
      .rotateX(angleRad)
      .translate(-px, -py, -pz);
  }
}
