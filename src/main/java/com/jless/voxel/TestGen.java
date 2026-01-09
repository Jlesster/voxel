package com.jless.voxel;

public class TestGen {
  public static void fill(BlockMap map) {
    int groundY = 8;

    for(int x = 0; x < map.sizeX(); x++) {
      for(int y = 0; y < map.sizeY(); y++) {
        for(int z = 0; z < map.sizeZ(); z++) {
          if(y < groundY - 3) map.set(x, y, z, BlockID.STONE);
          else if(y < groundY) map.set(x, y, z, BlockID.DIRT);
          else if(y == groundY) map.set(x, y, z, BlockID.GRASS);
          else map.set(x, y, z, BlockID.AIR);
        }
      }
    }

    for(int y = groundY + 1; y < groundY + 10; y++) {
      map.set(
        map.sizeX() / 2,
        y,
        map.sizeZ() / 2,
        BlockID.STONE);
    }
  }

  public static void fillChunk(Chunk chunk) {
    BlockMap map = chunk.getBlockMap();

    int chunkX = chunk.position.x;
    int chunkZ = chunk.position.z;

    float scale = 0.05f;
    int baseHeight = 8;
    int heightAmp = 10;

    for(int x = 0; x < map.sizeX(); x++) {
      for(int z = 0; z < map.sizeZ(); z++) {
        int wx = chunkX * WorldConsts.CHUNK_SIZE + x;
        int wz = chunkZ * WorldConsts.CHUNK_SIZE + z;

        float n = World.NOISE.noise2D(
          wx * scale,
          wz * scale
        );

        int height = baseHeight + (int)(n * heightAmp);

        for(int y = 0; y < map.sizeY(); y++) {
          if(y < height - 3) map.set(x, y, z, BlockID.STONE);
          else if(y < height) map.set(x, y, z, BlockID.DIRT);
          else if(y == height) map.set(x, y, z, BlockID.GRASS);
          else map.set(x, y, z, BlockID.AIR);
        }
      }
    }
  }

}
