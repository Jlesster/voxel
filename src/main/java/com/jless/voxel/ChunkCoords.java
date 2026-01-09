package com.jless.voxel;

public class ChunkCoords {
  private ChunkCoords() {}

  public static int chunk(int worldCoord) {
    return Math.floorDiv(worldCoord, WorldConsts.CHUNK_SIZE);
  }

  public static int local(int worldCoord) {
    return Math.floorMod(worldCoord, WorldConsts.CHUNK_SIZE);
  }

}
