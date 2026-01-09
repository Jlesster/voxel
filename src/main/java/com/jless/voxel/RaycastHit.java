package com.jless.voxel;

import org.joml.Vector3i;

public class RaycastHit {
  public final Vector3i block;
  public final Vector3i normal;

  public RaycastHit(Vector3i block, Vector3i normal) {
    this.block = block;
    this.normal = normal;
  }
}
