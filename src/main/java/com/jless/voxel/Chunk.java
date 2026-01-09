package com.jless.voxel;

import org.joml.Vector3i;

public class Chunk {
  public final Vector3i position;
  private final BlockMap blocks;

  public Chunk(Vector3i pos) {
    this.position = new Vector3i(pos);
    this.blocks = new BlockMap(
      WorldConsts.CHUNK_SIZE,
      WorldConsts.WORLD_HEIGHT,
      WorldConsts.CHUNK_SIZE
    );
  }

  public byte get(int x, int y, int z) {
    return blocks.get(x, y, z);
  }

  public void set(int x, int y, int z, byte id) {
    blocks.set(x, y, z, id);
  }

  public BlockMap getBlockMap() {
    return blocks;
  }
}
