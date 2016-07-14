/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.asyncworldedit.taskdispatcher;

import com.sk89q.worldedit.BlockVector2D;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.taskdispatcher.IDispatcherEntry;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.regions.Region;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import org.primesoft.asyncworldedit.ChunkWatch;
import org.primesoft.asyncworldedit.api.IWorld;
import org.primesoft.asyncworldedit.api.utils.IAction;
import org.primesoft.asyncworldedit.api.utils.IFunc;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.PositionHelper;

/**
 * This class is used to perform tasks that need to by performed as fast as
 * possible (mostly get's)
 *
 * @author SBPrime
 */
public class TaskDispatcher implements Runnable, ITaskDispatcher {

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
    private final AsyncWorldEditBukkit m_plugin;

    /**
     * List of fast tasks (high priority) Use linked list to overcome memory
     * leakage
     */
    private final Queue<IDispatcherEntry> m_fastTasks = new LinkedList<IDispatcherEntry>();

    /**
     * The main thread
     */
    private Thread m_mainThread;

    /**
     * Last enter time
     */
    private long m_lastEnter = -1;

    /**
     * Dispatcher main thread usage
     */
    private double m_usage = 0;

    /**
     * Indicates that the task dispatcher is paused
     */
    private boolean m_isPaused = false;

    /**
     * Is the task dispatcher paused
     *
     * @return
     */
    @Override
    public boolean isPaused() {
        return m_isPaused;
    }

    /**
     * Set pause on task dispatcher placer
     *
     * @param pause
     */
    @Override
    public void setPause(boolean pause) {
        m_isPaused = pause;
    }

    /**
     * Initialize new instance of the block placer
     *
     * @param plugin parent
     */
    public TaskDispatcher(AsyncWorldEditBukkit plugin) {
        m_scheduler = plugin.getServer().getScheduler();
        m_plugin = plugin;
        m_chunkWatch = m_plugin.getChunkWatch();

        m_lastEnter = System.currentTimeMillis();
        startFastTask();
    }

    /**
     * Start the high priority task
     */
    private void startFastTask() {
        synchronized (m_mutex) {
            m_fastTaskRunsRemaining = ConfigProvider.getDispatcherMaxIdle();
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
        long enter = System.currentTimeMillis();
        long runDelta = enter - m_lastEnter;
        long runTime;
        int jobsCount = ConfigProvider.getDispatcherMaxJobs();
        int maxTime = ConfigProvider.getDispatcherMaxTime();

        if (runDelta < 1) {
            runDelta = 0;
        }
        m_lastEnter = enter;

        if (Double.isNaN(m_usage)) {
            m_usage = 0;
        }

        double usage = m_usage;

        m_mainThread = Thread.currentThread();
        if (!isPaused()) {

            boolean processed = false;
            for (int i = 0; i < jobsCount && (m_usage * 3 + usage) / 4 < maxTime; i++) {
                IDispatcherEntry task = null;
                synchronized (m_fastTasks) {
                    if (!m_fastTasks.isEmpty()) {
                        task = m_fastTasks.poll();
                    }
                }

                if (task != null) {
                    task.Process();
                    processed = true;
                } else {
                    try {
                        //Force thread change
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                    }
                }

                runTime = System.currentTimeMillis() - enter;
                if (runTime + runDelta > 0) {
                    usage = 1000.0 * runTime / (runTime + runDelta);
                } else {
                    usage = 0;
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

        runTime = System.currentTimeMillis() - enter;
        if (runTime + runDelta > 0) {
            usage = 1000.0 * runTime / (runTime + runDelta);
        } else {
            usage = 0;
        }
        m_usage = (m_usage * 3 + usage) / 4;
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
    @Override
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
    @Override
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
    private boolean canPerform(IWorld world, int cx, int cz) {
        if (isMainTask()) {
            return true;
        }

        return m_chunkWatch.isChunkLoaded(cx, cz, world.getName()) //Do not use this!
                //The class containing loaded chunks is not thread safe.
                //&& world.isChunkLoaded(cx, cz)
                ;
    }

    /**
     * Queue secure get operation
     *
     * @param <T>
     * @param action
     * @return
     */
    @Override
    public <T> T queueFastOperation(IFunc<T> action) {
        if (action == null) {
            return null;
        }

        FuncEntry<T> getBlock = new FuncEntry<T>(action);
        if (isMainTask()) {
            return action.execute();
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
     */
    @Override
    public void queueFastOperation(IAction action) {
        if (action == null) {
            return;
        }

        ActionEntry actionEntry = new ActionEntry(action);
        if (isMainTask()) {
            action.execute();
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
     * @param mutex
     * @param action
     * @param world
     * @param pos
     */
    @Override
    public void performSafeChunk(Object mutex, IAction action, IWorld world, Vector2D pos) {
        synchronized (mutex) {
            int cx = pos.getBlockX();
            int cz = pos.getBlockZ();
            String worldName = world != null ? world.getName() : null;

            try {
                m_chunkWatch.add(cx, cz, worldName);
                if (canPerform(world, cx, cz)) {
                    try {
                        action.execute();
                        return;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s cx: %2$s cz: %3$s Loaded: %4$s",
                                        worldName, cx, cz, world.isChunkLoaded(cx, cz)));
                    }
                }
            } finally {
                m_chunkWatch.remove(cx, cz, worldName);
            }
        }
        queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param pos
     */
    @Override
    public void performSafe(Object mutex, IAction action, IWorld world, Vector pos) {
        synchronized (mutex) {
            int cx = PositionHelper.positionToChunk(pos.getX());
            int cz = PositionHelper.positionToChunk(pos.getZ());
            String worldName = world != null ? world.getName() : null;

            try {
                m_chunkWatch.add(cx, cz, worldName);
                if (canPerform(world, cx, cz)) {
                    try {
                        action.execute();
                        return;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s cx: %2$s cz: %3$s Loaded: %4$s",
                                        worldName, cx, cz, world.isChunkLoaded(cx, cz)));
                    }
                }
            } finally {
                m_chunkWatch.remove(cx, cz, worldName);
            }
        }
        queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     * @param world
     * @param region
     */
    @Override
    public void performSafe(Object mutex, IAction action, IWorld world, Region region) {
        synchronized (mutex) {
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
                        action.execute();
                        return;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s for region %2$s",
                                        worldName, region.toString()));
                    }
                }
            } finally {
                for (Vector2D vector : chunks) {
                    int cx = vector.getBlockX();
                    int cz = vector.getBlockZ();
                    m_chunkWatch.remove(cx, cz, worldName);
                }
            }
        }

        queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @param world
     * @param region
     * @return
     */
    @Override
    public <T> T performSafe(Object mutex, IFunc<T> action, IWorld world, Region region) {
        synchronized (mutex) {
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
                        T result = action.execute();
                        return result;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s for region %2$s",
                                        worldName, region.toString()));
                    }
                }
            } finally {
                for (Vector2D vector : chunks) {
                    int cx = vector.getBlockX();
                    int cz = vector.getBlockZ();
                    m_chunkWatch.remove(cx, cz, worldName);
                }
            }
        }
        return queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @param world
     * @param pos
     * @return
     */
    @Override
    public <T> T performSafe(Object mutex, IFunc<T> action, IWorld world, Vector pos) {
        synchronized (mutex) {
            int cx = PositionHelper.positionToChunk(pos.getX());
            int cz = PositionHelper.positionToChunk(pos.getZ());
            String worldName = world != null ? world.getName() : null;
            try {
                m_chunkWatch.add(cx, cz, worldName);
                if (canPerform(world, cx, cz)) {
                    try {
                        T result = action.execute();
                        return result;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s cx: %2$s cz: %3$s Loaded: %4$s",
                                        worldName, cx, cz, world.isChunkLoaded(cx, cz)));
                    }
                }
            } finally {
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
     * @param mutex
     * @param action
     * @param world
     * @param pos
     * @return
     */
    @Override
    public <T> T performSafeChunk(Object mutex, IFunc<T> action, IWorld world, Vector2D pos) {
        synchronized (mutex) {
            int cx = pos.getBlockX();
            int cz = pos.getBlockZ();
            String worldName = world != null ? world.getName() : null;
            try {
                m_chunkWatch.add(cx, cz, worldName);
                if (canPerform(world, cx, cz)) {
                    try {
                        T result = action.execute();
                        return result;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s cx: %2$s cz: %3$s Loaded: %4$s",
                                        worldName, cx, cz, world.isChunkLoaded(cx, cz)));
                    }
                }
            } finally {
                m_chunkWatch.remove(cx, cz, worldName);
            }
        }
        return queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param mutex
     * @param action
     */
    @Override
    public void performSafe(Object mutex, IAction action) {
        synchronized (mutex) {
            try {
                action.execute();
                return;
            } catch (Exception ex) {
                /*
                 * Exception here indicates that async block get is not
                 * available. Therefore use the queue fallback.
                 */
                ExceptionHelper.printException(ex, "Error performing safe operation.");
            }
        }
        queueFastOperation(action);
    }

    /**
     * Perform operation using a safe wrapper. If the basic operation fails
     * queue it on dispatcher
     *
     * @param <T>
     * @param mutex
     * @param action
     * @return
     */
    @Override
    public <T> T performSafe(Object mutex, IFunc<T> action) {
        synchronized (mutex) {
            try {
                T result = action.execute();
                return result;
            } catch (Exception ex) {
                /*
                 * Exception here indicates that async block get is not
                 * available. Therefore use the queue fallback.
                 */
                ExceptionHelper.printException(ex, "Error performing safe operation.");
            }
        }
        return queueFastOperation(action);
    }

    @Override
    public void performSafeChunk(Object mutex, IAction action, IWorld world, Collection<BlockVector2D> chunks) {
        synchronized (mutex) {            
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
                        action.execute();
                        return;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s for chunk list",
                                        worldName));
                    }
                }
            } finally {
                for (Vector2D vector : chunks) {
                    int cx = vector.getBlockX();
                    int cz = vector.getBlockZ();
                    m_chunkWatch.remove(cx, cz, worldName);
                }
            }
        }

        queueFastOperation(action);
    }

    @Override
    public <T> T performSafeChunk(Object mutex, IFunc<T> action, IWorld world, Collection<BlockVector2D> chunks) {
        synchronized (mutex) {            
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
                        T result = action.execute();
                        return result;
                    } catch (Exception ex) {
                        /*
                         * Exception here indicates that async block get is not
                         * available. Therefore use the queue fallback.
                         */
                        ExceptionHelper.printException(ex,
                                String.format("Error performing safe operation for %1$s for chunk list",
                                        worldName));
                    }
                }
            } finally {
                for (Vector2D vector : chunks) {
                    int cx = vector.getBlockX();
                    int cz = vector.getBlockZ();
                    m_chunkWatch.remove(cx, cz, worldName);
                }
            }
        }
        return queueFastOperation(action);
    }
}
