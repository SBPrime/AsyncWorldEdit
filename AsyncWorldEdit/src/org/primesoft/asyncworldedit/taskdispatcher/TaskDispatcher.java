/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.primesoft.asyncworldedit.taskdispatcher;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.ChunkWatch;
import org.primesoft.asyncworldedit.utils.Action;
import org.primesoft.asyncworldedit.utils.Func;

/**
 * This class is used to perform tasks that need to by performed as fast as
 * possible (mostly get's)
 *
 * @author SBPrime
 */
public class TaskDispatcher implements Runnable {

    /**
     * Maximum number of retries for fast task. If no block get is exeuted for X
     * the get task stops. If a block get is executed, this is the number of
     * retries for dequeuing operations.
     */
    private final int MAX_RETRIES = 200;

    /**
     * MTA mutex
     */
    private final Object m_mutex = new Object();

    /**
     * Bukkit scheduler
     */
    private final BukkitScheduler m_scheduler;

    /**
     * The chunk watcher
     */
    private final ChunkWatch m_chunkWatch;

    /**
     * Current scheduler fast task
     */
    private BukkitTask m_fastTask;

    /**
     * Number of fast task empty runs remaining
     */
    private int m_fastTaskRunsRemaining;

    /**
     * Parent plugin main
     */
    private final AsyncWorldEditMain m_plugin;

    /**
     * List of fast tasks (high priority)
     */
    private final List<IDispatcherEntry> m_fastTasks = new ArrayList<IDispatcherEntry>();

    /**
     * The main thread
     */
    private Thread m_mainThread;

    /**
     * Initialize new instance of the block placer
     *
     * @param plugin parent
     */
    public TaskDispatcher(AsyncWorldEditMain plugin) {
        m_scheduler = plugin.getServer().getScheduler();
        m_plugin = plugin;
        m_chunkWatch = m_plugin.getChunkWatch();
        startFastTask();
    }

    /**
     * Start the high priority task
     */
    private void startFastTask() {
        synchronized (m_mutex) {
            m_fastTaskRunsRemaining = MAX_RETRIES;
            if (m_fastTask != null) {
                return;
            }
            m_fastTask = m_scheduler.runTaskTimer(m_plugin, this, 1, 1);
        }
    }

    /**
     * Process the high priority requests
     */
    @Override
    public void run() {
        m_mainThread = Thread.currentThread();
        boolean run = true;

        boolean processed = false;
        for (int i = 0; i < MAX_RETRIES && run; i++) {
            run = false;

            final IDispatcherEntry[] tasks;
            synchronized (m_fastTasks) {
                tasks = m_fastTasks.toArray(new IDispatcherEntry[0]);
                m_fastTasks.clear();
            }

            for (IDispatcherEntry t : tasks) {
                t.Process();
            }
            if (tasks.length > 0) {
                processed = true;
                run = true;
                try {
                    //Force thread release!
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                }
            }
        }

        if (!processed) {
            synchronized (m_mutex) {
                m_fastTaskRunsRemaining--;
                if (m_fastTaskRunsRemaining <= 0 && m_fastTask != null) {
                    m_fastTask.cancel();
                    m_fastTask = null;
                }
            }
        }
    }

    /**
     * stop block logger
     */
    public void stop() {
        synchronized (m_mutex) {
            if (m_fastTask != null) {
                m_fastTask.cancel();
                m_fastTask = null;
            }
        }

    }

    /**
     * Add new fast/simple task (high priority tasks!)
     *
     * @param entry
     */
    public void addFastTask(IDispatcherEntry entry) {
        synchronized (m_fastTasks) {
            m_fastTasks.add(entry);
        }

        startFastTask();
    }

    /**
     * Is this thread the main bukkit thread
     *
     * @return
     */
    public boolean isMainTask() {
        return m_mainThread == Thread.currentThread();
    }

    /**
     * Check if world operation is allowed to continue
     *
     * @param world
     * @param cx
     * @param cz
     * @return
     */
    private boolean canPerform(World world, int cx, int cz) {
        return isMainTask() || world.isChunkLoaded(cx, cz);
    }

    /**
     * Queue sunced block get operation
     *
     * @param <T>
     * @param action
     * @return
     */
    private <T> T queueFastOperation(Func<T> action) {
        FuncEntry<T> getBlock = new FuncEntry<T>(action);
        if (isMainTask()) {
            return action.Execute();
        }

        final Object mutex = getBlock.getMutex();

        addFastTask(getBlock);
        synchronized (mutex) {
            while (getBlock.getResult() == null) {
                try {
                    mutex.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
        return getBlock.getResult();
    }

    /**
     *
     * @param action
     * @return
     */
    private void queueFastOperation(Action action) {
        ActionEntry actionEntry = new ActionEntry(action);
        if (isMainTask()) {
            action.Execute();
            return;
        }

        final Object mutex = actionEntry.getMutex();

        addFastTask(actionEntry);
        synchronized (mutex) {
            while (!actionEntry.isDone()) {
                try {
                    mutex.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param action
     * @param world
     * @param pos
     */
    public void performSafe(Action action, World world, Vector pos) {
        int cx = pos.getBlockX() >> 4;
        int cz = pos.getBlockZ() >> 4;
        String worldName = world != null ? world.getName() : null;

        try {
            m_chunkWatch.add(cx, cz, worldName);
            if (canPerform(world, cx, cz)) {
                try {
                    action.Execute();
                    return;
                } catch (Exception ex) {
                    /*
                     * Exception here indicates that async block get is not
                     * available. Therefore use the queue fallback.
                     */
                    AsyncWorldEditMain.log("Error performing safe operation for " + worldName
                            + " cx:" + cx + " cy:" + cz + " Loaded: " + world.isChunkLoaded(cx, cz)
                            + ", inUse: " + world.isChunkInUse(cx, cz) + ". Error: "
                            + ex.toString());
                }
            }
        } finally {
            m_chunkWatch.remove(cx, cz, worldName);
        }

        queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param action
     * @param world
     * @param region
     */
    public void performSafe(Action action, World world, Region region) {
        Set<Vector2D> chunks = region.getChunks();
        String worldName = world != null ? world.getName() : null;

        try {
            boolean canPerform = true;
            for (Vector2D vector : chunks) {
                int cx = vector.getBlockX();
                int cz = vector.getBlockZ();
                m_chunkWatch.add(cx, cz, worldName);
                canPerform &= canPerform(world, cx, cz);
            }
            if (canPerform) {
                try {
                    action.Execute();
                    return;
                } catch (Exception ex) {
                    /*
                     * Exception here indicates that async block get is not
                     * available. Therefore use the queue fallback.
                     */
                    AsyncWorldEditMain.log("Error performing safe operation for " + worldName
                            + " for region " + region.toString() + ". Error: "
                            + ex.toString());
                }
            }
        } finally {
            for (Vector2D vector : chunks) {
                int cx = vector.getBlockX();
                int cz = vector.getBlockZ();
                m_chunkWatch.remove(cx, cz, worldName);
            }
        }

        queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param action
     * @param world
     * @param region
     * @return
     */
    public <T> T performSafe(Func<T> action, World world, Region region) {
        Set<Vector2D> chunks = region.getChunks();
        String worldName = world != null ? world.getName() : null;

        try {
            boolean canPerform = true;
            for (Vector2D vector : chunks) {
                int cx = vector.getBlockX();
                int cz = vector.getBlockZ();
                m_chunkWatch.add(cx, cz, worldName);
                canPerform &= canPerform(world, cx, cz);
            }
            if (canPerform) {
                try {
                    T result = action.Execute();
                    return result;
                } catch (Exception ex) {
                    /*
                     * Exception here indicates that async block get is not
                     * available. Therefore use the queue fallback.
                     */
                    AsyncWorldEditMain.log("Error performing safe operation for " + worldName
                            + " for region " + region.toString() + ". Error: "
                            + ex.toString());
                }
            }
        } finally {
            for (Vector2D vector : chunks) {
                int cx = vector.getBlockX();
                int cz = vector.getBlockZ();
                m_chunkWatch.remove(cx, cz, worldName);
            }
        }

        return queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param action
     * @param world
     * @param pos
     * @return
     */
    public <T> T performSafe(Func<T> action, World world, Vector pos) {
        int cx = pos.getBlockX() >> 4;
        int cz = pos.getBlockZ() >> 4;
        String worldName = world != null ? world.getName() : null;

        try {
            m_chunkWatch.add(cx, cz, worldName);
            if (canPerform(world, cx, cz)) {
                try {
                    T result = action.Execute();
                    return result;
                } catch (Exception ex) {
                    /*
                     * Exception here indicates that async block get is not
                     * available. Therefore use the queue fallback.
                     */
                    AsyncWorldEditMain.log("Error performing safe operation for " + worldName
                            + " cx:" + cx + " cy:" + cz + " Loaded: " + world.isChunkLoaded(cx, cz)
                            + ", inUse: " + world.isChunkInUse(cx, cz) + ". Error: "
                            + ex.toString());
                }
            }
        } finally {
            m_chunkWatch.remove(cx, cz, worldName);
        }

        return queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param action
     */
    public void performSafe(Action action) {
        try {
            action.Execute();
            return;
        } catch (Exception ex) {
            /*
             * Exception here indicates that async block get is not
             * available. Therefore use the queue fallback.
             */
            AsyncWorldEditMain.log("Error performing safe operation. Error: "
                    + ex.toString());
        }
        queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param action
     * @return
     */
    public <T> T performSafe(Func<T> action) {
        try {
            T result = action.Execute();
            return result;
        } catch (Exception ex) {
            /*
             * Exception here indicates that async block get is not
             * available. Therefore use the queue fallback.
             */
            AsyncWorldEditMain.log("Error performing safe operation. Error: "
                    + ex.toString());
        }
        return queueFastOperation(action);
    }
}
