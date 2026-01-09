package com.jless.voxel;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3i;

public class World {

  private final Map<Long, Chunk> chunks = new HashMap<>();

  public static final SimplexNoise NOISE = new SimplexNoise(1337L);

  public Chunk getChunk(int cx, int cz) {
    long key = chunkKey(cx, cz);
    Chunk c = chunks.get(key);
    if(c != null) return c;

    c = new Chunk(new Vector3i(cx, 0, cz));
    TestGen.fillChunk(c);
    chunks.put(key, c);
    return c;
  }

  public void generateSpawn() {
    int r = WorldConsts.INIT_CHUNK_RADS;

    for(int cx = -r; cx <= r; cx++) {
      for(int cz = -r; cz <= r; cz++) {
        getChunk(cx, cz);
      }
    }
  }

  public byte getIfLoaded(int x, int y, int z) {
    int cx = ChunkCoords.chunk(x);
    int cz = ChunkCoords.chunk(z);

    Chunk chunk = chunks.get(chunkKey(cx, cz));
    if(chunk == null) {
      return BlockID.AIR;
    }
    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    return chunk.get(lx, y, lz);
  }

  public byte get(int x, int y, int z) {
    int cx = ChunkCoords.chunk(x);
    int cz = ChunkCoords.chunk(z);

    Chunk chunk = getChunk(cx, cz);

    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    return chunk.get(lx, y, lz);
  }

  public void set(int x, int y , int z, byte id) {
    int cx = ChunkCoords.chunk(x);
    int cz = ChunkCoords.chunk(z);

    Chunk chunk = getChunk(cx, cz);

    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    chunk.set(lx, y, lz, id);
    chunk.markDirty();

    if(lx == 0) {
      markNeighborDirty(cx - 1, cz);
    }
    if(lx == WorldConsts.CHUNK_SIZE - 1) {
      markNeighborDirty(cx + 1, cz);
    }
    if(lz == 0) {
      markNeighborDirty(cx, cz - 1);
    }
    if(lz == WorldConsts.CHUNK_SIZE - 1) {
      markNeighborDirty(cx, cz + 1);
    }
  }

  private static long chunkKey(int cx, int cz) {
    return (((long)cx) << 32) ^ (cz & 0xffffffffL);
  }

  private void markNeighborDirty(int x, int z) {
    Chunk neighbor = chunks.get(chunkKey(x, z));
    if(neighbor != null) {
      neighbor.markDirty();
    }
  }

public Iterable<Chunk> getLoadedChunks() {
    return chunks.values();
  }
}
