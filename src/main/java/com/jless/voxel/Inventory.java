package com.jless.voxel;

public class Inventory {

  private final byte[] hotbar;
  private static int selected = 0;

  public Inventory(int slots) {
    hotbar = new byte[slots];

    hotbar[0] = BlockID.GRASS;
    hotbar[1] = BlockID.DIRT;
    hotbar[2] = BlockID.STONE;
    hotbar[3] = BlockID.LOG;
    hotbar[4] = BlockID.LEAF;
    hotbar[5] = BlockID.COBBLE;
    hotbar[6] = BlockID.SAND;
    hotbar[7] = BlockID.CHEST;
    hotbar[8] = BlockID.PLANK;
  }

  public int getSelectedIndex() {
    return selected;
  }

  public byte getSelectedBlock() {
    return hotbar[selected];
  }

  public byte getBlockAt(int slot) {
    return hotbar[slot];
  }

  public void setBlockAt(int slot, byte blockID) {
    if(slot < 0 || slot >= hotbar.length) return;
    hotbar[slot] = blockID;
  }

  public void scroll(int direction) {
    selected += direction;

    if(selected < 0) selected = hotbar.length - 1;
    if(selected >= hotbar.length) selected = 0;
  }

  public void selectSlot(int slot) {
    if(slot < 0 || slot >= hotbar.length) return;
    selected = slot;
  }

  public int getSlotCount() {
    return hotbar.length;
  }
}
