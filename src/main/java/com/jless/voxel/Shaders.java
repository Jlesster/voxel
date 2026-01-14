package com.jless.voxel;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.*;

import java.io.*;
import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Shaders {
  private int programID;
  private int vertShaderID;
  private int fragShaderID;

  private final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

  public Shaders(String vertPath, String fragPath) {
    String vertexSource = loadShaderSource(vertPath);
    String fragSource = loadShaderSource(fragPath);

    vertShaderID = compileShader(vertexSource, GL_VERTEX_SHADER);
    fragShaderID = compileShader(fragSource, GL_FRAGMENT_SHADER);

    programID = linkProgram(vertShaderID, fragShaderID);
    System.out.println("Shaders created");
  }

  private String loadShaderSource(String path) {
    StringBuilder source = new StringBuilder();

    try {
      InputStream in = Shaders.class.getResourceAsStream(path);
      if(in == null) {
      throw new RuntimeException("Shader file not found");
      }
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String line;
      while((line = reader.readLine()) != null) {
      source.append(line).append("\n");
      }
      reader.close();
    } catch (Exception e) {
      System.err.println("Err loading shader" + path);
      e.printStackTrace();
      throw new RuntimeException("Failed to load " + path + " shader");
    }
    return source.toString();
  }

  private int compileShader(String source, int type) {
    int shaderID = glCreateShader(type);

    glShaderSource(shaderID, source);
    glCompileShader(shaderID);

    int success = glGetShaderi(shaderID, GL_COMPILE_STATUS);
    if(success == GL_FALSE) {
      String log = glGetShaderInfoLog(shaderID, 1024);
      String typeName = (type == GL_VERTEX_SHADER) ? "vertex" : "fragment";

      System.err.println("Err " + typeName + " shader comp failed");
      System.err.println(log);
      throw new RuntimeException("Shader comp failed");
    }
    return shaderID;
  }

  private int linkProgram(int vertID, int fragID) {
    int program = glCreateProgram();

    glAttachShader(program, vertID);
    glAttachShader(program, fragID);

    glBindAttribLocation(programID, 0, "position");
    glBindAttribLocation(programID, 1, "normal");
    glBindAttribLocation(programID, 2, "texCoord");

    glLinkProgram(program);

    int success = glGetProgrami(program, GL_LINK_STATUS);
    if(success == GL_FALSE) {
      String log = glGetProgramInfoLog(program, GL_LINK_STATUS);
      System.err.println("Err shader program link failed");
      System.err.println(log);
      throw new RuntimeException("Shader Linking failed");
    }

    glValidateProgram(program);
    success = glGetProgrami(program, GL_VALIDATE_STATUS);
    if(success == GL_FALSE) {
      String log = glGetProgramInfoLog(program, 1024);
      System.err.println("Shader program validation failed");
      System.err.println(log);
    }
    return program;
  }

  private int getUniformLocation(String name) {
    int location = glGetUniformLocation(programID, name);
    if(location == -1) {
      System.err.println("Uniform " + name + " not found");
    }
    return location;
  }

  public void setUniformMatrix4f(String name, Matrix4f matrix) {
    int location = getUniformLocation(name);
    matrixBuffer.clear();
    matrix.get(matrixBuffer);
    glUniformMatrix4fv(location, false, matrixBuffer);
  }

  public void setUniformVec(String name, Vector3f vec) {
    int location = getUniformLocation(name);
    glUniform3f(location, vec.x, vec.y, vec.z);
  }

  public void setUniformFloat(String name, float value) {
    int location = getUniformLocation(name);
    glUniform1f(location, value);
  }

  public void setUniformInt(String name, int value) {
    int location = getUniformLocation(name);
    glUniform1i(location, value);
  }

  public void cleanup() {
    stop();

    glDetachShader(programID, vertShaderID);
    glDetachShader(programID, fragShaderID);

    glDeleteShader(vertShaderID);
    glDeleteShader(fragShaderID);

    glDeleteProgram(programID);

    System.out.println("Shader program cleaned");
  }

  public int getProgramID() {
    return programID;
  }

  public void use() {
    glUseProgram(programID);
  }

  public void stop() {
    glUseProgram(0);
  }
}
