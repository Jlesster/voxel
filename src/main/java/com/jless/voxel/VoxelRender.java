package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

public class VoxelRender {

  public static void render(World world) {
    for(Chunk chunk : world.getLoadedChunks()) {
      for(VoxelFace f : chunk.getMesh(world)) {
        float[] c = Blocks.COLOR[f.blockID];
        glColor3f(c[0], c[1], c[2]);

        drawFace(f.x, f.y, f.z, f.face);
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
