package com.jless.voxel;

import static org.lwjgl.opengl.GL11.*;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

import org.joml.Vector3i;

public class UI {

  private ImGuiImplGl3 imGuiGL;
  private ImGuiImplGlfw imGuiGLFW;

  public void initGUI(long window) {
    ImGui.createContext();

    imGuiGLFW = new ImGuiImplGlfw();
    imGuiGLFW.init(window, true);

    imGuiGL = new ImGuiImplGl3();
    imGuiGL.init();
  }

  public void renderGUI() {
    imGuiGLFW.newFrame();
    ImGui.newFrame();

    fps();
    drawCrosshair();

    ImGui.render();
    imGuiGL.renderDrawData(ImGui.getDrawData());
  }

  private void fps() {
    ImGui.setNextWindowPos(20, 20);
    ImGui.setNextWindowBgAlpha(0.35f);

    ImGui.begin(
      "##fps",
      ImGuiWindowFlags.NoTitleBar |
      ImGuiWindowFlags.AlwaysAutoResize |
      ImGuiWindowFlags.NoMove |
      ImGuiWindowFlags.NoSavedSettings
    );

    ImGui.textColored(0.3f, 0.6f, 0.6f, 1, String.format("FPS: %.1f", ImGui.getIO().getFramerate()));
    ImGui.end();
  }

  public void drawOutline(Vector3i block) {
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
    glEnable(GL_TEXTURE_2D);
    glEnable(GL_CULL_FACE);
  }

  private void line(
    float x1, float y1, float z1,
    float x2, float y2, float z2
  ) {
    glVertex3f(x1, y1, z1);
    glVertex3f(x2, y2, z2);
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

}
