package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

public class Chunk {
  public static final int CHUNK_SIZE = 16;

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
            addVisibleFaces(vertices, x, y, z);
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

  // public void renderVisibleChunks(Controller c) {
  //   Vector3f cameraPos = c.getPosition();
  //
  //   for(Chunk chunk : chunks) {
  //     if(isChunkInFrustum(chunk, cameraPos)) {
  //       chunk.render();
  //     }
  //   }
  // }
  //
  // private boolean isChunkInFrustum(Chunk chunk, Vector3f cameraPos) {
  //   Vector3f chunkCenter = chunk.getCenter();
  //   float distance = cameraPos.distance(chunkCenter);
  //
  //   return distance < renderDistance;
  // }
}
