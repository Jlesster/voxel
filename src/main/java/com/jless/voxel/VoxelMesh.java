package com.jless.voxel;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

public class VoxelMesh {
  private int vboID;
  private int vertexCount;

  public VoxelMesh(float[] vertices) {
    this.vertexCount = vertices.length / 3;

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
    glEnableClientState(GL_VERTEX_ARRAY);
    glVertexPointer(3, GL_FLOAT, 0, 0);

    glDrawArrays(GL_TRIANGLES, 0, vertexCount);

    glDisableClientState(GL_VERTEX_ARRAY);
    glBindBuffer(GL_ARRAY_BUFFER, 0);
  }

  public void cleanup() {
    glDeleteBuffers(vboID);
  }

  public int[][] vertices = {
    {},
    {},
    {},
//TODO add vertices or make a square
    {},
    {},
    {},

  };
}
