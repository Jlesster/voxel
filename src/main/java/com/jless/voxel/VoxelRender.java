package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

public class VoxelRender {

  public static void render(BlockMap map) {
    for(int x = 0; x < map.sizeX(); x++) {
      for(int y = 0; y < map.sizeY(); y++) {
        for(int z = 0; z < map.sizeZ(); z++) {
          byte id = map.get(x, y, z);
          if(!Blocks.SOLID[id]) continue;

          for(int face = 0; face < 6; face++) {
            if(VoxelCuller.isFaceVisible(map, x, y, z, face)) {
              drawFace(x, y, z, face);
            }
          }
        }
      }
    }
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
