package com.jless.voxel;

public class BlockTexture {

  private BlockTexture() {}

  public static final int ATLAS_TILES = 16;

  public static int tile(int tx, int ty) {
    return (ty << 8) | tx;
  }

  public static int tileX(int packed) { return packed & 0xFF; }
  public static int tileY(int packed) { return (packed >> 8) & 0xFF; }
}
