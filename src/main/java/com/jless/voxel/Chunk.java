package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import org.joml.Vector3i;

public class Chunk {
  public final Vector3i position;
  private final BlockMap blocks;

  private boolean dirty = true;
  private boolean uploaded = false;
  private int vertexCount = 0;
  private int vboID = 0;

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

  private FloatBuffer buildMeshBuffer(World w) {
    int initFloats = 800_000;
    FloatBuffer buf = BufferUtils.createFloatBuffer(initFloats);

    BlockMap map = getBlockMap();

    int baseX = position.x * WorldConsts.CHUNK_SIZE;
    int baseZ = position.z * WorldConsts.CHUNK_SIZE;

    int count = 0;

    for(int x = 0; x < map.sizeX(); x++) {
      for(int y = 0; y < map.sizeY(); y++) {
        for(int z = 0; z < map.sizeZ(); z++) {
          byte id = map.get(x, y, z);
          if(!Blocks.SOLID[id]) continue;

          int wx = baseX + x;
          int wz = baseZ + z;

          for(int face = 0; face < 6; face++) {
            if(isFaceVisible(w, wx, y, wz, face)) {
              count += emitFaceAsTriangles(buf, wx, y, wz, face, id);
            }
          }
        }
      }
    }
    vertexCount = count;
    buf.flip();
    return buf;
  }

  public void ensureUploaded(World w) {
    if(!dirty && uploaded) return;

    FloatBuffer data = buildMeshBuffer(w);
    uploadToGPU(data);
    dirty = false;
    uploaded = true;
  }

  private void uploadToGPU(FloatBuffer data) {
    if(vboID == 0) {
      vboID = glGenBuffers();
    }
    glBindBuffer(GL_ARRAY_BUFFER, vboID);
    glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

  private int emitFaceAsTriangles(FloatBuffer b, int x, int y, int z, int face, byte blockID) {

    int packedTile;
    if(face == 2) packedTile = Blocks.TEX_TOP[blockID];
    else if(face == 3) packedTile = Blocks.TEX_BOTTOM[blockID];
    else packedTile = Blocks.TEX_SIDE[blockID];

    float[] uv = new float[4];
    getTileUVPacked(packedTile, uv);

    float u0 = uv[0];
    float v0 = uv[1];
    float u1 = uv[2];
    float v1 = uv[3];

    float au = 0, av = 0;
    float bu = 0, bv = 0;
    float cu = 0, cv = 0;
    float du = 0, dv = 0;

    float nx = 0, ny = 0, nz = 0;
    float x0 = x, x1 = x + 1;
    float y0 = y, y1 = y + 1;
    float z0 = z, z1 = z + 1;

    float ax = 0, ay = 0, az = 0;
    float bx = 0, by = 0, bz = 0;
    float cx = 0, cy = 0, cz = 0;
    float dx = 0, dy = 0, dz = 0;

    switch(face) {
      case 0 -> {
        nx = 1; ny = 0; nz = 0;
        ax = x1; ay = y0; az = z0;
        bx = x1; by = y1; bz = z0;
        cx = x1; cy = y1; cz = z1;
        dx = x1; dy = y0; dz = z1;

        au = u0; av = v0;
        bu = u0; bv = v1;
        cu = u1; cv = v1;
        du = u1; dv = v0;
      }
      case 1 -> {
        nx = -1; ny = 0; nz = 0;
        ax = x0; ay = y0; az = z1;
        bx = x0; by = y1; bz = z1;
        cx = x0; cy = y1; cz = z0;
        dx = x0; dy = y0; dz = z0;

        au = u1; av = v0;
        bu = u1; bv = v1;
        cu = u0; cv = v1;
        du = u0; dv = v0;
      }
      case 2 -> {
        nx = 0; ny = 1; nz = 0;
        ax = x0; ay = y1; az = z1;
        bx = x1; by = y1; bz = z1;
        cx = x1; cy = y1; cz = z0;
        dx = x0; dy = y1; dz = z0;

        au = u0; av = v1;
        bu = u1; bv = v1;
        cu = u1; cv = v0;
        du = u0; dv = v0;
      }
      case 3 -> {
        nx = 0; ny = -1; nz = 0;
        ax = x0; ay = y0; az = z0;
        bx = x1; by = y0; bz = z0;
        cx = x1; cy = y0; cz = z1;
        dx = x0; dy = y0; dz = z1;

        au = u0; av = v0;
        bu = u1; bv = v0;
        cu = u1; cv = v1;
        du = u0; dv = v1;
      }
      case 4 -> {
        nx = 0; ny = 0; nz = 1;
        ax = x1; ay = y0; az = z1;
        bx = x1; by = y1; bz = z1;
        cx = x0; cy = y1; cz = z1;
        dx = x0; dy = y0; dz = z1;

        au = u1; av = v0;
        bu = u1; bv = v1;
        cu = u0; cv = v1;
        du = u0; dv = v0;
      }
      case 5 -> {
        nx = 0; ny = 0; nz = -1;
        ax = x0; ay = y0; az = z0;
        bx = x0; by = y1; bz = z0;
        cx = x1; cy = y1; cz = z0;
        dx = x1; dy = y0; dz = z0;

        au = u0; av = v0;
        bu = u0; bv = v1;
        cu = u1; cv = v1;
        du = u1; dv = v0;
      }
    }
    putV(b, ax, ay, az, nx, ny, nz, au, av);
    putV(b, bx, by, bz, nx, ny, nz, bu, bv);
    putV(b, cx, cy, cz, nx, ny, nz, cu, cv);

    putV(b, ax, ay, az, nx, ny, nz, au, av);
    putV(b, cx, cy, cz, nx, ny, nz, cu, cv);
    putV(b, dx, dy, dz, nx, ny, nz, du, dv);

    return 6;
  }

  private void putV(FloatBuffer b,
    float px, float py, float pz,
    float nx, float ny, float nz,
    float u, float v) {
    b.put(px).put(py).put(pz);
    b.put(nx).put(ny).put(nz);
    b.put(u).put(v);
  }

  public void drawVBO() {
    if(!uploaded || vboID == 0 || vertexCount == 0) return;

    glBindBuffer(GL_ARRAY_BUFFER, vboID);

    int stride = 8 * Float.BYTES;

    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_TEXTURE_COORD_ARRAY);

    glVertexPointer(3, GL_FLOAT, stride, 0L);
    glNormalPointer(GL_FLOAT, stride, 3L * Float.BYTES);
    glTexCoordPointer(2, GL_FLOAT, stride, 6l * Float.BYTES);

    glDrawArrays(GL_TRIANGLES, 0, vertexCount);

    glDisableClientState(GL_TEXTURE_COORD_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
    glDisableClientState(GL_VERTEX_ARRAY);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

  private static void getTileUVPacked(int packed, float[] out) {
    int tx = BlockTexture.tileX(packed);
    int ty = BlockTexture.tileY(packed);

    float sx = 1.0f / BlockTexture.ATLAS_TILE_X;
    float sy = 1.0f / BlockTexture.ATLAS_TILE_Y;

    ty = (BlockTexture.ATLAS_TILE_Y - 1) - ty;

    float u0 = tx * sx;
    float v0 = ty * sy;

    float u1 = u0 + sx;
    float v1 = v0 + sy;

    out[0] = u0; out[1] = v0;
    out[2] = u1; out[3] = v1;
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

  public void cleanup() {
    if(vboID != 0) {
      glDeleteBuffers(vboID);
      vboID = 0;
    }
    uploaded = false;
  }
}
