package com.jless.voxel;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3i;

public class World {

  private final Map<Vector3i, Chunk> chunks = new HashMap<>();

  public Chunk getChunk(Vector3i chunkPos) {
    return chunks.computeIfAbsent(chunkPos, pos -> {
      Chunk c = new Chunk(pos);
      TestGen.fillChunk(c);
      return c;
    });
  }

  public byte get(int x, int y, int z) {
    Vector3i chunkPos = new Vector3i(
      ChunkCoords.chunk(x),
      0,
      ChunkCoords.chunk(z)
    );

    Chunk chunk = getChunk(chunkPos);

    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    return chunk.get(lx, y, lz);
  }

  public void set(int x, int y , int z, byte id) {
    Vector3i chunkPos = new Vector3i(
      ChunkCoords.chunk(x),
      0,
      ChunkCoords.chunk(z)
    );

    Chunk chunk = getChunk(chunkPos);

    int lx = ChunkCoords.local(x);
    int lz = ChunkCoords.local(z);

    chunk.set(lx,y,lz, id);
  }

  public Iterable<Chunk> getLoadedChunks() {
    return chunks.values();
  }
}
