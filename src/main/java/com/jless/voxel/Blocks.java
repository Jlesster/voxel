package com.jless.voxel;

public class Blocks {
  private Blocks() {}

  public static final boolean[] SOLID = new boolean[256];

  static {
    SOLID[BlockID.AIR] = false;
    SOLID[BlockID.GRASS] = true;
    SOLID[BlockID.DIRT] = true;
    SOLID[BlockID.STONE] = true;
  }
}
