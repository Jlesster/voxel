package com.jless.voxel;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import org.joml.*;
import org.joml.Math;


public class App {
  private UI ui;
  private World world;
  public Controller c;
  private Chunk chunk;
  private Lighting lighting;
  private TextureLoader atlas;
  private VoxelRender voxelRender;
  private RaycastHit currHit = null;
  private final EntityManager entityManager = new EntityManager();

  private Matrix4f projMatrix;
  private Matrix4f viewMatrix;
  private Matrix4f modelMatrix;
  private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  private boolean breakReq = false;
  private boolean placeReq = false;

  private boolean jumpPressed = false;

  public static long window;
  public static int wWidth = 1280;
  public static int wHeight = 720;

  public static int vSync = 1;
  public static float FOV = 90.0f;

  private double lastTime = 0.0;

  private double lastMouseX, lastMouseY;
  private boolean firstMouse = true;

  private void run() {
    waylandCheck();
    initWindow();
    initGL();

    voxelRender = new VoxelRender();
    ui = new UI();

    ui.initGUI(window);

    setupMouse();

    loop();
    cleanup();
  }

  private void loop() {
    while(!glfwWindowShouldClose(window)) {
      float dt = getDeltaTime();

      glfwPollEvents();
      processInput();

      lighting.update(dt);

      boolean f = glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS;
      boolean b = glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS;
      boolean l = glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS;
      boolean r = glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS;

      c.update(world, dt, jumpPressed, f, b, l, r);

      currHit = VoxelRaycast.raycast(
        world,
        c.getEyePos(),
        c.getForward(),
        6.0f
      );

      blockManip();

      Vector3f sky = lighting.getSkyColor();
      glClearColor(sky.x, sky.y, sky.z, 1.0f);
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      lighting.beginShadowPass(c.getPosition());

      glDisable(GL_TEXTURE_2D);
      glDisable(GL_LIGHTING);
      glColorMask(false, false, false, false);

      VoxelRender.render(world, lighting.getLightSpaceMatrix(), new Matrix4f(), null, atlas);

      glColorMask(true, true, true, true);
      glEnable(GL_LIGHTING);

      lighting.endShadowPass(wWidth, wHeight);

      viewMatrix = c.getViewMatrix();

      if(!VoxelRender.isShadersInit()) {
        loadMatrix(projMatrix, GL_PROJECTION);
        loadMatrix(viewMatrix, GL_MODELVIEW);
      }

      glActiveTexture(GL_TEXTURE0);
      glEnable(GL_TEXTURE_2D);
      glBindTexture(GL_TEXTURE_2D, atlas.id);

      glActiveTexture(GL_TEXTURE1);
      glEnable(GL_TEXTURE_2D);
      lighting.bindShadowMap(1);

      VoxelRender.render(world, projMatrix, c.getViewMatrix(), lighting, atlas);

      glActiveTexture(GL_TEXTURE1);
      glBindTexture(GL_TEXTURE_2D, 0);
      glActiveTexture(GL_TEXTURE0);
      glBindTexture(GL_TEXTURE_2D, 0);

      entityManager.update(world, c.getPosition(), dt);

      if(currHit != null) {
        ui.drawOutline(currHit.block);
      }

      entityManager.render(voxelRender);

      ui.renderGUI();

      glfwSwapBuffers(window);
    }
    cleanup();
  }

  private void loadMatrix(Matrix4f matrix, int mode) {
    matrixBuffer.clear();

    matrix.get(matrixBuffer);

    glMatrixMode(mode);
    glLoadMatrixf(matrixBuffer);
  }

  private void initWindow() {
    if(!glfwInit()) {
      throw new IllegalStateException("Err init GLFW");
    }
    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    glfwWindowHint(GLFW_VISIBLE, GLFW_TRUE);
    glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);
    glfwWindowHint(GLFW_DECORATED, GLFW_TRUE);

    window = glfwCreateWindow(wWidth, wHeight, "Voxel beta", 0, 0);
    if(window == 0) throw new RuntimeException("Err init window");

    glfwSetWindowFocusCallback(window, (win, focused) -> {
      if(!focused) {
        glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        firstMouse = true;
      }
    });

    glfwSetMouseButtonCallback(window, (win, button, action, mods) -> {
      if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        glfwSetInputMode(win, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
      }

      //Raycast breaking
      if(button == GLFW_MOUSE_BUTTON_LEFT && action == GLFW_PRESS) {
        breakReq = true;
      }

      //raycast placing
      if(button == GLFW_MOUSE_BUTTON_RIGHT && action == GLFW_PRESS) {
        placeReq = true;
      }
    });

    glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
      if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
        glfwSetWindowShouldClose(window, true);
      }

      if(key == GLFW_KEY_ENTER && action == GLFW_PRESS) {
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        firstMouse = true;
      }
    });
  }

  private void initGL() {
    glfwMakeContextCurrent(window);
    glfwSwapInterval(vSync);
    GL.createCapabilities();

    glEnable(GL_DEPTH_TEST);
    glDepthFunc(GL_LEQUAL);

    glEnable(GL_CULL_FACE);
    glCullFace(GL_BACK);
    glFrontFace(GL_CCW);

    //lighting stuff
    glEnable(GL_LIGHT0);
    glEnable(GL_LIGHTING);
    glEnable(GL_NORMALIZE);
    glEnable(GL_COLOR_MATERIAL);
    glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    lighting = new Lighting();
    lighting.initShadowMapping();
    VoxelRender.initShaders();
    Vector3f sky = lighting.getSkyColor();
    glClearColor(sky.x, sky.y, sky.z, 1.0f);
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    projMatrix = new Matrix4f();
    viewMatrix = new Matrix4f();
    modelMatrix = new Matrix4f();
    matrixBuffer = BufferUtils.createFloatBuffer(16);

    atlas = TextureLoader.loadResource("/Tileset.png", true);
    glEnable(GL_TEXTURE_2D);
    glBindTexture(GL_TEXTURE_2D, atlas.id);

    lighting.initShadowMapping();

    updateProjectionMatrix(wWidth, wHeight);

    //player controller
    c = new Controller(8, 40, 30);

    //world init
    world = new World();
    world.generateSpawn();

    //window resizing
    glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
      glViewport(0, 0, width, height);
      updateProjectionMatrix(width, height);
    });

    try(MemoryStack stack = stackPush()) {
      IntBuffer w = stack.mallocInt(1);
      IntBuffer h = stack.mallocInt(1);
      glfwGetFramebufferSize(window, w, h);
      glViewport(0, 0, w.get(0), h.get(0));
    }
  }

  private void blockManip() {
    if(breakReq) {
      breakReq = false;
      System.out.println("breakRequested fired");

      RaycastHit hit = VoxelRaycast.raycast(world,
        c.getEyePos(),
        c.getForward(),
        6.0f
      );

      if(hit != null) {
        world.set(
          hit.block.x,
          hit.block.y,
          hit.block.z,
          BlockID.AIR
        );
        byte after = world.getIfLoaded(hit.block.x, hit.block.y, hit.block.z);
        System.out.println("After set, id=" + after);
      }
    }

    if(placeReq) {
      placeReq = false;

      RaycastHit hit = VoxelRaycast.raycast(world,
        c.getEyePos(),
        c.getForward(),
        6.0f
      );

      if(hit != null) {
        Vector3i p = new Vector3i(hit.block).add(hit.normal);
        if(!c.wouldCollideWithBlock(p.x, p.y, p.z)) {
          world.set(
            p.x,
            p.y,
            p.z,
            BlockID.STONE
          );
        }
      }
    }
  }

  private void updateProjectionMatrix(int width, int height) {
    projMatrix = new Matrix4f().perspective(
      (float)Math.toRadians(FOV),     //fov in radians
      (float)width / height,              //aspect ratio
      0.1f,                               //near plane
      1000.0f                             //far plane
    );
  }

  private void cleanup() {
    lighting.cleanup();
    chunk.cleanup();
    glfwDestroyWindow(window);
    glfwTerminate();
  }

  private void processInput() {
    jumpPressed = glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS;

    if(glfwGetKey(window, GLFW_KEY_T) == GLFW_PRESS) {
      lighting.setTimeOfDay(lighting.getTimeOfDay() + 0.01f);
    }
    if(glfwGetKey(window, GLFW_KEY_Y) == GLFW_PRESS) {
      lighting.setTimeOfDay(lighting.getTimeOfDay() - 0.01f);
    }
  }

  private void setupMouse() {

    glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
      if(firstMouse) {
        lastMouseX = xpos;
        lastMouseY = ypos;
        firstMouse = false;
        return;
      }

      float dx = (float)(xpos - lastMouseX);
      float dy = (float)(ypos - lastMouseY);

      lastMouseX = xpos;
      lastMouseY = ypos;

      c.processMouse(dx, dy);
    });
  }

  private float getDeltaTime() {
    double now = glfwGetTime();
    float dt = (float)(now - lastTime);
    lastTime = now;
    return Math.min(dt, 0.05f);
  }

  private void waylandCheck() {
    String wayland = System.getenv("WAYLAND_DISPLAY");
    String xdg = System.getenv("XDG_SESSION_TYPE");
    if(wayland != null || "wayland".equalsIgnoreCase(xdg)) {
      glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
    }
  }

  public static void main(String[] args) {
    App a = new App();
    a.run();
  }
}
