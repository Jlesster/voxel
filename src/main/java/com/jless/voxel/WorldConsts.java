package com.jless.voxel;

public class WorldConsts {
  private WorldConsts() {}

  public static final long WORLD_SEED = 1555L;
  public static final int WORLD_HEIGHT = 256;
  public static final int CHUNK_SIZE = 16;
  public static final int INIT_CHUNK_RADS = 32;

  public static final float scale = 0.005f;
  public static final int baseHeight = 32;
  public static final int heightAmp = 16;
  public static final int TERRAIN_OCTAVES = 4;

  public static final int CAVE_MAX_Y = 50;
  public static final float CAVE_FREQ = 0.05f;
  public static final float CAVE_FREQ_Y = 0.05f;
  public static final float CAVE_THRESH = 0.15f;
}
