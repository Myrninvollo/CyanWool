package net.CyanWool.api.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.CyanWool.api.Server;
import net.CyanWool.api.io.WorldIOService;
import net.CyanWool.api.scheduler.CyanTask;

public class WorldManager {

    private Server server;
    private final List<WorldEntry> worldsEntry = new ArrayList<WorldEntry>();
    private WorldIOService service;

    public WorldManager(Server server, WorldIOService service) {
        this.server = server;
        this.service = service;
    }

    public Server getServer() {
        return server;
    }

    public List<World> getWorlds() {
        ArrayList<World> worlds = new ArrayList<World>();
        for (WorldEntry entry : worldsEntry) {
            worlds.add(entry.getWorld());
        }
        return worlds;
    }

    public void loadWorld(final World world) {
        service.readWorld(world);
    }

    public boolean addWorld(final World world) {
        for (WorldEntry entry : worldsEntry) {
            if (entry.getWorld().getName().equals(world.getName())) {
                return false;
            }
        }

        CyanTask thread = server.getScheduler().runAsyncTaskRepeat(new Runnable() {

            @Override
            public void run() {
                world.onTick();
            }
        }, 1, 1);
        WorldEntry we = new WorldEntry(world, thread);
        worldsEntry.add(we);
        server.getLogger().info("Added new world: " + world.getName());
        return true;
    }

    public World getWorld(String name) {
        for (WorldEntry entry : worldsEntry) {
            if (!entry.getWorld().getName().equals(name)) {
                return entry.getWorld();
            }
        }
        return null;
    }

    public void removeWorld(final World world) {
        // Save
        // world.saveAll();
        // ???
        world.saveAll();
        service.saveWorld(world);
        stopWorldEntry(world);
        server.getLogger().info("removed world: " + world.getName());
    }

    public void saveAllWorlds() {
        Iterator<WorldEntry> it = worldsEntry.iterator();
        while (it.hasNext()) {
            WorldEntry w = it.next();
            w.getTask().cancel(false);
            it.remove();
        }
    }

    // Needed ?
    private boolean stopWorldEntry(World world) {
        Iterator<WorldEntry> it = worldsEntry.iterator();
        while (it.hasNext()) {
            WorldEntry w = it.next();
            if (w.getWorld().getName().equals(world.getName())) {
                w.getTask().cancel(false);
                it.remove();
                return true;
            }
        }
        return false;
    }
}