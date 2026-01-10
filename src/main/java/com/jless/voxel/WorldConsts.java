package com.jless.voxel;

public class WorldConsts {
  private WorldConsts() {}

  public static final long WORLD_SEED = 1555L;
  public static final int WORLD_HEIGHT = 256;
  public static final int CHUNK_SIZE = 16;
  public static final int INIT_CHUNK_RADS = 16;

  public static final float SCALE = 0.001f;
  public static final int BASE_HEIGHT = 32;
  public static final int HEIGHT_AMP = 16;
  public static final int TERRAIN_OCTAVES = 4;

  public static final int CAVE_MAX_Y = 50;
  public static final float CAVE_FREQ = 0.05f;
  public static final float CAVE_FREQ_Y = 0.05f;
  public static final float CAVE_THRESH = 0.15f;

  public static final float TREE_CHANCE = 0.02f;
  public static final int TREE_MIN_HEIGHT = 3;
  public static final int TREE_MAX_HEIGHT = 6;
  public static final int TREE_CANOPY_RADS = 2;
}
