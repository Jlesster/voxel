package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import org.joml.Vector3i;

public class VoxelRender {

  public static void render(World world) {
    for(Chunk chunk : world.getLoadedChunks()) {
      Vector3i cp = chunk.position;

      int baseX = cp.x * WorldConsts.CHUNK_SIZE;
      int baseZ = cp.z * WorldConsts.CHUNK_SIZE;

      BlockMap map = chunk.getBlockMap();

      for(int x = 0; x < map.sizeX(); x++) {
        for(int y = 0; y < map.sizeY(); y++) {
          for(int z = 0; z < map.sizeZ(); z++) {
            byte id = map.get(x, y, z);
            if(!Blocks.SOLID[id]) continue;

            float[] c = Blocks.COLOR[id];
            glColor3f(c[0], c[1], c[2]);

            int wx = baseX + x;
            int wz = baseZ + z;

            for(int face = 0; face < 6; face++) {
              if(VoxelCuller.isFaceVisible(map, x, y, z, face)) {
                drawFace(wx, y, wz, face);
              }
            }
          }
        }
      }
    }
  }

  public static void renderChunkGrid(BlockMap map) {
    glDisable(GL_LIGHTING);
    glColor3f(0f, 0f, 0f);

    int sizeX = map.sizeX();
    int sizeZ = map.sizeZ();

    for(int x = 0; x < sizeX; x += WorldConsts.CHUNK_SIZE) {
      glBegin(GL_LINES);
      glVertex3f(x, 0, 0);
      glVertex3f(x, 0, sizeZ);
      glEnd();
    }

    for(int z = 0; z <= sizeZ; z += WorldConsts.CHUNK_SIZE) {
      glBegin(GL_LINES);
      glVertex3f(0, 0, z);
      glVertex3f(sizeX, 0, z);
      glEnd();
    }
    glEnable(GL_LIGHTING);
  }

  private static void drawFace(int x, int y, int z, int face) {
    glBegin(GL_QUADS);

    switch (face) {
      case 0 -> { // +X
        glNormal3f(1, 0, 0);
        glVertex3f(x+1, y,   z);
        glVertex3f(x+1, y+1, z);
        glVertex3f(x+1, y+1, z+1);
        glVertex3f(x+1, y,   z+1);
      }
      case 1 -> { // -X
        glNormal3f(-1, 0, 0);
        glVertex3f(x, y,   z+1);
        glVertex3f(x, y+1, z+1);
        glVertex3f(x, y+1, z);
        glVertex3f(x, y,   z);
      }
      case 2 -> { // +Y
        glNormal3f(0, 1, 0);
        glVertex3f(x,   y+1, z+1);
        glVertex3f(x+1, y+1, z+1);
        glVertex3f(x+1, y+1, z);
        glVertex3f(x,   y+1, z);
      }
      case 3 -> { // -Y
        glNormal3f(0, -1, 0);
        glVertex3f(x,   y, z);
        glVertex3f(x+1, y, z);
        glVertex3f(x+1, y, z+1);
        glVertex3f(x,   y, z+1);
      }
      case 4 -> { // +Z
        glNormal3f(0, 0, 1);
        glVertex3f(x+1, y,   z+1);
        glVertex3f(x+1, y+1, z+1);
        glVertex3f(x,   y+1, z+1);
        glVertex3f(x,   y,   z+1);
      }
      case 5 -> { // -Z
        glNormal3f(0, 0, -1);
        glVertex3f(x,   y,   z);
        glVertex3f(x,   y+1, z);
        glVertex3f(x+1, y+1, z);
        glVertex3f(x+1, y,   z);
      }
    }

    glEnd();
  }
}
