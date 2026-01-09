package com.jless.voxel;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3i;

public class Chunk {
  public final Vector3i position;
  private final BlockMap blocks;

  private List<VoxelFace> mesh;
  private boolean dirty = true;

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
    dirty = true;
  }

  public BlockMap getBlockMap() {
    return blocks;
  }

  public void rebuildMesh(World world) {
    mesh = new ArrayList<>();

    BlockMap map = getBlockMap();

    for(int x = 0; x < map.sizeX(); x++) {
      for(int y = 0; y < map.sizeY(); y++) {
        for(int z = 0; z < map.sizeZ(); z++) {
          byte id = map.get(x, y, z);
          if(!Blocks.SOLID[id]) continue;

          int wx = position.x * WorldConsts.CHUNK_SIZE + x;
          int wz = position.z * WorldConsts.CHUNK_SIZE + z;

          for(int face = 0; face < 6; face++) {
            if(isFaceVisible(world, wx, y, wz, face)) {
              mesh.add(new VoxelFace(wx, y, wz, face, id));
            }
          }
        }
      }
    }
    dirty = false;
  }

  public List<VoxelFace> getMesh(World w) {
    if(dirty || mesh == null) {
      rebuildMesh(w);
    }
    return mesh;
  }

  public void markDirty() {
    dirty = true;
  }

  private boolean isFaceVisible(World w, int wx, int y, int wz, int face) {
    int[] d = VoxelCuller.DIRS[face];
    byte neighbor = w.getIfLoaded(
      wx + d[0],
      y + d[1],
      wz + d[2]
    );
    return !Blocks.SOLID[neighbor];
  }
}
