package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class Chunk {
  //texture mapping
  public static final byte AIR = 0;
  public static final byte DIRT = 1;
  public static final byte GRASS = 2;
  public static final byte STONE = 3;

  //normal mapping
  private static final int FACE_FRONT = 0;
  private static final int FACE_BACK = 1;
  private static final int FACE_LEFT = 2;
  private static final int FACE_RIGHT = 3;
  private static final int FACE_TOP = 4;
  private static final int FACE_BOTTOM = 5;

  //size of the rendered chunk cubed
  public static final int CHUNK_SIZE = 32;

  //storing block data in memory as bytes
  private byte[][][] blocks = new byte[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];

  private VoxelMesh mesh;
  private int chunkX, chunkY, chunkZ;

  public Chunk(int chunkX, int chunkY, int chunkZ) {
    this.chunkX = chunkX;
    this.chunkY = chunkY;
    this.chunkZ = chunkZ;
    generateBlocks();
    generateMesh();
  }

  private void generateMesh() {
    List<Float> vertices = new ArrayList<>();

    for(int x = 0; x < CHUNK_SIZE; x++) {
      for(int y = 0; y < CHUNK_SIZE; y++) {
        for(int z = 0; z < CHUNK_SIZE; z++) {
          if(isVoxelSolid(x, y, z)) {
            addVisibleFaces(vertices, x, y, z, blocks[x][y][z]); //face culling in this
            // addCube(vertices, x, y, z); //this is for a big fucky block with no face culling
          }
        }
      }
    }
    float[] vertArray = new float [vertices.size()];
    for(int i = 0; i < vertices.size(); i++) {
      vertArray[i] = vertices.get(i);
    }
    mesh = new VoxelMesh(vertArray);
  }

  private void generateBlocks() {
    for(int x = 0; x < CHUNK_SIZE; x++) {
      for(int y = 0; y < CHUNK_SIZE; y++) {
        for(int z = 0; z < CHUNK_SIZE; z++) {
          if(y == CHUNK_SIZE - 1) {
            blocks[x][y][z] = GRASS;
          } else if(y > CHUNK_SIZE - 4) {
            blocks[x][y][z] = DIRT;
          } else {
            blocks[x][y][z] = STONE;
          }
        }
      }
    }
  }

  public void render() {
    glPushMatrix();
    glTranslatef(
      chunkX * CHUNK_SIZE,
      chunkY * CHUNK_SIZE,
      chunkZ * CHUNK_SIZE
    );
    mesh.render();
    glPopMatrix();
  }

  public Vector3f getCenter() {
    return new Vector3f(
      chunkX * CHUNK_SIZE + CHUNK_SIZE / 2.0f,
      chunkY * CHUNK_SIZE + CHUNK_SIZE / 2.0f,
      chunkZ * CHUNK_SIZE + CHUNK_SIZE / 2.0f
    );
  }

  private float[] getBlockColor(byte blockID, int face) {
    switch(blockID) {
      case GRASS:
        if(face == 4) return new float[]{0.2f, 0.8f, 0.2f, 1.0f};
        if(face == 5) return new float[]{0.4f, 0.3f, 0.1f, 1.0f};
        return new float[]{0.3f, 0.6f, 0.2f, 1.0f};

      case DIRT:
        return new float[]{0.5f, 0.35f, 0.2f, 1.0f};

      case STONE:
        return new float[]{0.6f, 0.6f, 0.6f, 1.0f};

      default:
        return new float[]{1, 0, 1, 1};
    }
  }

  private boolean isVoxelSolid(int x, int y, int z) {
    if(x < 0 || y < 0 || z < 0) return false;
    if(x >= CHUNK_SIZE || y >= CHUNK_SIZE || z >= CHUNK_SIZE) return false;
    return blocks[x][y][z] != AIR;
  }

  private void addVisibleFaces(List<Float> v, int x, int y, int z, byte blockID) {
    if(!isVoxelSolid(x, y, z + 1)) {
      float[] c = getBlockColor(blockID, FACE_FRONT);
      addQuad(v, 0, 0, 1, x, y, z + 1, x + 1, y, z + 1, x + 1, y + 1, z + 1, x, y + 1, z + 1, c[0], c[1], c[2], c[3]);
    }

    if(!isVoxelSolid(x, y, z - 1)) {
      float[] c = getBlockColor(blockID, FACE_BACK);
      addQuad(v, 0, 0, -1, x + 1, y, z, x, y, z, x, y + 1, z, x + 1, y + 1, z, c[0], c[1], c[2], c[3]);
    }

    if(!isVoxelSolid(x - 1, y, z)) {
      float[] c = getBlockColor(blockID, FACE_RIGHT);
      addQuad(v, -1, 0, 0, x, y, z, x, y, z + 1, x, y + 1, z + 1, x, y + 1, z, c[0], c[1], c[2], c[3]);
    }

    if(!isVoxelSolid(x + 1, y, z)) {
      float[] c = getBlockColor(blockID, FACE_LEFT);
      addQuad(v, 1, 0, 0, x + 1, y, z + 1, x + 1, y, z, x + 1, y + 1, z, x + 1, y + 1, z + 1, c[0], c[1], c[2], c[3]);
    }

    if(!isVoxelSolid(x, y + 1, z)) {
      float[] c = getBlockColor(blockID, FACE_TOP);
      addQuad(v, 0, 1, 0, x, y + 1, z + 1, x + 1, y + 1, z + 1, x + 1, y + 1, z, x, y + 1, z, c[0], c[1], c[2], c[3]);
    }

    if(!isVoxelSolid(x, y - 1, z)) {
      float[] c = getBlockColor(blockID, FACE_BOTTOM);
      addQuad(v, 0, -1, 0, x, y, z, x + 1, y, z, x + 1, y, z + 1, x, y, z + 1, c[0], c[1], c[2], c[3]);
    }
  }

  private void addCube(List<Float> v, float x, float y, float z) {
    //front
    addQuad(v, 0, 0, 1, x, y, z + 1, x + 1, y, z + 1, x + 1, y + 1, z + 1, x, y + 1, z + 1, 0, 1, 1, 0.6f);

    //back
    addQuad(v, 0, 0, -1, x + 1, y, z, x, y, z, x, y + 1, z, x + 1, y + 1, z, 1, 0, 1, 0.6f);

    //left
    addQuad(v, -1, 0, 0, x, y, z, x, y, z + 1, x, y + 1, z + 1, x, y + 1, z, 1, 1, 0, 0.6f);

    //right
    addQuad(v, 1, 0, 0, x + 1, y, z + 1, x + 1, y, z, x + 1, y + 1, z, x + 1, y + 1, z + 1, 0, 0, 1, 0.6f);

    //top
    addQuad(v, 0, 1, 0, x, y + 1, z + 1, x + 1, y + 1, z + 1, x + 1, y + 1, z, x, y + 1, z, 0, 1, 0, 0.6f);

    //bottom
    addQuad(v, 0, -1, 0, x, y, z, x + 1, y, z, x + 1, y, z + 1, x, y, z + 1, 1, 0, 0, 0.6f);
  }

  //creates one square per call
  private void addQuad(List<Float> v,
    float nx, float ny, float nz,
    float x1, float y1, float z1,
    float x2, float y2, float z2,
    float x3, float y3, float z3,
    float x4, float y4, float z4,
    float r, float g, float b, float a
  ) {
    //triangle 1
    addVertex(v, x1, y1, z1, nx, ny, nz, r, g, b, a);
    addVertex(v, x2, y2, z2, nx, ny, nz, r, g, b, a);
    addVertex(v, x3, y3, z3, nx, ny, nz, r, g, b, a);
    //triangle 2
    addVertex(v, x3, y3, z3, nx, ny, nz, r, g, b, a);
    addVertex(v, x4, y4, z4, nx, ny, nz, r, g, b, a);
    addVertex(v, x1, y1, z1, nx, ny, nz, r, g, b, a);
  }

  private void addVertex(List<Float> v, float x, float y, float z, float nx, float ny, float nz, float r, float g, float b, float a) {
    v.add(x);
    v.add(y);
    v.add(z);
    v.add(nx);
    v.add(ny);
    v.add(nz);
    v.add(r);
    v.add(g);
    v.add(b);
    v.add(a);
  }
}
