package com.jless.voxel;

public class GenerateTerrain {

  public static void fillChunk(Chunk chunk, World world) {
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
    World.applyQueuedBlocks(chunk);
    populateTrees(chunk, world);
  }

  private static void populateTrees(Chunk c, World world) {
    BlockMap map = c.getBlockMap();

    int cx = c.position.x;
    int cz = c.position.z;

    int seed = (int)WorldConsts.WORLD_SEED;

    for(int x = 0; x < map.sizeX(); x++) {
      for(int z = 0; z < map.sizeZ(); z++) {
        int wx = cx * WorldConsts.CHUNK_SIZE + x;
        int wz = cz * WorldConsts.CHUNK_SIZE + z;

        float r = hash01(wx, wz, seed);
        if(r > WorldConsts.TREE_CHANCE) continue;

        int surfY = findSurfaceY(map, x, z);
        if(surfY < 1) continue;

        if(map.get(x, surfY, z) != BlockID.GRASS) continue;
        if(map.get(x, surfY - 1, z) == BlockID.AIR) continue;

        int h = WorldConsts.TREE_MIN_HEIGHT + (int)(hash01(wx + 99, wz - 99, seed) * (WorldConsts.TREE_MAX_HEIGHT - WorldConsts.TREE_MIN_HEIGHT + 1));

        if(surfY + h + 4 >= map.sizeY()) continue;
        placeTree(world, c,x, surfY + 1, z, h);
      }
    }
  }

  private static void placeTree(World world, Chunk chunk,int x, int y, int z, int height) {
    BlockMap map = chunk.getBlockMap();
    for(int i = 0; i < height; i++) {
      map.set(x, y + i, z, BlockID.LOG);
    }

    int wx = chunk.position.x * WorldConsts.CHUNK_SIZE + x;
    int wz = chunk.position.z * WorldConsts.CHUNK_SIZE + x;
    int top = y + height;
    int centerY = top + 1;

    int r = WorldConsts.TREE_CANOPY_RADS;
    for(int dx = -r; dx <= r; dx++) {
      for(int dz = -r; dz < r; dz++) {
        for(int dy = -r; dy < r; dy++) {
          int xx = x + dx;
          int yy = centerY + dy;
          int zz = z + dz;

          if(xx < 0 || zz < 0 || yy < 0) continue;
          if(xx >= map.sizeX() || zz >= map.sizeZ() || yy >= map.sizeY()) continue;

          int dist2 = (dx * dx) + (dy * dy) + (dz * dz);
          if(dist2 > (r * r) + 1) continue;

          if(map.get(xx, yy, zz) == BlockID.AIR) {
            map.set(xx, yy, zz, BlockID.LEAF);
          }
        }
      }
    }
  }

  private static void setBlockWorld(World w, int wx, int wy, int wz, byte id) {
    int cx = Math.floorDiv(wx, WorldConsts.CHUNK_SIZE);
    int cz = Math.floorDiv(wz, WorldConsts.CHUNK_SIZE);

    int lx = Math.floorDiv(wx, WorldConsts.CHUNK_SIZE);
    int lz = Math.floorDiv(wz, WorldConsts.CHUNK_SIZE);

    Chunk chunk = w.getChunkIfLoaded(cx, cz);
    if(chunk == null) {
      w.queueBlock(wx, wy, wz, id);
      return;
    }
    chunk.getBlockMap().set(lx, wy, wz, id);
    chunk.markDirty();
  }

  private static int findSurfaceY(BlockMap map, int x, int z) {
    for(int y = map.sizeY() - 2; y >= 1; y--) {
      if(map.get(x, y, z) != BlockID.AIR) return y;
    }
    return -1;
  }

  private static float getTerrainHeight(int x, int z) {
    float height = WorldConsts.BASE_HEIGHT;
    float amp = WorldConsts.HEIGHT_AMP;
    float freq = WorldConsts.SCALE;

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

  private static int hash(int x, int z, int seed) {
    int h = seed;
    h ^= x * 223134194;
    h ^= z * 324834899;
    h = (h ^ (h >> 13)) * 1252131123;
    return h ^ (h >> 16);
  }

  private static float  hash01(int x, int z, int seed) {
    int h = hash(x, z, seed);
    return (h & 0x7fffffff) / (float)0x7fffffff;
  }
}
