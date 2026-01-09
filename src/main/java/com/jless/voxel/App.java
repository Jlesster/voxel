package com.jless.voxel;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import org.joml.Matrix4f;
import org.joml.Vector3i;


public class App {
  private UI ui;
  private World world;
  public Controller c;
  private RaycastHit currHit = null;

  private Matrix4f projMatrix;
  private Matrix4f viewMatrix;
  private Matrix4f modelMatrix;
  private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  private boolean breakReq = false;
  private boolean placeReq = false;

  public static long window;
  public static int wWidth = 1280;
  public static int wHeight = 720;

  public static int vSync = 1;
  public static float FOV = 70.0f;

  private double lastMouseX, lastMouseY;
  private boolean firstMouse = true;

  private void run() {
    waylandCheck();
    initWindow();
    initGL();

    ui = new UI();
    ui.initGUI(window);

    setupMouse();

    loop();
    cleanup();
  }

  private void loop() {
    while(!glfwWindowShouldClose(window)) {
      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      processInput();

      loadMatrix(projMatrix, GL_PROJECTION);
      loadMatrix(Controller.getViewMatrix(), GL_MODELVIEW);


      FloatBuffer lightPos = BufferUtils.createFloatBuffer(4);
      lightPos.put(new float[] {
        0.5f, 1.0f, 0.3f, 0.0f //dir light
      }).flip();

      glLightfv(GL_LIGHT0, GL_POSITION, lightPos);

      currHit = VoxelRaycast.raycast(
        world,
        c.getEyePos(),
        c.getForward(),
        6.0f
      );

      blockManip();
      VoxelRender.render(world);
      if(currHit != null) {
        drawOutline(currHit.block);
      }

      drawCrosshair();
      ui.renderGUI();

      glfwSwapBuffers(window);
      glfwPollEvents();
    }
  }

  private void drawOutline(Vector3i block) {
    float x = block.x;
    float y = block.y;
    float z = block.z;

    float eps = 0.002f;

    glDisable(GL_TEXTURE_2D);
    glDisable(GL_CULL_FACE);
    glLineWidth(2.0f);

    glColor3f(0f, 0f, 0f);

    glBegin(GL_LINES);

    line(x - eps, y - eps, z - eps, x + 1 + eps, y - eps, z - eps);
    line(x + 1 + eps, y - eps, z - eps, x + 1 + eps, y - eps, z + 1 + eps);
    line(x + 1 + eps, y - eps, z + 1 + eps, x - eps, y - eps, z + 1 + eps);
    line(x - eps, y - eps, z + 1 + eps, x - eps, y - eps, z - eps);

    line(x - eps, y + 1 + eps, z - eps, x + 1 + eps, y + 1 + eps, z - eps);
    line(x + 1 + eps, y + 1 + eps, z - eps, x + 1 + eps, y + 1 + eps, z + 1 + eps);
    line(x + 1 + eps, y + 1 + eps, z + 1 + eps, x - eps, y + 1 + eps, z + 1 + eps);
    line(x - eps, y + 1 + eps, z + 1 + eps, x - eps, y + 1 + eps, z - eps);

    line(x - eps, y - eps, z - eps, x - eps, y + 1 + eps, z - eps);
    line(x + 1 + eps, y - eps, z - eps, x + 1 + eps, y + 1 + eps, z - eps);
    line(x + 1 + eps, y - eps, z + 1 + eps, x + 1 + eps, y + 1 + eps, z + 1 + eps);
    line(x - eps, y + 1 + eps, z + 1 + eps, x - eps, y + 1 + eps, z - eps);

    glEnd();
    glEnable(GL_CULL_FACE);
  }

  private void line(
    float x1, float y1, float z1,
    float x2, float y2, float z2
  ) {
    glVertex3f(x1, y1, z1);
    glVertex3f(x2, y2, z2);
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
    glDepthFunc(GL_LESS);

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

    glClearColor(0.5f, 0.7f, 1.0f, 0.8f);

    projMatrix = new Matrix4f();
    viewMatrix = new Matrix4f();
    modelMatrix = new Matrix4f();
    matrixBuffer = BufferUtils.createFloatBuffer(16);

    FloatBuffer lightColor = BufferUtils.createFloatBuffer(4);
    lightColor.put(new float[] {
      1.0f, 1.0f, 1.0f, 1.0f
    }).flip();

    glLightfv(GL_LIGHT0, GL_DIFFUSE, lightColor);

    updateProjectionMatrix(wWidth, wHeight);

    //player controller
    c = new Controller(8, 16, 30);

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
        world.set(
          p.x,
          p.y,
          p.z,
          BlockID.STONE
        );
      }
    }
  }

  public static void drawCrosshair() {
      // Disable depth test for UI elements
      glDisable(GL_DEPTH_TEST);
      glDisable(GL_LIGHTING);

      // Switch to 2D orthographic projection
      glMatrixMode(GL_PROJECTION);
      glPushMatrix();
      glLoadIdentity();
      glOrtho(0, App.wWidth, 0, App.wHeight, -1, 1);

      glMatrixMode(GL_MODELVIEW);
      glPushMatrix();
      glLoadIdentity();

      // Draw crosshair at screen center
      float cx = App.wWidth / 2.0f;
      float cy = App.wHeight / 2.0f;
      float size = 10.0f;

      glColor3f(1.0f, 1.0f, 1.0f);
      glLineWidth(2.0f);

      glBegin(GL_LINES);
      // Horizontal line
      glVertex2f(cx - size, cy);
      glVertex2f(cx + size, cy);
      // Vertical line
      glVertex2f(cx, cy - size);
      glVertex2f(cx, cy + size);
      glEnd();

      // Restore matrices
      glPopMatrix();
      glMatrixMode(GL_PROJECTION);
      glPopMatrix();
      glMatrixMode(GL_MODELVIEW);

      glEnable(GL_DEPTH_TEST);
      glEnable(GL_LIGHTING);
  }

  private void updateProjectionMatrix(int width, int height) {
    projMatrix = new Matrix4f().perspective(
      (float)Math.toRadians(FOV),     //fov in radians
      (float)width / height,            //aspect ratio
      0.1f,                               //near plane
      1000.0f                             //far plane
    );
  }

  private void cleanup() {
    glfwDestroyWindow(window);
    glfwTerminate();
  }

  private void processInput() {
    if(glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
      c.moveF();
    }
    if(glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
      c.moveB();
    }
    if(glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
      c.moveL();
    }
    if(glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
      c.moveR();
    }
    if(glfwGetKey(window, GLFW_KEY_SPACE) == GLFW_PRESS) {
      c.moveU();
    }
    if(glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) {
      c.moveD();
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

      Controller.processMouse(dx, dy);
    });
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
