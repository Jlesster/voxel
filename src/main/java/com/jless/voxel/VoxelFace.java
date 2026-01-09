package com.jless.voxel;

public class VoxelFace {
  public final int x, y, z;
  public final int face;
  public final byte blockID;

  public VoxelFace(int x, int y, int z, int face, byte blockID) {
    this.x = x;
    this.y = y;
    this.z = z;
    this.face = face;
    this.blockID = blockID;
  }
}
