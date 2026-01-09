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

}
