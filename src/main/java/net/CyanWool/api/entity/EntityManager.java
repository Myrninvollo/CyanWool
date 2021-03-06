package net.CyanWool.api.entity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.CyanWool.api.world.Chunk;
import net.CyanWool.api.world.Location;

public class EntityManager {

    private Map<Integer, Entity> entities = new HashMap<Integer, Entity>();
    private Set<Integer> usedIds = new HashSet<Integer>();
    private int last = 0;

    public Collection<Entity> getAll() {
        return entities.values();
    }

    public Entity getEntity(int id) {
        return entities.get(id);
    }

    public synchronized void register(Entity entity) {
        if (getEntity(entity.getEntityID()) != null) {
            return; // IMPOSIBBLEEE
        }

        int startedAt = last;
        for (int id = last + 1; id != startedAt; ++id) {
            if (id == -1 || id == 0)
                continue;

            if (usedIds.add(id)) {
                entity.setEntityID(id);
                last = id;
                break;
            }
        }
        entities.put(entity.getEntityID(), entity);
        Location loc = entity.getLocation();
        loc.getChunk().getEntities().add(entity);
    }

    public synchronized void unregister(Entity entity) {
        entities.remove(entity.getEntityID());
        usedIds.remove(entity.getEntityID());

        Iterator<Entity> it = entity.getLocation().getChunk().getEntities().iterator();
        while (it.hasNext()) {
            Entity ent = it.next();
            if (ent.getEntityID() == entity.getEntityID()) {
                it.remove();
            }
        }
    }

    public void moveToOtherLocation(Entity entity, Location loc) {
        Chunk prev = entity.getLocation().getChunk();
        Chunk next = loc.getChunk();

        if (!prev.equals(next)) {
            Iterator<Entity> it = prev.getEntities().iterator();
            while (it.hasNext()) {
                Entity ent = it.next();
                if (ent.getEntityID() == entity.getEntityID()) {
                    it.remove();
                }
            }
            next.getEntities().add(entity);
        }
    }

}