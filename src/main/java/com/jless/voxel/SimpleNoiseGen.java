package com.jless.voxel;

public class SimpleNoiseGen implements TerrainGenerator {
  @Override
  public float density(int x, int y, int z) {
    double nx = x * 0.05;
    double nz = z * 0.05;

    double height = Math.sin(nx) + Math.cos(nz) * 0.5;
    height = (height + 1.0) * 0.5;

    return (float)height;
  }
}
