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


public class App {
  UI ui;

  public Controller c;
  private Matrix4f projMatrix;
  private Matrix4f viewMatrix;
  private Matrix4f modelMatrix;
  private FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);


  public static long window;
  public static int wWidth = 800;
  public static int wHeight = 800;

  public static int vSync = 1;
  public static float FOV = 70.0f;
  private double lastMouseX, lastMouseY;
  private boolean firstMouse = true;

  private void run() {
    ui = new UI(this);

    waylandCheck();
    initWindow();
    initGL();

    ui.initGui(window);

    setupMouse();

    loop();
    cleanup();
  }

  private void loop() {
    Chunk chunk = new Chunk(0, 0, 0);
    World world = new World();
    world.generateTerra(32);
    while(!glfwWindowShouldClose(window)) {
      processInput();

      glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

      loadMatrix(projMatrix, GL_PROJECTION);
      loadMatrix(Controller.getViewMatrix(), GL_MODELVIEW);

      FloatBuffer lightPos = BufferUtils.createFloatBuffer(4);
      lightPos.put(new float[] {
        0.5f, 1.0f, 0.3f, 0.0f //dir light
      }).flip();

      glLightfv(GL_LIGHT0, GL_POSITION, lightPos);

      world.render();
      ui.guiFrameRender();

      glfwSwapBuffers(window);
      glfwPollEvents();
    }
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

    c = new Controller(8, 8, 30);

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
