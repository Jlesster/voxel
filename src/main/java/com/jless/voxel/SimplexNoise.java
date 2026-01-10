package com.jless.voxel;

import java.util.Random;

public class SimplexNoise {

  private static final int PSIZE = 2048;
  private static final int PMASK = 2047;

  private final static short[] perm = new short[PSIZE];

  public SimplexNoise(long seed) {
    short[] source = new short[PSIZE];
    for(short i = 0; i < PSIZE; i++) source[i] = i;

    Random rand = new Random(seed);
    for(int i = PSIZE - 1; i >= 0; i--) {
      int r = rand.nextInt(i + 1);
      perm[i] = source[r];
      source[r] = source[i];
    }
  }

  public float noise2D(float x, float y) {
    int xi = fastFloor(x);
    int yi = fastFloor(y);

    float xf = x - xi;
    float yf = y - yi;

    int aa = perm[(perm[xi & PMASK] + yi) & PMASK];
    int ab = perm[(perm[xi & PMASK] + yi + 1) & PMASK];
    int ba = perm[(perm[(xi + 1) & PMASK] + yi) & PMASK];
    int bb = perm[(perm[(xi + 1) & PMASK] + yi + 1) & PMASK];

    float u = fade(xf);
    float v = fade(yf);

    float x1 = lerp(grad(aa, xf, yf), grad(ba, xf - 1, yf), u);
    float x2 = lerp(grad(ab, xf, yf - 1), grad(bb, xf - 1, yf - 1), u);

    return lerp(x1, x2, v);
  }

  private static int fastFloor(float f) {
    return f >= 0 ? (int)f : (int)f - 1;
  }

  private static float fade(float t) {
    return t * t * t * (t * (t * 6 - 15) + 10);
  }

  private static float lerp(float a, float b, float t) {
    return a + t * (b - a);
  }

  private static float grad3d(int hash, float x, float y, float z) {
    int h = hash & 15;
    float u = h < 8 ? x : y;
    float v = h < 4 ? y : (h == 12 || h == 14 ? x : z);
    return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
  }

  private static float grad(int hash, float x, float y) {
    switch(hash & 3) {
      case 0: return x + y;
      case 1: return -x + y;
      case 2: return x - y;
      default: return -x - y;
    }
  }

  public static float noise(float x, float y, float z) {
    int xi = fastFloor(x);
    int yi = fastFloor(y);
    int zi = fastFloor(z);

    float xf = x - xi;
    float yf = y - yi;
    float zf = z - zi;

    int aaa = perm[(perm[(perm[xi & PMASK] + yi) & PMASK] + zi) & PMASK];
    int aba = perm[(perm[(perm[xi & PMASK] + yi + 1) & PMASK] + zi) & PMASK];
    int aab = perm[(perm[(perm[xi & PMASK] + yi) & PMASK] + zi + 1) & PMASK];
    int abb = perm[(perm[(perm[xi & PMASK] + yi + 1) & PMASK] + zi + 1) & PMASK];
    int baa = perm[(perm[(perm[(xi+ 1) & PMASK] + yi) & PMASK] + zi) & PMASK];
    int bba = perm[(perm[(perm[(xi+ 1) & PMASK] + yi + 1) & PMASK] + zi) & PMASK];
    int bab = perm[(perm[(perm[(xi+ 1) & PMASK] + yi) & PMASK] + zi + 1) & PMASK];
    int bbb = perm[(perm[(perm[(xi+ 1) & PMASK] + yi + 1) & PMASK] + zi + 1) & PMASK];

    float u = fade(xf);
    float v = fade(yf);
    float w = fade(zf);

    float x1 = lerp(grad3d(aaa, xf, yf, zf), grad3d(baa, xf - 1, yf, zf), u);
    float x2 = lerp(grad3d(aba, xf, yf - 1, zf), grad3d(bba, xf - 1, yf - 1, zf), u);
    float x3 = lerp(grad3d(aab, xf, yf, zf - 1), grad3d(bab, xf - 1, yf, zf - 1), u);
    float x4 = lerp(grad3d(abb, xf, yf - 1, zf - 1), grad3d(bbb, xf - 1, yf - 1, zf - 1), u);

    float y1 = lerp(x1, x2, v);
    float y2 = lerp(x3, x4, v);

    return lerp(y1, y2, w);

  }
}
