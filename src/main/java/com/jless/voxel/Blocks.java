package com.jless.voxel;

public class Blocks {
  private Blocks() {}

  public static final boolean[] SOLID = new boolean[256];
  public static final float[][] COLOR = new float[256][3];

  static {
    SOLID[BlockID.AIR] = false;
    SOLID[BlockID.GRASS] = true;
    SOLID[BlockID.DIRT] = true;
    SOLID[BlockID.STONE] = true;

    COLOR[BlockID.GRASS] = new float[] {0.3f, 0.8f, 0.3f};
    COLOR[BlockID.DIRT] = new float[] {0.5f, 0.3f, 0.2f};
    COLOR[BlockID.STONE] = new float[] {0.4f, 0.4f, 0.4f};
  }
}
