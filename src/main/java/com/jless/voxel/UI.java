package com.jless.voxel;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;


public class UI {
  private ImGuiImplGlfw imGuiGLFW;
  private ImGuiImplGl3 imGuiGL;

  App app;

  public UI(App app) {
    this.app = app;
  }

  public void initGui(long window) {
		ImGui.createContext();

		ImGuiIO io = ImGui.getIO();;
		io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

		imGuiGLFW = new ImGuiImplGlfw();
		imGuiGLFW.init(window, true);

		imGuiGL = new ImGuiImplGl3();
		imGuiGL.init();
  }

  public void guiFrameRender() {
    imGuiGLFW.newFrame();
    ImGui.newFrame();

    drawFPS();

    ImGui.render();
    imGuiGL.renderDrawData(ImGui.getDrawData());
  }

  public static void drawFPS() {
    ImGui.setNextWindowPos(20, 20);
    ImGui.setNextWindowBgAlpha(0.35f);
    ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.0f, 0.0f, 1.0f);
    ImGui.begin(
      "##FPS",
      ImGuiWindowFlags.NoTitleBar |
      ImGuiWindowFlags.NoMove |
      ImGuiWindowFlags.AlwaysAutoResize
    );
    ImGui.text(String.format("FPS: %.1f", ImGui.getIO().getFramerate()));
    ImGui.popStyleColor();
    ImGui.end();
  }

}
