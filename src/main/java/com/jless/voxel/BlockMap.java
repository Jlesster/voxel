package com.jless.voxel;

public final class BlockMap {
  private final int sizeX, sizeY, sizeZ;
  private final byte[] blocks;

  public BlockMap(int sizeX, int sizeY, int sizeZ) {
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    this.sizeZ = sizeZ;
    this.blocks = new byte[sizeX * sizeY * sizeZ];
  }

  private int idx(int x, int y, int z) {
    return (x) + (sizeX * z) + (sizeX * sizeZ * y);
  }

  public boolean inBounds(int x, int y, int z) {
    return x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ;
  }

  public byte get(int x, int y, int z) {
    if(!inBounds(x, y, z)) return BlockID.AIR;
    return blocks[idx(x, y, z)];
  }

  public void set(int x, int y, int z, byte id) {
    if(!inBounds(x, y, z)) return;
    blocks[idx(x, y, z)] = id;
  }

  public int sizeX() { return sizeX; }
  public int sizeY() { return sizeY; }
  public int sizeZ() { return sizeZ; }
}
