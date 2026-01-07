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

  public boolean isBlockSolid(int wx, int wy, int wz) {
    int chunkX = floorDiv(wx, Chunk.CHUNK_SIZE);
    int chunkY = floorDiv(wy, Chunk.CHUNK_SIZE);
    int chunkZ = floorDiv(wz, Chunk.CHUNK_SIZE);

    Chunk chunk = getChunk(chunkX, chunkY, chunkZ);
    if(chunk == null) return false;

    int lx = mod(wx, Chunk.CHUNK_SIZE);
    int ly = mod(wy, Chunk.CHUNK_SIZE);
    int lz = mod(wz, Chunk.CHUNK_SIZE);

    return chunk.isLocalBlockSolid(lx, ly, lz);
  }

  public void generateTerra(int radius) {
    TerrainGenerator gen = new SimpleNoiseGen();
    for(int x = -radius; x <= radius; x++) {
      for(int z = -radius; z <= radius; z++) {
        Chunk c = new Chunk(x, 0, z);
        c.generateBlocks(gen);
        chunks.put(chunkKey(x, 0, z), c);
      }
    }
    for(Chunk c : chunks.values()) {
      c.generateMesh(this);
    }
  }

  public Chunk getChunk(int cx, int cy, int cz) {
    return chunks.get(chunkKey(cx, cy, cz));
  }

  private int floorDiv(int a, int b) {
    return (int)Math.floor((double)a / b);
  }

  private int mod(int a, int b) {
    int m = a % b;
    return m < 0 ? m + b : m;
  }

  public void render() {
    for(Chunk c : chunks.values()) {
      c.render();
    }
  }
}
