package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryUtil;

public final class TextureLoader {

  public final int id;
  public final int width;
  public final int height;

  private TextureLoader(int id, int width, int height) {
    this.id = id;
    this.width = width;
    this.height = height;
  }

  public static TextureLoader load(String filePath, boolean flipY) {
    STBImage.stbi_set_flip_vertically_on_load(flipY);

    IntBuffer w = BufferUtils.createIntBuffer(1);
    IntBuffer h = BufferUtils.createIntBuffer(1);
    IntBuffer comp = BufferUtils.createIntBuffer(1);

    ByteBuffer pixels = STBImage.stbi_load(filePath, w, h, comp, 4);
    if(pixels == null) {
      throw new RuntimeException("Failed to load tex: " + filePath + "\nSTB reason: " + STBImage.stbi_failure_reason());
    }

    int width = w.get(0);
    int height = h.get(0);

    int texID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, texID);

    glTexImage2D(
      GL_TEXTURE_2D,
      0,
      GL_RGBA8,
      width,
      height,
      0,
      GL_RGBA,
      GL_UNSIGNED_BYTE,
      pixels
    );

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    STBImage.stbi_image_free(pixels);
    glBindTexture(GL_TEXTURE_2D, 0);

    return new TextureLoader(texID, width, height);
  }

  public static TextureLoader loadResource(String cp, boolean flipY) {
    if(!cp.startsWith("/")) {
      throw new RuntimeException("Texture.loadResource must start with /");
    }

    STBImage.stbi_set_flip_vertically_on_load(flipY);

    ByteBuffer fileData = null;
    ByteBuffer pixels = null;

    IntBuffer w = BufferUtils.createIntBuffer(1);
    IntBuffer h = BufferUtils.createIntBuffer(1);
    IntBuffer comp = BufferUtils.createIntBuffer(1);

    try {
      fileData = readResourceToByteBuffer(cp);

      pixels = STBImage.stbi_load_from_memory(fileData, w, h, comp, 4);
      if(pixels == null) {
        throw new RuntimeException("Failed to load tex res");
      }
      TextureLoader tex = uploadRGBA(pixels, w.get(0), h.get(0));
      STBImage.stbi_image_free(pixels);
      return tex;
    } catch(IOException e) {
      throw new RuntimeException("failed reading resource");
    } finally {
      if(fileData != null) MemoryUtil.memFree(fileData);
    }
  }

  private static TextureLoader uploadRGBA(ByteBuffer pixels, int w, int h) {
    int texID = glGenTextures();
    glBindTexture(GL_TEXTURE_2D, texID);

    glTexImage2D(
      GL_TEXTURE_2D,
      0,
      GL_RGBA8,
      w,
      h,
      0,
      GL_RGBA,
      GL_UNSIGNED_BYTE,
      pixels
    );

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

    return new TextureLoader(texID, w, h);
  }

  private static ByteBuffer readResourceToByteBuffer(String path) throws IOException {
    try(InputStream in = TextureLoader.class.getResourceAsStream(path)) {
      if(in == null) {
        throw new IOException("Resource not found: " + path);
      }
      byte[] bytes = in.readAllBytes();
      ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
      buffer.put(bytes);
      buffer.flip();
      return buffer;
    }
  }

  public void bind() {
    glBindTexture(GL_TEXTURE_2D, id);
  }

  public void unbind() {
    glBindTexture(GL_TEXTURE_2D, 0);
  }

  public void cleanup() {
    glDeleteTextures(id);
  }
}
