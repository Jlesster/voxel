package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class Chunk {
  public static final int CHUNK_SIZE = 8;

  private VoxelMesh mesh;
  private int chunkX, chunkY, chunkZ;

  public Chunk(int chunkX, int chunkY, int chunkZ) {
    this.chunkX = chunkX;
    this.chunkY = chunkY;
    this.chunkZ = chunkZ;
    generateMesh();
  }

  private void generateMesh() {
    List<Float> vertices = new ArrayList<>();

    for(int x = 0; x < CHUNK_SIZE; x++) {
      for(int y = 0; y < CHUNK_SIZE; y++) {
        for(int z = 0; z < CHUNK_SIZE; z++) {
          if(isVoxelSolid(x, y, z)) {
            addCube(vertices, x, y, z);
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

  public void render() {
    glColor3f(0.7f, 0.7f, 0.7f);
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

  private boolean isVoxelSolid(int x, int y, int z) {
    //TODO add logic
    return true;
  }

  private void addVisibleFaces(List<Float> vertices, int x, int y, int z) {
    //TODO face rendering stuff
  }

  private void addCube(List<Float> v, float x, float y, float z) {
    //front
    addQuad(v, 0, 0, 1, x, y, z + 1, x + 1, y, z + 1, x + 1, y + 1, z + 1, x, y + 1, z + 1);

    //back
    addQuad(v, 0, 0, -1, x + 1, y, z, x, y, z, x, y + 1, z, x + 1, y + 1, z);

    //left
    addQuad(v, -1, 0, 0, x, y, z, x, y, z + 1, x, y + 1, z + 1, x, y + 1, z);

    //right
    addQuad(v, 1, 0, 0, x + 1, y, z + 1, x + 1, y, z, x + 1, y + 1, z, x + 1, y + 1, z + 1);

    //top
    addQuad(v, 0, 1, 0, x, y + 1, z + 1, x + 1, y + 1, z + 1, x + 1, y + 1, z, x, y + 1, z);

    //bottom
    glNormal3f(0, -1, 0);
    addQuad(v, 0, -1, 0, x, y, z, x + 1, y, z, x + 1, y, z + 1, x, y, z + 1);
  }

  //creates one square per call
  private void addQuad(List<Float> v,
    float nx, float ny, float nz,
    float x1, float y1, float z1,
    float x2, float y2, float z2,
    float x3, float y3, float z3,
    float x4, float y4, float z4
  ) {
    //triangle 1
    addVertex(v, x1, y1, z1, nx, ny, nz);
    addVertex(v, x2, y2, z2, nx, ny, nz);
    addVertex(v, x3, y3, z3, nx, ny, nz);
    //triangle 2
    addVertex(v, x3, y3, z3, nx, ny, nz);
    addVertex(v, x4, y4, z4, nx, ny, nz);
    addVertex(v, x1, y1, z1, nx, ny, nz);
  }

  private void addVertex(List<Float> v, float x, float y, float z, float nx, float ny, float nz) {
    v.add(x);
    v.add(y);
    v.add(z);
    v.add(nx);
    v.add(ny);
    v.add(nz);
  }

  // public void renderVisibleChunks(Controller c) {
  //   Vector3f cameraPos = c.getPosition();
  //
  //   for(Chunk chunk : chunks) {
  //     if(isChunkInFrustum(chunk, cameraPos)) {
  //       chunk.render();
  //     }
  //   }
  // }

  // private boolean isChunkInFrustum(Chunk chunk, Vector3f cameraPos) {
  //   Vector3f chunkCenter = chunk.getCenter();
  //   float distance = cameraPos.distance(chunkCenter);
  //
  //   return distance < renderDistance;
  // }
}
