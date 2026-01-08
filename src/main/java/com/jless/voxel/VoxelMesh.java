package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

public class VoxelMesh {
  private int vboID;
  private int vertexCount;

  public VoxelMesh(float[] vertices) {
    this.vertexCount = vertices.length / 10;

    vboID = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, vboID);

    FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
    buffer.put(vertices);
    buffer.flip();
    glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

  public void render() {
    glBindBuffer(GL_ARRAY_BUFFER, vboID);

    int stride = 10 * Float.BYTES;

    glEnableClientState(GL_VERTEX_ARRAY);
    glEnableClientState(GL_NORMAL_ARRAY);
    glEnableClientState(GL_COLOR_ARRAY);

    glVertexPointer(3, GL_FLOAT, stride, 0);
    glNormalPointer(GL_FLOAT, stride, 3 * Float.BYTES);
    glColorPointer(4, GL_FLOAT, stride, 6 * Float.BYTES);

    glDrawArrays(GL_TRIANGLES, 0, vertexCount);

    glDisableClientState(GL_VERTEX_ARRAY);
    glDisableClientState(GL_NORMAL_ARRAY);
    glDisableClientState(GL_COLOR_ARRAY);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

  public void cleanup() {
    glDeleteBuffers(vboID);
  }
}
