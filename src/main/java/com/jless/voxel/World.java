package com.jless.voxel;

import java.util.HashMap;
import java.util.Map;

public class World {

  private Map<Long, Chunk> chunks = new HashMap<>();

  private long chunkKey(int x, int y, int z) {
    return (((long)x & 0x3FFFFF) << 42)
        |  (((long)y & 0x3FFFFF) << 21)
        |  ((long)z & 0x3FFFFF);
  }

  public void generateFlat(int radius) {
    for(int x = -radius; x <= radius; x++) {
      for(int z = -radius; z <= radius; z++) {
        Chunk c = new Chunk(x, 0, z);
        chunks.put(chunkKey(x, 0, z), c);
      }
    }
  }

  public void render() {
    for(Chunk c : chunks.values()) {
      c.render();
    }
  }
}
