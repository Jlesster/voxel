package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import org.joml.FrustumIntersection;
import org.joml.Matrix4f;

public class VoxelRender {
  private static final FrustumIntersection frustum = new FrustumIntersection();
  private static final Matrix4f pv = new Matrix4f();

  public static void render(World world, Matrix4f proj, Matrix4f view) {
    pv.set(proj).mul(view);
    frustum.set(pv);

    for(Chunk chunk : world.getLoadedChunks()) {
      int minX = chunk.position.x * WorldConsts.CHUNK_SIZE;
      int minY = 0;
      int minZ = chunk.position.z * WorldConsts.CHUNK_SIZE;

      int maxX = minX + WorldConsts.CHUNK_SIZE;
      int maxY = WorldConsts.WORLD_HEIGHT;
      int maxZ = minZ + WorldConsts.CHUNK_SIZE;

      if(!frustum.testAab(minX, minY, minZ, maxX, maxY, maxZ)) continue;

      chunk.ensureUploaded(world);
      chunk.drawVBO();
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

  public static void drawBox(float x0, float y0, float z0, float x1, float y1, float z1, float[] rgb) {
    glColor3f(rgb[0], rgb[1], rgb[2]);

    glBegin(GL_QUADS);

    //r
    glNormal3f(1, 0, 0);
    glVertex3f(x1, y0, z0);
    glVertex3f(x1, y1, z0);
    glVertex3f(x1, y1, z1);
    glVertex3f(x1, y0, z1);

    //l
    glNormal3f(-1, 0, 0);
    glVertex3f(x0, y0, z1);
    glVertex3f(x0, y1, z1);
    glVertex3f(x0, y1, z0);
    glVertex3f(x0, y0, z0);

    //t
    glNormal3f(0, 1, 0);
    glVertex3f(x0, y1, z0);
    glVertex3f(x1, y1, z0);
    glVertex3f(x1, y1, z1);
    glVertex3f(x0, y1, z1);

    //b
    glNormal3f(0, -1, 0);
    glVertex3f(x0, y0, z1);
    glVertex3f(x1, y0, z1);
    glVertex3f(x1, y0, z0);
    glVertex3f(x0, y0, z0);

    //f
    glNormal3f(0, 0, 1);
    glVertex3f(x0, y0, z1);
    glVertex3f(x0, y1, z1);
    glVertex3f(x1, y1, z1);
    glVertex3f(x1, y0, z1);

    //ba
    glNormal3f(0, 0, -1);
    glVertex3f(x0, y0, z0);
    glVertex3f(x0, y1, z0);
    glVertex3f(x1, y1, z0);
    glVertex3f(x1, y0, z0);

    glEnd();
  }

  public static void debugVoxel(int x, int y, int z, float[] rgb) {
    glDisable(GL_LIGHTING);
    glDisable(GL_CULL_FACE);
    glColor3f(rgb[0], rgb[1], rgb[2]);
    glBegin(GL_QUADS);

    glNormal3f(1, 0, 0);
    glVertex3f(x + 1, y,     z);
    glVertex3f(x + 1, y + 1, z);
    glVertex3f(x + 1, y + 1, z + 1);
    glVertex3f(x + 1, y,     z + 1);

    glNormal3f(-1, 0, 0);
    glVertex3f(x, y,     z + 1);
    glVertex3f(x, y + 1, z + 1);
    glVertex3f(x, y + 1, z);
    glVertex3f(x, y,     z);

    glNormal3f(0, 1, 0);
    glVertex3f(x,     y + 1, z);
    glVertex3f(x + 1, y + 1, z);
    glVertex3f(x + 1, y + 1, z + 1);
    glVertex3f(x,     y + 1, z + 1);

    glNormal3f(0, -1, 0);
    glVertex3f(x,     y, z + 1);
    glVertex3f(x + 1, y, z + 1);
    glVertex3f(x + 1, y, z);
    glVertex3f(x,     y, z);

    glNormal3f(0, 0, 1);
    glVertex3f(x + 1, y,     z + 1);
    glVertex3f(x + 1, y + 1, z + 1);
    glVertex3f(x,     y + 1, z + 1);
    glVertex3f(x,     y,     z + 1);

    glNormal3f(0, 0, -1);
    glVertex3f(x,     y,     z);
    glVertex3f(x,     y + 1, z);
    glVertex3f(x + 1, y + 1, z);
    glVertex3f(x + 1, y,     z);

    glEnd();
    glEnable(GL_LIGHTING);
    glEnable(GL_CULL_FACE);
  }
}
