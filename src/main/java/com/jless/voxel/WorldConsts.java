package com.jless.voxel;

public class WorldConsts {
  private WorldConsts() {}

  public static final long WORLD_SEED = 1555L;
  public static final int WORLD_HEIGHT = 256;
  public static final int CHUNK_SIZE = 16;
  public static final int INIT_CHUNK_RADS = 16;

  public static final float SCALE = 0.002f;
  public static final int BASE_HEIGHT = 32;
  public static final int HEIGHT_AMP = 32;
  public static final int TERRAIN_OCTAVES = 5;

  public static final int CAVE_MAX_Y = 50;
  public static final float CAVE_FREQ = 0.01f;
  public static final float CAVE_FREQ_Y = 0.07f;
  public static final float CAVE_THRESH = 0.2f;

  public static final float TREE_CHANCE = 0.01f;
  public static final int TREE_MIN_HEIGHT = 3;
  public static final int TREE_MAX_HEIGHT = 6;
  public static final int TREE_CANOPY_RADS = 3;

  public static final int MAX_ENTITIES = 32;
  public static final float ENTITY_SPAWN_RADIUS = 20.0f;
  public static final float ENTITY_DESPAWN_RADIUS = 120.0f;
  public static final float ENTITY_SPAWN_INTERVAL = 0.5f;

  public static final float DAY_LENGTH = 240.0f; //4 mins
  public static final float AMBIENT_DAY = 0.6f;
  public static final float AMBIENT_NIGHT = 0.15f;

  public static final int SHADOW_MAP_SIZE = 2048;
  public static final float SHADOW_DISTANCE = 80.0f;
}
