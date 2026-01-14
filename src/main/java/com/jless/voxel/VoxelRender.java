package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

import org.joml.*;
import org.joml.Math;

public class VoxelRender {
  private static final FrustumIntersection frustum = new FrustumIntersection();
  private static final Matrix4f pv = new Matrix4f();

  private static Shaders shaders;
  private static boolean shadersInit = false;

  private static int entityVao;
  private static int entityVbo;
  private static int entityIbo;
  private static boolean entityMeshInit = false;

  public static void render(World world, Matrix4f proj, Matrix4f view, Lighting lighting, TextureLoader atlas) {
    pv.set(proj).mul(view);
    frustum.set(pv);

    if(shadersInit && shaders != null) {
      shaders.use();

      glActiveTexture(GL_TEXTURE0);
      glEnable(GL_TEXTURE_2D);
      glBindTexture(GL_TEXTURE_2D, atlas.id);

      shaders.setUniformInt("textureSampler", 0);

      if(lighting != null) {
        lighting.bindShadowMap(1);
        shaders.setUniformInt("shadowMap", 1);

        glActiveTexture(GL_TEXTURE0);
      }

      shaders.setUniformMatrix4f("projMatrix", proj);
      shaders.setUniformMatrix4f("viewMatrix", view);
      shaders.setUniformMatrix4f("modelMatrix", new Matrix4f());

      if(lighting != null) {
        shaders.setUniformMatrix4f("lightSpaceMatrix", lighting.getLightSpaceMatrix());
        shaders.setUniformVec("sunDir", lighting.getSunDir());
        shaders.setUniformVec("sunColor", lighting.getSunColor());
        shaders.setUniformVec("ambientColor", lighting.getAmbientColor());
        shaders.setUniformFloat("shadowBias", 0.0003f);
      }
    }
    shaders.setUniformInt("useSolidColor", 0);

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

  public static void initEntityMesh() {
    if(entityMeshInit) return;

    float[] verts = {
      //Front
      0,0,1,  0,0,1,  0,0,
      1,0,1,  0,0,1,  1,0,
      1,1,1,  0,0,1,  1,1,
      0,1,1,  0,0,1,  0,1,

      //Back
      1,0,0,  0,0,-1, 0,0,
      0,0,0,  0,0,-1, 1,0,
      0,1,0,  0,0,-1, 1,1,
      1,1,0,  0,0,-1, 0,1,

      0,0,0,  -1,0,0, 0,0,
      0,0,1,  -1,0,0, 1,0,
      0,1,1,  -1,0,0, 1,1,
      0,1,0,  -1,0,0, 0,1,

      1,0,1,  1,0,0, 0,0,
      1,0,0,  1,0,0, 1,0,
      1,1,0,  1,0,0, 1,1,
      1,1,1,  1,0,0, 0,1,

      //top
      0,1,1,  0,1,0, 0,0,
      1,1,1,  0,1,0, 1,0,
      1,1,0,  0,1,0, 1,1,
      0,1,0,  0,1,0, 0,1,

      0,0,0,  0,-1,0, 0,0,
      1,0,0,  0,-1,0, 1,0,
      1,0,1,  0,-1,0, 1,1,
      0,0,1,  0,-1,0, 0,1,
    };

    int[] indices = {
      0,1,2,  2,3,0,
      4,5,6,  6,7,4,
      8,9,10,  10,11,8,
      12,13,14,  14,15,12,
      16,17,18,  18,19,16,
      20,21,22,  22,23,20
    };

    entityVao = glGenVertexArrays();
    glBindVertexArray(entityVao);

    entityVbo = glGenBuffers();
    glBindBuffer(GL_ARRAY_BUFFER, entityVbo);
    glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);

    entityIbo = glGenBuffers();
    glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, entityIbo);
    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

    int stride = (3 + 3 + 2) * Float.BYTES;

    glEnableVertexAttribArray(0);
    glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0);

    glEnableVertexAttribArray(1);
    glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 3 * Float.BYTES);

    glEnableVertexAttribArray(2);
    glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, (3 + 3) * Float.BYTES);

    glBindVertexArray(0);

    entityMeshInit = true;
  }

  public static void setShaders(Shaders s) {
    shaders = s;
  }

  public static void drawEntityBoxShader(
    float x0, float y0, float z0,
    float x1, float y1, float z1,
    float[] rgb,
    Matrix4f entityModel
  ) {
    initEntityMesh();
    float minX = Math.min(x0, x1);
    float maxX = Math.max(x0, x1);

    float minY = Math.min(y0, y1);
    float maxY = Math.max(y0, y1);

    float minZ = Math.min(z0, z1);
    float maxZ = Math.max(z0, z1);

    Matrix4f boxModel = new Matrix4f(entityModel)
      .translate(minX, minY, minZ)
      .scale(maxX - minX, maxY - minY, maxZ - minZ);

    shaders.setUniformMatrix4f("modelMatrix", boxModel);
    shaders.setUniformInt("useSolidColor", 1);
    shaders.setUniformVec("solidColor", new Vector3f(rgb[0], rgb[1], rgb[2]));

    glBindVertexArray(entityVao);
    glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
    glBindVertexArray(0);
  }


  public static boolean isShadersInit() {
    return shadersInit;
  }

  public static void initShaders() {
    if(shadersInit) return;

    try {

    shaders = new Shaders("/shaders/vert.glsl", "/shaders/frag.glsl");

    shadersInit = true;
    System.out.println("Shaders init successfully");
    } catch(Exception e) {
      System.err.println("failed to init shaders");
      e.printStackTrace();
      shadersInit = false;
    }
  }
}
