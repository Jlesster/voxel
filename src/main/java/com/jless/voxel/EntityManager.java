package com.jless.voxel;

import java.util.ArrayList;

import org.joml.Vector3f;

public class EntityManager {

  private final ArrayList<Entity> entities = new ArrayList<>();

  private float spawnTimer = 0f;

  public EntityManager() {
    System.out.println("EntityManager Constructed");
  }

  public void update(World world, Vector3f playerPos, float dt) {
    for(int i = 0; i < entities.size(); i++) {
      Entity e = entities.get(i);
      e.update(world, dt);

      float dx = e.pos.x - playerPos.x;
      float dz = e.pos.z - playerPos.z;

      float dist2 = (dx * dx) + (dz * dz);

      float despawn = WorldConsts.ENTITY_DESPAWN_RADIUS;
      if(dist2 > despawn * despawn) {
        e.removed = true;
      }
    }

    entities.removeIf(e -> e.removed);

    spawnTimer -= dt;
    if(spawnTimer <= 0f) {
      spawnTimer = WorldConsts.ENTITY_SPAWN_INTERVAL;

      if(entities.size() < WorldConsts.MAX_ENTITIES) {
        trySpawnPig(world, playerPos);
      }
    }
  }

  private void trySpawnPig(World world, Vector3f playerPos) {
    float radius = WorldConsts.ENTITY_SPAWN_RADIUS;

    float angle = (float)(Math.random() * Math.PI * 2);
    float dist = (float)(Math.random() * radius);

    float x = playerPos.x + (float)Math.cos(angle) * dist;
    float z = playerPos.z + (float)Math.sin(angle) * dist;

    int ix = (int)Math.floor(x);
    int iz = (int)Math.floor(z);

    int cx = Math.floorDiv(ix, WorldConsts.CHUNK_SIZE);
    int cz = Math.floorDiv(iz, WorldConsts.CHUNK_SIZE);
    if(world.getChunkIfLoaded(cx, cz) == null) {
      return;
    }

    int y = world.getSurfaceY(ix, iz);
    if(y < 0) {
      return;
    }

    byte ground = world.getBlockWorld(ix, y, iz);
    if(ground != BlockID.GRASS) {
      return;
    }

    EntityPig pig = new EntityPig(ix + 0.5f, y + 1f, iz + 0.5f);
    entities.add(pig);
  }

  public void render(VoxelRender render) {
    for(Entity e : entities) {
      e.render(render);
    }
  }
}
