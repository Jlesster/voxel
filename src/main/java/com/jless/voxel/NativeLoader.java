package com.jless.voxel;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.*;

public class NativeLoader {
  public static void load() throws IOException {
    String os = System.getProperty("os.name").toLowerCase();
    String arch = System.getProperty("os.arch");

    String platform;
    if(os.contains("win")) platform = "windows";
    else if(os.contains("mac")) platform = "macos";
    else platform = "linux";

    Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "cbchess", "natives", platform);
    tempDir.toFile().mkdirs();

    extract("/natives/" + platform, tempDir);

    System.setProperty("java.library.path", tempDir.toAbsolutePath().toString());
    resetLibraryPath();
  }

  private static void extract(String resRoot, Path targetDir) throws IOException {
    try(InputStream in = NativeLoader.class.getResourceAsStream(resRoot + "/natives.list")) {
      if(in == null) {
        throw new IOException("Missing natives.list for " + resRoot);
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line;

      while((line = reader.readLine()) != null) {
        Path out = targetDir.resolve(line);
        try(InputStream lib = NativeLoader.class.getResourceAsStream(resRoot + "/" + line)) {
          Files.copy(lib, out, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private static void resetLibraryPath() {
    try {
      Field field = ClassLoader.class.getDeclaredField("sys_paths");
      field.setAccessible(true);
      field.set(null, null);
    } catch (Exception ignored) {}
  }
}
