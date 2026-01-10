package com.jless.voxel;

public class GenerateTerrain {

  public static void fillChunk(Chunk chunk) {
    BlockMap map = chunk.getBlockMap();

    int chunkX = chunk.position.x;
    int chunkZ = chunk.position.z;

    for(int x = 0; x < map.sizeX(); x++) {
      for(int z = 0; z < map.sizeZ(); z++) {
        int wx = chunkX * WorldConsts.CHUNK_SIZE + x;
        int wz = chunkZ * WorldConsts.CHUNK_SIZE + z;

        float height = getTerrainHeight(wx, wz);
        int terraHeight = Math.round(height);

        for(int y = 0; y < map.sizeY(); y++) {
          byte blockType = BlockID.AIR;

          if(y > terraHeight) {
            blockType = BlockID.AIR;
          } else if(y == terraHeight) {
            blockType = BlockID.GRASS;
          } else if(y < terraHeight && y >= terraHeight - 3) {
            blockType = BlockID.DIRT;
          } else if(y < terraHeight - 3) {
            blockType = BlockID.STONE;

            if(shouldCarveCave(wx, y, wz, terraHeight, chunk)) {
              blockType = BlockID.AIR;
            }
          }
          map.set(x, y, z, blockType);
        }
      }
    }
  }

  private static float getTerrainHeight(int x, int z) {
    float height = WorldConsts.baseHeight;
    float amp = WorldConsts.heightAmp;
    float freq = WorldConsts.scale;

    for(int octave = 0; octave < WorldConsts.TERRAIN_OCTAVES; octave++) {
      float n = World.NOISE.noise2D(x * freq, z * freq);
      height += n * amp;

      freq *= 2.0f;
      amp *= 0.5f;
    }
    return height;
  }

  private static boolean shouldCarveCave(int x, int y, int z, int surfHeight, Chunk chunk) {
    if(y <= 2 || y >= WorldConsts.CAVE_MAX_Y) return false;
    if(y > surfHeight - 4) return false;

    float n1 = SimplexNoise.noise(
      x * WorldConsts.CAVE_FREQ,
      y * WorldConsts.CAVE_FREQ,
      z * WorldConsts.CAVE_FREQ
    );

    float n2 = SimplexNoise.noise(
      (x + 100) * WorldConsts.CAVE_FREQ,
      (y + 100) * WorldConsts.CAVE_FREQ,
      (z + 100) * WorldConsts.CAVE_FREQ
    );

    float r1 = 1.0f - Math.abs(n1);
    float r2 = 1.0f - Math.abs(n2);

    float combined = r1 * r2;

    float thickness = SimplexNoise.noise(
      x * WorldConsts.CAVE_FREQ * 0.5f,
      y * WorldConsts.CAVE_FREQ_Y * 0.5f,
      z * WorldConsts.CAVE_FREQ * 0.5f
    );

    thickness = (thickness + 1.0f) * 0.5f;

    float dynamicThresh = WorldConsts.CAVE_THRESH + (thickness * 1.0f);

    return combined > dynamicThresh;
  }

}
