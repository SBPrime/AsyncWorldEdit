/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
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
package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerListener;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.util.eventbus.EventBus;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.worldedit.AsyncTask;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

import java.util.*;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.api.MessageSystem;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerPlayer;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.JobStatus;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.utils.IAsyncCommand;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;
import org.primesoft.asyncworldedit.configuration.ConfigMemory;
import org.primesoft.asyncworldedit.configuration.ConfigRenderer;
import org.primesoft.asyncworldedit.core.AwePlatform;
import org.primesoft.asyncworldedit.events.JobAddedEvent;
import org.primesoft.asyncworldedit.events.JobRemovedEvent;
import org.primesoft.asyncworldedit.platform.api.IScheduler;
import org.primesoft.asyncworldedit.utils.GCUtils;

/**
 *
 * @author SBPrime
 */
public class BlockPlacer implements IBlockPlacer {

    /**
     * Bukkit scheduler
     */
    private final IScheduler m_scheduler;

    /**
     * MTA mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The physics watcher
     */
    private final IPhysicsWatch m_physicsWatcher;

    /**
     * Current scheduler task
     */
    private BlockPlacerTask m_task;

    /**
     * Logged events queue (per player)
     */
    private final HashMap<IPlayerEntry, BlockPlacerPlayer> m_blocks;

    /**
     * All locked queues
     */
    private final HashSet<IPlayerEntry> m_lockedQueues;

    /**
     * Is the global queue locked
     */
    private boolean m_globalQueueLocked;

    /**
     * Global queue max size
     */
    private int m_queueMaxSizeHard;

    /**
     * Global queue max size
     */
    private int m_queueMaxSizeSoft;

    /**
     * Minimum free memory
     */
    private long m_minMemoryHard;

    /**
     * Minimum free memory
     */
    private long m_minMemorySoft;

    /**
     * Talk interval
     */
    private int m_talkInterval;

    /**
     * Run number
     */
    private int m_runNumber;

    /**
     * Last run time
     */
    private long m_lastRunTime;

    /**
     * The progress display integrator
     */
    private final IProgressDisplay m_progressDisplay;

    /**
     * The task dispatcher
     */
    private final ITaskDispatcher m_taskDispatcher;

    /**
     * List of all job added listeners
     */
    private final List<IBlockPlacerListener> m_jobAddedListeners;
    
    

    /**
     * Indicates that the blocks placer is paused
     */
    private boolean m_isPaused = false;

    /**
     * The global queue wait mutex
     */
    private final Object m_globalWaitMutex = new Object();

    /**
     * The blocks placer interval
     */
    private long m_interval;
    
    /**
     * Is the blocks placer paused
     *
     * @return
     */
    @Override
    public boolean isPaused() {
        return m_isPaused;
    }

    /**
     * Set pause on blocks placer
     *
     * @param pause
     */
    @Override
    public void setPause(boolean pause) {
        m_isPaused = pause;
    }

    /**
     * Get the physics watcher
     *
     * @return
     */
    public IPhysicsWatch getPhysicsWatcher() {
        return m_physicsWatcher;
    }

    /**
     * Initialize new instance of the block placer
     *
     * @param aweCore parent
     */
    public BlockPlacer(IAsyncWorldEditCore aweCore) {
        m_jobAddedListeners = new ArrayList<IBlockPlacerListener>();
        m_lastRunTime = System.currentTimeMillis();
        m_runNumber = 0;
        m_blocks = new HashMap<IPlayerEntry, BlockPlacerPlayer>();
        m_lockedQueues = new HashSet<IPlayerEntry>();
        m_scheduler = aweCore.getPlatform().getScheduler();
        m_progressDisplay = aweCore.getProgressDisplayManager();        

        m_physicsWatcher = aweCore.getPhysicsWatcher();
        m_taskDispatcher = aweCore.getTaskDispatcher();
        
        loadConfig();
    }

    /**
     * Reload the AWE configuration
     */
    public final void loadConfig() {
        final BlockPlacer blocPlacer = this;

        ConfigRenderer rConfig = ConfigProvider.renderer();
        
        long interval = rConfig.getInterval();
        m_talkInterval = rConfig.getQueueTalkInterval();
        m_queueMaxSizeHard = rConfig.getQueueMaxSizeHard();
        m_queueMaxSizeSoft = rConfig.getQueueMaxSizeSoft();
        
        ConfigMemory mConfig = ConfigProvider.memory();
        
        m_minMemoryHard = mConfig.getMinMemoryHard() * 1000;
        m_minMemorySoft = mConfig.getMinMemorySoft() * 1000;

        if (m_task != null) {
            m_task.queueStop();
        }

        m_interval = interval;
        m_task = new BlockPlacerTask(m_scheduler, m_interval) {
            @Override
            public void run(BlockPlacerTask task) {
                blocPlacer.run(task);
            }
        };
    }

    /**
     * Add event listener
     *
     * @param listener
     */
    @Override
    public void addListener(IBlockPlacerListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (m_jobAddedListeners) {
            if (!m_jobAddedListeners.contains(listener)) {
                m_jobAddedListeners.add(listener);
            }
        }
    }

    /**
     * Remove event listener
     *
     * @param listener
     */
    @Override
    public void removeListener(IBlockPlacerListener listener) {
        if (listener == null) {
            return;
        }
        synchronized (m_jobAddedListeners) {
            if (m_jobAddedListeners.contains(listener)) {
                m_jobAddedListeners.remove(listener);
            }
        }
    }

    /**
     * Block placer main loop
     */
    private void run(BlockPlacerTask task) {
        long enterFunctionTime = System.currentTimeMillis();
        final long timeDelte = enterFunctionTime - m_lastRunTime;

        if (isPaused()) {
            m_lastRunTime = enterFunctionTime;
            return;
        }

        boolean talk = false;
        final List<IJobEntry> jobsToCancel = new ArrayList<IJobEntry>();
        //Number of blocks placed for player        
        final HashMap<IPermissionGroup, HashSet<IPlayerEntry>> groups = new HashMap<IPermissionGroup, HashSet<IPlayerEntry>>();

        synchronized (m_mutex) {
            final IPlayerEntry[] keys = m_blocks.keySet().toArray(new IPlayerEntry[0]);
            for (IPlayerEntry player : keys) {
                IPermissionGroup group = player.getPermissionGroup();

                HashSet<IPlayerEntry> uuids;
                if (!groups.containsKey(group)) {
                    uuids = new HashSet<IPlayerEntry>();
                    uuids.add(player);
                    groups.put(group, uuids);
                } else {
                    uuids = groups.get(group);
                    if (!uuids.contains(player)) //Should not happen but better be safe then sorry ;)
                    {
                        uuids.add(player);
                    }
                }
            }
            m_runNumber++;
        }
        if (m_runNumber > m_talkInterval) {
            m_runNumber = 0;
            talk = true;
        }

        if (m_task.isShutingDown()) {
            IPlayerEntry[] entries;
            synchronized (m_mutex) {
                entries = m_blocks.keySet().toArray(new IPlayerEntry[0]);
            }

            for (IPlayerEntry pe : entries) {
                Object mutex = pe.getWaitMutex();
                synchronized (mutex) {
                    mutex.notifyAll();
                }

            }
            return;
        }
        
        final HashMap<IPlayerEntry, Integer> blocksPlaced = new HashMap<IPlayerEntry, Integer>();
        final Map.Entry<IPermissionGroup, HashSet<IPlayerEntry>>[] knownGroups = groups.entrySet().toArray(new Map.Entry[0]);
        final List<BlockPlacerGroup> processedGroups = new ArrayList<BlockPlacerGroup>(knownGroups.length);        
        
        for (Map.Entry<IPermissionGroup, HashSet<IPlayerEntry>> entry : knownGroups) {
            IPermissionGroup permissionGroup = entry.getKey();
            IPlayerEntry[] keys = entry.getValue().toArray(new IPlayerEntry[0]);

            if (keys.length > 0) {
                processedGroups.add(new BlockPlacerGroup(permissionGroup, keys));
            }
        }
        
        boolean blockPlaced = !groups.isEmpty() && processQueue(processedGroups, blocksPlaced, jobsToCancel);

        if (m_globalQueueLocked) {
            boolean unlock = GCUtils.getTotalAvailableMemory() >= m_minMemorySoft;

            if (blockPlaced) {
                int globalSize = 0;

                synchronized (m_mutex) {
                    for (Map.Entry<IPlayerEntry, BlockPlacerPlayer> queueEntry : m_blocks.entrySet()) {
                        globalSize += queueEntry.getValue().getQueue().size();
                    }
                }

                unlock &= (globalSize < m_queueMaxSizeSoft);
            }

            if (unlock) {
                synchronized (m_globalWaitMutex) {
                    m_globalWaitMutex.notifyAll();
                }

                m_globalQueueLocked = false;
            } else if (!blockPlaced) {
                GCUtils.GC();
            }
        }

        synchronized (m_mutex) {
            for (Map.Entry<IPlayerEntry, BlockPlacerPlayer> queueEntry : m_blocks.entrySet()) {
                IPlayerEntry playerEntry = queueEntry.getKey();
                BlockPlacerPlayer entry = queueEntry.getValue();
                Integer cnt = blocksPlaced.get(playerEntry);

                showProgress(playerEntry, entry, cnt != null ? cnt : 0, timeDelte, talk);
            }
        }

        for (IJobEntry job : jobsToCancel) {
            job.setStatus(JobStatus.Done);
            onJobRemoved(job);
        }

        m_lastRunTime = enterFunctionTime;
    }

    /**
     * process queued blocks
     *
     * @param blocksPlaced number of blocksplaced for players
     * @param jobsToCancel canceled blocks
     */
    private boolean processQueue(final List<BlockPlacerGroup> groups,
            final HashMap<IPlayerEntry, Integer> blocksPlaced, final List<IJobEntry> jobsToCancel) {        
        long startTime = System.currentTimeMillis();
        int blocks = 0;
        
        boolean demanding = false;

        int pos = 0;
        while (!groups.isEmpty()) {
            BlockPlacerGroup group = groups.get(pos);
            int maxTime = group.getRendererTime();
            int maxBlocksCount = group.getRendererBlocks();
            
            IBlockPlacerEntry entry;
            synchronized (m_mutex) {
                entry = fetchEntry(group, blocksPlaced, jobsToCancel);
            }

            if (entry == null) {
                groups.remove(group);
            } else {
                entry.process(this);
                blocks++;
                
                
                boolean isDemanding = entry.isDemanding();
                demanding |= isDemanding;
                
                if (isDemanding) {
                    groups.clear();
                } else if ((maxTime != -1 && (System.currentTimeMillis() - startTime) >= maxTime) ||
                        (maxBlocksCount != -1 && blocks > maxBlocksCount))
                {
                    groups.remove(group);
                }
            }
            
            pos = pos + 1;
            if (pos >= groups.size()) {
                pos = 0;
            }
        }

        if (ConfigProvider.isDebugOn()) {
            log(String.format("[BP RUN] Blocks: %d\tTime: %d\tDemanding: %s",
                    blocks, (System.currentTimeMillis() - startTime), demanding ? "Y" : "N"));
        }
        return blocks > 0;
    }

    /**
     * Fetch next entry that is going to be processed in this run
     *
     * @param playerNames list of all players
     * @param seqNumber sequence number in player names (everyone is treated
     * equally)
     * @param blocksPlaced number of blocks placed for player
     * @param jobsToCancel jobs to cancel
     * @return fatched block
     */
    private IBlockPlacerEntry fetchEntry(final BlockPlacerGroup permissionGroup,
            final HashMap<IPlayerEntry, Integer> blocksPlaced,
            final List<IJobEntry> jobsToCancel) {
        if (permissionGroup == null) {
            return null;
        }
        
        IPlayerEntry[] playerEntries = permissionGroup.getPlayers();        
        if (playerEntries == null || playerEntries.length == 0) {
            return null;
        }

        int keyPos = permissionGroup.getSeqNumber() % playerEntries.length;
        IBlockPlacerEntry result = null;
        IPlayerEntry resultPlayer = null;

        for (int retry = playerEntries.length; result == null && retry > 0; retry--) {
            final IPlayerEntry player = playerEntries[keyPos];
            final BlockPlacerPlayer playerEntry = m_blocks.get(player);
            if (playerEntry != null) {
                Queue<IBlockPlacerEntry> queue = playerEntry.getQueue();
                synchronized (queue) {
                    if (!queue.isEmpty()) {
                        IBlockPlacerEntry entry = queue.poll();
                        if (entry != null) {
                            result = entry;
                            resultPlayer = player;

                            if (blocksPlaced.containsKey(player)) {
                                blocksPlaced.put(player, blocksPlaced.get(player) + 1);
                            } else {
                                blocksPlaced.put(player, 1);
                            }
                        }
                    } else {
                        for (IJobEntry job : playerEntry.getJobs()) {
                            JobStatus jStatus = job.getStatus();
                            if (jStatus == JobStatus.Done
                                    || jStatus == JobStatus.Waiting
                                    || jStatus == JobStatus.Canceled) {
                                jobsToCancel.add(job);
                            }
                        }

                        for (IJobEntry job : jobsToCancel) {
                            playerEntry.removeJob(job);
                        }
                    }
                }

                final int size = queue.size();
                if (size < permissionGroup.getQueueSoftLimit()) {
                    unlockQueue(player, true);
                }
                if (size == 0 && !playerEntry.hasJobs()) {
                    m_blocks.remove(playerEntries[keyPos]);
                    if (player.getMessaging(MessageSystem.BAR)) {
                        hideProgressBar(player, playerEntry);
                    }
                }
            } else {
                unlockQueue(player, true);
            }
            keyPos = (keyPos + 1) % playerEntries.length;
        }

        permissionGroup.updateProgress(keyPos, resultPlayer);
        return result;
    }

    /**
     * stop block logger
     */
    public void stop() {
        m_task.stop();

        BlockPlacerPlayer[] entries;
        synchronized (m_mutex) {
            entries = m_blocks.values().toArray(new BlockPlacerPlayer[0]);
        }

        for (BlockPlacerPlayer pe : entries) {
            for (IJobEntry je : pe.getJobs()) {
                je.cancel();
            }
        }

        synchronized (m_globalWaitMutex) {
            m_globalWaitMutex.notifyAll();
        }

        m_globalQueueLocked = false;
    }

    /**
     * Get next job id for player
     *
     * @param player
     * @return
     */
    @Override
    public int getJobId(IPlayerEntry player) {
        BlockPlacerPlayer playerEntry;
        synchronized (m_mutex) {
            if (m_blocks.containsKey(player)) {
                playerEntry = m_blocks.get(player);
            } else {
                playerEntry = new BlockPlacerPlayer(player);
                m_blocks.put(player, playerEntry);
            }
        }

        return playerEntry.getNextJobId();
    }

    /**
     * Get the player job
     *
     * @param player player uuid
     * @param jobId job ID
     * @return
     */
    @Override
    public IJobEntry getJob(IPlayerEntry player, int jobId) {
        synchronized (m_mutex) {
            if (!m_blocks.containsKey(player)) {
                return null;
            }
            BlockPlacerPlayer playerEntry = m_blocks.get(player);
            return playerEntry.getJob(jobId);
        }
    }

    /**
     * Add new job for player
     *
     * @param player player UUID
     * @param job the job
     * @return
     */
    @Override
    public boolean addJob(IPlayerEntry player, IJobEntry job) {
        boolean result;

        synchronized (m_mutex) {
            BlockPlacerPlayer playerEntry;

            if (!m_blocks.containsKey(player)) {
                playerEntry = new BlockPlacerPlayer(player);
                m_blocks.put(player, playerEntry);
            } else {
                playerEntry = m_blocks.get(player);
            }
            result = playerEntry.addJob(job, false);
        }

        if (result) {
            synchronized (m_jobAddedListeners) {
                for (IBlockPlacerListener listener : m_jobAddedListeners) {
                    listener.jobAdded(job);
                }
            }
            
            AwePlatform.getInstance().getCore().getEventBus().post(new JobAddedEvent(job));
        } else {
            player.say(MessageType.BLOCK_PLACER_JOBS_LIMIT.format());
            job.cancel();
        }

        return result;
    }

    /**
     * Add task to perform in async mode
     *
     * @param player
     * @param entry
     * @return
     */
    @Override
    public boolean addTasks(final IPlayerEntry player, final IBlockPlacerEntry entry) {
        if (player == null) {
            return false;
        }

        boolean isMain = m_taskDispatcher.isMainTask();

        boolean retry = false;
        Object waitOn = null;

        do {
            if (retry && isMain) {
                return false;
            }

            retry = false;

            if (player.isDisposed()) {
                return false;
            }

            if (waitOn != null) {
                synchronized (waitOn) {
                    try {
                        int wait = (int) (m_interval < 1 ? 5000
                                : Math.min(10000, 10000 * ConfigProvider.TICKS_PER_SECOND / m_interval));
                        waitOn.wait(wait);
                    } catch (InterruptedException ex) {
                        return false;

                    }
                }
            }

            synchronized (m_mutex) {
                final BlockPlacerPlayer playerEntry;
                if (!m_blocks.containsKey(player)) {
                    playerEntry = new BlockPlacerPlayer(player);
                    m_blocks.put(player, playerEntry);
                } else {
                    playerEntry = m_blocks.get(player);
                }

                if (m_lockedQueues.contains(player) && !(entry instanceof JobEntry)) {
                    waitOn = player.getWaitMutex();
                    retry = true;
                    continue;
                }

                final Queue<IBlockPlacerEntry> queue = playerEntry.getQueue();
                final boolean bypassGlobal = (player.isAllowed(Permission.QUEUE_BYPASS) && !ConfigProvider.permission().isQueueBypassDisabled())
                        || entry instanceof JobEntry;
                final boolean bypass = player.isAllowed(Permission.QUEUE_BYPASS) || entry instanceof JobEntry;
                final IPermissionGroup group = player.getPermissionGroup();
                int globalSize = 0;
                for (Map.Entry<IPlayerEntry, BlockPlacerPlayer> queueEntry : m_blocks.entrySet()) {
                    globalSize += queueEntry.getValue().getQueue().size();
                }

                long memAvailable = GCUtils.getTotalAvailableMemory();

                boolean queueFull = m_queueMaxSizeHard > 0 && globalSize > m_queueMaxSizeHard;
                boolean memLow = m_minMemoryHard > 0 && memAvailable < m_minMemoryHard;

                if ((queueFull || memLow) && !bypassGlobal) {
                    if (!playerEntry.isInformed()) {
                        playerEntry.setInformed(true);

                        if (queueFull) {
                            player.say(MessageType.BLOCK_PLACER_GLOBAL_QUEUE_FULL.format());
                        } else if (memLow) {
                            player.say(MessageType.BLOCK_PLACER_MEMORY_LOW.format());
                        }
                    }

                    waitOn = m_globalWaitMutex;
                    m_globalQueueLocked = true;
                    retry = true;

                    continue;
                }

                if (playerEntry.isInformed()) {
                    playerEntry.setInformed(false);
                }

                boolean wait;
                synchronized (queue) {
                    if (queue.size() >= group.getQueueHardLimit() && !bypass) {
                        wait = true;
                    } else {
                        queue.add(entry);
                        wait = false;
                    }
                }

                if (wait) {
                    if (!m_lockedQueues.contains(player)) {
                        m_lockedQueues.add(player);
                        player.say(MessageType.BLOCK_PLACER_QUEUE_FULL.format());
                    }

                    waitOn = player.getWaitMutex();
                    retry = true;
                    continue;
                }

                if (entry instanceof IBlockPlacerLocationEntry) {
                    IBlockPlacerLocationEntry bpEntry = (IBlockPlacerLocationEntry) entry;
                    String worldName = bpEntry.getWorldName();
                    if (worldName != null) {
                        m_physicsWatcher.addLocation(worldName, bpEntry.getLocation());
                    }
                }
                if (entry instanceof JobEntry) {
                    playerEntry.addJob((JobEntry) entry, true);
                }

            }
        } while (retry);

        return true;
    }

    /**
     * Wait for job to finish
     *
     * @param job
     */
    private void waitForJob(IJobEntry job) {
        if (job instanceof UndoJob) {
            log("Warning: Undo jobs shuld not by canceled, ingoring!");
            return;
        }

        if (job == null) {
            return;
        }

        final int SLEEP = 10;
        int maxWaitTime = 1000 / SLEEP;
        JobStatus status = job.getStatus();
        while (status != JobStatus.Initializing
                && !job.isTaskDone() && maxWaitTime > 0) {
            try {
                Thread.sleep(10);
                maxWaitTime--;
            } catch (InterruptedException ex) {
            }
            status = job.getStatus();
        }

        if (status != JobStatus.Done
                && status != JobStatus.Canceled
                && !job.isTaskDone()) {
            log("-----------------------------------------------------------------------");
            log("Warning: timeout waiting for job to finish. Manual job cancel.");
            log(String.format("Job Id: %1$s, %2$s Done: %3$s Status: %4$s",
                    job.getJobId(), job.getName(), job.isTaskDone(), job.getStatus()));
            log("Send this message to the author of the plugin!");
            log("-----------------------------------------------------------------------");
            job.cancel();
            job.setStatus(JobStatus.Done);
        }
    }

    /**
     * Cancel job
     *
     * @param player
     * @param jobId
     * @return
     */
    @Override
    public int cancelJob(IPlayerEntry player, int jobId) {
        int newSize, result;
        BlockPlacerPlayer playerEntry;
        Queue<IBlockPlacerEntry> queue = null;
        IJobEntry job = null;
        synchronized (m_mutex) {
            if (!m_blocks.containsKey(player)) {
                return 0;
            }

            playerEntry = m_blocks.get(player);
            if (playerEntry != null) {
                job = playerEntry.getJob(jobId);
                if (job instanceof UndoJob) {
                    player.say(MessageType.BLOCK_PLACER_CANCEL_UNDO.format());
                    return 0;
                }

                queue = playerEntry.getQueue();

                if (job != null) {
                    playerEntry.removeJob(job);
                    onJobRemoved(job);
                }
            }
        }

        waitForJob(job);

        synchronized (m_mutex) {
            Queue<IBlockPlacerEntry> filtered = new ArrayDeque<IBlockPlacerEntry>();
            if (queue != null) {
                synchronized (queue) {
                    for (IBlockPlacerEntry entry : queue) {
                        if (entry.getJobId() == jobId) {
                            if (entry instanceof IBlockPlacerLocationEntry) {
                                IBlockPlacerLocationEntry bpEntry = (IBlockPlacerLocationEntry) entry;
                                String worldName = bpEntry.getWorldName();
                                if (worldName != null) {
                                    m_physicsWatcher.removeLocation(worldName, bpEntry.getLocation());
                                }
                            } else if (playerEntry != null && entry instanceof JobEntry) {
                                JobEntry jobEntry = (JobEntry) entry;
                                playerEntry.removeJob(jobEntry);
                                onJobRemoved(jobEntry);
                            }
                        } else {
                            filtered.add(entry);
                        }
                    }
                }

                newSize = filtered.size();
                result = queue.size() - filtered.size();
            } else {
                newSize = 0;
                result = 0;
            }
            IPermissionGroup group = player.getPermissionGroup();
            if (newSize > 0 && playerEntry != null) {
                playerEntry.updateQueue(filtered);
            } else if (newSize == 0) {
                m_blocks.remove(player);
                if (player.getMessaging(MessageSystem.BAR)) {
                    hideProgressBar(player, playerEntry);
                }
            }
            if (newSize == 0 || newSize < group.getQueueSoftLimit()) {
                unlockQueue(player, newSize != 0);
            }
        }
        return result;
    }

    /**
     * Remove all entries for player
     *
     * @param player
     * @return
     */
    @Override
    public int purge(IPlayerEntry player) {
        int result = 0;
        synchronized (m_mutex) {
            if (m_blocks.containsKey(player)) {
                BlockPlacerPlayer playerEntry = m_blocks.get(player);
                Queue<IBlockPlacerEntry> queue = playerEntry.getQueue();
                synchronized (queue) {
                    for (IBlockPlacerEntry entry : queue) {
                        if (entry instanceof IBlockPlacerLocationEntry) {
                            IBlockPlacerLocationEntry bpEntry = (IBlockPlacerLocationEntry) entry;
                            String name = bpEntry.getWorldName();
                            if (name != null) {
                                m_physicsWatcher.removeLocation(name, bpEntry.getLocation());
                            }
                        } else if (entry instanceof JobEntry) {
                            JobEntry jobEntry = (JobEntry) entry;
                            playerEntry.removeJob(jobEntry);
                            onJobRemoved(jobEntry);
                        }
                    }
                }

                IJobEntry[] jobs = playerEntry.getJobs();
                for (IJobEntry job : jobs) {
                    playerEntry.removeJob(job.getJobId());
                    onJobRemoved(job);
                }
                result = queue.size();
                m_blocks.remove(player);                
                if (player.getMessaging(MessageSystem.BAR)) {
                    hideProgressBar(player, playerEntry);
                }
            }
            unlockQueue(player, false);
        }

        return result;
    }

    /**
     * Remove all entries
     *
     * @return Number of purged job entries
     */
    @Override
    public int purgeAll() {
        int result = 0;
        synchronized (m_mutex) {
            for (IPlayerEntry user : getAllPlayers()) {
                result += purge(user);
            }
        }

        return result;
    }

    /**
     * Get all players in log
     *
     * @return players list
     */
    @Override
    public IPlayerEntry[] getAllPlayers() {
        synchronized (m_mutex) {
            return m_blocks.keySet().toArray(new IPlayerEntry[0]);
        }
    }

    /**
     * Gets the number of events for a player
     *
     * @param player player login
     * @return number of stored events
     */
    @Override
    public IBlockPlacerPlayer getPlayerEvents(IPlayerEntry player) {
        synchronized (m_mutex) {
            if (m_blocks.containsKey(player)) {
                return m_blocks.get(player);
            }
            return null;
        }
    }

    /**
     * Gets the player message string
     *
     * @param player player login
     * @return
     */
    public String getPlayerMessage(IPlayerEntry player) {
        BlockPlacerPlayer entry = null;
        synchronized (m_mutex) {
            if (m_blocks.containsKey(player)) {
                entry = m_blocks.get(player);
            }
        }

        boolean bypass = player.isAllowed(Permission.QUEUE_BYPASS);
        IPermissionGroup group = player.getPermissionGroup();
        return getPlayerMessage(entry, group, bypass);
    }

    /**
     * Gets the player message string
     *
     * @param player player login
     * @return
     */
    private String getPlayerMessage(BlockPlacerPlayer player, IPermissionGroup group, boolean bypass) {
        int blocks = 0;
        double speed = 0;
        double time = 0;

        if (player != null) {
            blocks = player.getQueue().size();
            speed = player.getSpeed();
        }
        if (speed > 0) {
            time = blocks / speed;
        }

        if (bypass) {
            return MessageType.CMD_JOBS_SHORT.format(blocks, speed, time);
        }

        int queueHardLimit = group.getQueueHardLimit();
        return MessageType.CMD_JOBS_LONG.format(blocks, queueHardLimit, 100.0 * blocks / queueHardLimit, speed, time);
    }

    /**
     * Remove the player job
     *
     * @param player
     * @param jobEntry
     */
    @Override
    public void removeJob(final IPlayerEntry player, IJobEntry jobEntry) {
        BlockPlacerPlayer playerEntry;
        synchronized (m_mutex) {
            playerEntry = m_blocks.get(player);
        }

        if (playerEntry != null) {
            playerEntry.removeJob(jobEntry);
            onJobRemoved(jobEntry);
        }
    }

    /**
     * Hide the progress bar
     *
     * @param player
     * @param entry
     */
    private void hideProgressBar(IPlayerEntry player, BlockPlacerPlayer entry) {
        if (entry != null) {
            entry.setMaxQueueBlocks(0);
        }

        m_progressDisplay.disableMessage(player);
    }

    /**
     * Set progress bar value
     *
     * @param player
     * @param entry
     * @param bypass
     */
    private void setBar(IPlayerEntry player, BlockPlacerPlayer entry, boolean bypass) {
        int blocks = 0;
        int maxBlocks = 0;
        int jobs = 0;
        double speed = 0;
        double time = 0;
        double percentage = 100;

        if (entry != null) {
            jobs = entry.getJobs().length;
            blocks = entry.getQueue().size();
            maxBlocks = entry.getMaxQueueBlocks();
            speed = entry.getSpeed();
        }
        if (speed > 0) {
            time = blocks / speed;
        }

        int newMax = Math.max(blocks, maxBlocks);
        if (newMax > 0) {
            percentage = 100 - 100 * blocks / newMax;
        }
        if (newMax != maxBlocks && entry != null) {
            entry.setMaxQueueBlocks(newMax);
        }

        m_progressDisplay.setMessage(player, jobs, blocks, newMax, time, speed, percentage);
    }

    /**
     * Fire job removed event
     *
     * @param job
     */
    private void onJobRemoved(IJobEntry job) {
        synchronized (m_jobAddedListeners) {
            for (IBlockPlacerListener listener : m_jobAddedListeners) {
                listener.jobRemoved(job);
            }
        }
        
        AwePlatform.getInstance().getCore().getEventBus().post(new JobRemovedEvent(job));
    }

    /**
     * Show player operation progress and update speed
     *
     * @param playerEntry the player UUID
     * @param entry Player entry
     * @param placedBlocks number of blocks placed in this run
     * @param timeDelte ellapsed time from last run
     * @param talk "tell" the stats on chat
     */
    private void showProgress(IPlayerEntry playerEntry, BlockPlacerPlayer entry,
            int placedBlocks, final long timeDelte,
            final boolean talk) {
        entry.updateSpeed(placedBlocks, timeDelte);

        final IPermissionGroup group = playerEntry.getPermissionGroup();
        boolean bypass = playerEntry.isAllowed(Permission.QUEUE_BYPASS);
        if (entry.getQueue().isEmpty()) {
            if (playerEntry.getMessaging(MessageSystem.BAR)) {
                hideProgressBar(playerEntry, entry);
            }
        } else {
            if (talk && playerEntry.getMessaging(MessageSystem.CHAT)) {
                playerEntry.say(MessageType.CMD_JOBS_PROGRESS_MSG.format(getPlayerMessage(entry, group, bypass)));
            }

            if (playerEntry.getMessaging(MessageSystem.BAR)) {
                setBar(playerEntry, entry, bypass);
            }
        }
    }

    /**
     * Unlock player queue
     *
     * @param player
     */
    private void unlockQueue(final IPlayerEntry player, boolean talk) {
        if (m_lockedQueues.contains(player)) {
            if (talk) {
                player.say(MessageType.BLOCK_PLACER_QUEUE_UNLOCKED.format());
            }
            m_lockedQueues.remove(player);

            Object mutex = player.getWaitMutex();
            synchronized (mutex) {
                mutex.notifyAll();
            }
        }
    }

    /**
     * Wrap action into AsyncWorldEdit job and perform it asynchronicly
     *
     * @param editSession
     * @param player
     * @param jobName
     * @param action
     */
    @Override
    public void performAsAsyncJob(final IThreadSafeEditSession editSession, final IPlayerEntry player, final String jobName,
            final IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException> action) {
        final int jobId = getJobId(player);
        final CancelabeEditSession session = new CancelabeEditSession(editSession, editSession.getMask(), jobId);
        final JobEntry job = new JobEntry(player, session, jobId, jobName);
        addJob(player, job);
        m_scheduler.runTaskAsynchronously(new AsyncTask(session, player, jobName,
                this, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        return action.execute(session);
                    }
                });
    }
    
    
    /**
     * Perform an async command asynchronicly
     *
     * @param editSession
     * @param asyncCommand
     */
    @Override
    public void performAsAsyncJob(final IThreadSafeEditSession editSession, final IAsyncCommand asyncCommand) {
        if (asyncCommand == null) {
            return;
        }
        performAsAsyncJob(editSession, asyncCommand.getPlayer(), asyncCommand.getName(), asyncCommand);
    }
}
