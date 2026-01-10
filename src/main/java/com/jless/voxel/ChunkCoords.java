package com.jless.voxel;

public class ChunkCoords {
  public int cx;
  public int cz;

  public ChunkCoords(int cx, int cz) {
    this.cx = cx;
    this.cz = cz;
  }

  public static int chunk(int worldCoord) {
    return Math.floorDiv(worldCoord, WorldConsts.CHUNK_SIZE);
  }

  public static int local(int worldCoord) {
    return Math.floorMod(worldCoord, WorldConsts.CHUNK_SIZE);
  }

}
