package com.jless.voxel;

public class Blocks {
  private Blocks() {}

  public static final boolean[] SOLID = new boolean[256];
  public static final float[][] COLOR = new float[256][3];
  public static final int[] TEX_TOP = new int[256];
  public static final int[] TEX_SIDE = new int[256];
  public static final int[] TEX_BOTTOM = new int[256];

  static {
    SOLID[BlockID.AIR] = false;
    SOLID[BlockID.GRASS] = true;
    SOLID[BlockID.DIRT] = true;
    SOLID[BlockID.STONE] = true;

    SOLID[BlockID.LOG] = true;
    SOLID[BlockID.LEAF] = true;

    COLOR[BlockID.GRASS] = new float[] {0.3f, 0.8f, 0.3f};
    COLOR[BlockID.DIRT] = new float[] {0.5f, 0.3f, 0.2f};
    COLOR[BlockID.STONE] = new float[] {0.4f, 0.4f, 0.4f};
    COLOR[BlockID.LOG] = new float[] {0.55f, 0.27f, 0.07f};
    COLOR[BlockID.LEAF] = new float[] {0.10f, 0.55f, 0.10f};

    TEX_TOP[BlockID.GRASS] = BlockTexture.tile(1, 0);
    TEX_SIDE[BlockID.GRASS] = BlockTexture.tile(2, 0);
    TEX_BOTTOM[BlockID.GRASS] = BlockTexture.tile(3, 0);

    TEX_TOP[BlockID.STONE] = BlockTexture.tile(0, 0);
    TEX_SIDE[BlockID.STONE] = BlockTexture.tile(0, 0);
    TEX_BOTTOM[BlockID.STONE] = BlockTexture.tile(0, 0);

    TEX_TOP[BlockID.DIRT] = BlockTexture.tile(3, 0);
    TEX_SIDE[BlockID.DIRT] = BlockTexture.tile(3, 0);
    TEX_BOTTOM[BlockID.DIRT] = BlockTexture.tile(3, 0);

    TEX_TOP[BlockID.LOG] = BlockTexture.tile(5, 1);
    TEX_SIDE[BlockID.LOG] = BlockTexture.tile(4, 1);
    TEX_BOTTOM[BlockID.LOG] = BlockTexture.tile(5, 1);

    TEX_TOP[BlockID.LEAF] = BlockTexture.tile(3, 1);
    TEX_SIDE[BlockID.LEAF] = BlockTexture.tile(3, 1);
    TEX_BOTTOM[BlockID.LEAF] = BlockTexture.tile(3, 1);
  }
}
