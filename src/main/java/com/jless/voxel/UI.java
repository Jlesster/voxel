package com.jless.voxel;

import imgui.ImGui;
import imgui.flag.ImGuiWindowFlags;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;

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
}
