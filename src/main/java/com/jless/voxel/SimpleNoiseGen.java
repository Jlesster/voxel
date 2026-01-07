package com.jless.voxel;

public class SimpleNoiseGen implements TerrainGenerator {
  @Override
  public int getHeight(int x, int z) {
    double nx = x * 0.05;
    double nz = z * 0.05;

    double height = Math.sin(nx) + Math.cos(nz);
    height *= 4;

    return (int)(height + 8);
  }
}
