package com.jless.voxel;

public class PerlinTerraGen implements TerrainGenerator {

  @Override
  public float density(int wx, int wy, int wz) {
    float scale = 0.05f;

    float nx = wx * scale;
    float ny = wy * scale;
    float nz = wz * scale;

    float noise = Perlin.noise(nx, ny, nz);

    float ground = wy - 32;

    return noise * 8.0f - ground;
  }
}
