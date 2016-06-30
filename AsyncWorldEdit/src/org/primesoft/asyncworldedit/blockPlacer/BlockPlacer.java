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
package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerListener;
import com.sk89q.worldedit.MaxChangedBlocksException;
import org.bukkit.scheduler.BukkitScheduler;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.worldedit.AsyncTask;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

import java.util.*;
import org.primesoft.asyncworldedit.AsyncWorldEditBukkit;
import static org.primesoft.asyncworldedit.AsyncWorldEditBukkit.log;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerPlayer;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.JobStatus;
import org.primesoft.asyncworldedit.api.configuration.IPermissionGroup;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.progressDisplay.IProgressDisplay;
import org.primesoft.asyncworldedit.api.utils.IAsyncCommand;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;

/**
 *
 * @author SBPrime
 */
public class BlockPlacer implements IBlockPlacer {

    /**
     * Bukkit scheduler
     */
    private final BukkitScheduler m_scheduler;

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
    private final HashMap<IPlayerEntry, IBlockPlacerPlayer> m_blocks;

    /**
     * All locked queues
     */
    private final HashSet<IPlayerEntry> m_lockedQueues;

    /**
     * Global queue max size
     */
    private int m_queueMaxSize;

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
     * List of all job added listeners
     */
    private final List<IBlockPlacerListener> m_jobAddedListeners;

    /**
     * Parent plugin main
     */
    private final AsyncWorldEditBukkit m_plugin;
    
    /**
     * Indicates that the blocks placer is paused
     */
    private boolean m_isPaused = false;

    
    /**
     * Is the blocks placer paused
     * @return 
     */
    @Override
    public boolean isPaused() {
        return m_isPaused;
    }
    
    
    /**
     * Set pause on blocks placer
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
     * @param plugin parent
     */
    public BlockPlacer(AsyncWorldEditBukkit plugin) {
        m_jobAddedListeners = new ArrayList<IBlockPlacerListener>();
        m_lastRunTime = System.currentTimeMillis();
        m_runNumber = 0;
        m_blocks = new HashMap<IPlayerEntry, IBlockPlacerPlayer>();
        m_lockedQueues = new HashSet<IPlayerEntry>();
        m_scheduler = plugin.getServer().getScheduler();
        m_progressDisplay = plugin.getProgressDisplayManager();

        m_plugin = plugin;
        m_physicsWatcher = plugin.getPhysicsWatcher();

        loadConfig();
    }

    /**
     * Reload the AWE configuration
     */
    public final void loadConfig() {
        final BlockPlacer blocPlacer = this;

        long interval = ConfigProvider.getInterval();
        m_talkInterval = ConfigProvider.getQueueTalkInterval();
        m_queueMaxSize = ConfigProvider.getQueueMaxSize();

        if (m_task != null) {
            m_task.queueStop();
        }
        m_task = new BlockPlacerTask(m_plugin, m_scheduler, interval) {
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
        final HashMap<IPlayerEntry, Integer> blocksPlaced = new HashMap<IPlayerEntry, Integer>();
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
            return;
        }

        for (Map.Entry<IPermissionGroup, HashSet<IPlayerEntry>> entry : groups.entrySet()) {
            IPermissionGroup permissionGroup = entry.getKey();
            IPlayerEntry[] keys = entry.getValue().toArray(new IPlayerEntry[0]);

            processQueue(keys, permissionGroup, blocksPlaced, jobsToCancel);
        }

        synchronized (m_mutex) {
            for (Map.Entry<IPlayerEntry, IBlockPlacerPlayer> queueEntry : m_blocks.entrySet()) {
                IPlayerEntry playerEntry = queueEntry.getKey();
                IBlockPlacerPlayer entry = queueEntry.getValue();
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
     * @param playerUUID players to process
     * @param permissionGroup
     * @param blocksPlaced number of blocksplaced for players
     * @param jobsToCancel canceled blocks
     */
    private void processQueue(final IPlayerEntry[] playerUUID,
            IPermissionGroup permissionGroup,
            final HashMap<IPlayerEntry, Integer> blocksPlaced, final List<IJobEntry> jobsToCancel) {
        InOutParam<Integer> seqNumber = InOutParam.Ref(0);
        long startTime = System.currentTimeMillis();
        int blocks = 0;
        boolean process = true;

        int maxTime = permissionGroup.getRendererTime();
        int maxBlocksCount = permissionGroup.getRendererBlocks();

        while (process) {
            IBlockPlacerEntry entry;
            synchronized (m_mutex) {
                entry = fetchBlocks(playerUUID, permissionGroup,
                        seqNumber, blocksPlaced, jobsToCancel);
            }

            if (entry != null) {
                entry.process(this);
                blocks++;

                process = !entry.isDemanding(); //Allow only one demanding task
                process &= maxTime == -1 || (System.currentTimeMillis() - startTime) < maxTime;
                process &= maxBlocksCount == -1 || blocks <= maxBlocksCount;
            } else {
                process = false;
            }
        }
    }

    /**
     * Fetch next block that is going to by placed in this run
     *
     * @param playerNames list of all players
     * @param seqNumber sequence number in player names (everyone is treated
     * equally)
     * @param blocksPlaced number of blocks placed for player
     * @param jobsToCancel jobs to cancel
     * @return fatched block
     */
    private IBlockPlacerEntry fetchBlocks(final IPlayerEntry[] playerNames,
            IPermissionGroup permissionGroup,
            InOutParam<Integer> seqNumber,
            final HashMap<IPlayerEntry, Integer> blocksPlaced,
            final List<IJobEntry> jobsToCancel) {
        if (playerNames == null || playerNames.length == 0) {
            return null;
        }

        int keyPos = seqNumber.getValue();
        IBlockPlacerEntry result = null;

        for (int retry = playerNames.length; result == null && retry > 0; retry--) {
            final IPlayerEntry player = playerNames[keyPos];
            final IBlockPlacerPlayer playerEntry = m_blocks.get(player);
            if (playerEntry != null) {
                Queue<IBlockPlacerEntry> queue = playerEntry.getQueue();
                synchronized (queue) {
                    if (!queue.isEmpty()) {
                        IBlockPlacerEntry entry = queue.poll();
                        if (entry != null) {
                            result = entry;

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
                    m_blocks.remove(playerNames[keyPos]);
                    if (permissionGroup.isBarApiProgressEnabled()) {
                        hideProgressBar(player, playerEntry);
                    }
                }
            } else {
                unlockQueue(player, true);
            }
            keyPos = (keyPos + 1) % playerNames.length;
        }

        seqNumber.setValue(keyPos);
        return result;
    }

    /**
     * stop block logger
     */
    public void stop() {
        m_task.stop();
    }

    /**
     * Get next job id for player
     *
     * @param player
     * @return
     */
    @Override
    public int getJobId(IPlayerEntry player) {
        IBlockPlacerPlayer playerEntry;
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
            IBlockPlacerPlayer playerEntry = m_blocks.get(player);
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
            IBlockPlacerPlayer playerEntry;

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
    public boolean addTasks(IPlayerEntry player, IBlockPlacerEntry entry) {
        if (player == null) {
            return false;
        }

        synchronized (m_mutex) {
            IBlockPlacerPlayer playerEntry;

            if (!m_blocks.containsKey(player)) {
                playerEntry = new BlockPlacerPlayer(player);
                m_blocks.put(player, playerEntry);
            } else {
                playerEntry = m_blocks.get(player);
            }
            Queue<IBlockPlacerEntry> queue = playerEntry.getQueue();

            if (m_lockedQueues.contains(player) && !(entry instanceof JobEntry)) {
                return false;
            }

            boolean bypass = !player.isAllowed(Permission.QUEUE_BYPASS);
            IPermissionGroup group = player.getPermissionGroup();

            int size = 0;
            for (Map.Entry<IPlayerEntry, IBlockPlacerPlayer> queueEntry : m_blocks.entrySet()) {
                size += queueEntry.getValue().getQueue().size();
            }

            bypass |= entry instanceof JobEntry;
            
            //TODO: Add wait for queue here!
            if (m_queueMaxSize > 0 && size > m_queueMaxSize && !bypass) {
                if (!playerEntry.isInformed()) {
                    playerEntry.setInformed(true);
                    player.say(MessageType.BLOCK_PLACER_GLOBAL_QUEUE_FULL.format());
                }

                return false;
            } else {
                if (playerEntry.isInformed()) {
                    playerEntry.setInformed(false);
                }

                synchronized (queue) {
                    queue.add(entry);
                }
                if (entry instanceof IBlockPlacerLocationEntry) {
                    IBlockPlacerLocationEntry bpEntry = (IBlockPlacerLocationEntry) entry;
                    String worldName = bpEntry.getWorldName();
                    if (worldName != null) {
                        m_physicsWatcher.addLocation(worldName, bpEntry.getLocation());
                    }
                }
                if (entry instanceof IJobEntry) {
                    playerEntry.addJob((IJobEntry) entry, true);
                }
                
                //TODO: Add wait for queue here!
                if (queue.size() >= group.getQueueHardLimit() && bypass) {
                    m_lockedQueues.add(player);
                    player.say(MessageType.BLOCK_PLACER_QUEUE_FULL.format());
                    return false;
                }
            }

            return true;
        }
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
            log("Job Id: " + job.getJobId() + ", " + job.getName() + " Done:" + job.isTaskDone() + " Status: " + job.getStatus());
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
        IBlockPlacerPlayer playerEntry;
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
                            } else if (playerEntry != null && entry instanceof IJobEntry) {
                                IJobEntry jobEntry = (IJobEntry) entry;
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
                if (group.isBarApiProgressEnabled()) {
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
                IBlockPlacerPlayer playerEntry = m_blocks.get(player);
                Queue<IBlockPlacerEntry> queue = playerEntry.getQueue();
                synchronized (queue) {
                    for (IBlockPlacerEntry entry : queue) {
                        if (entry instanceof IBlockPlacerLocationEntry) {
                            IBlockPlacerLocationEntry bpEntry = (IBlockPlacerLocationEntry) entry;
                            String name = bpEntry.getWorldName();
                            if (name != null) {
                                m_physicsWatcher.removeLocation(name, bpEntry.getLocation());
                            }
                        } else if (entry instanceof IJobEntry) {
                            IJobEntry jobEntry = (IJobEntry) entry;
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
                IPermissionGroup group = player.getPermissionGroup();
                if (group.isBarApiProgressEnabled()) {
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
        IBlockPlacerPlayer entry = null;
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
    private String getPlayerMessage(IBlockPlacerPlayer player, IPermissionGroup group, boolean bypass) {
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
        IBlockPlacerPlayer playerEntry;
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
    private void hideProgressBar(IPlayerEntry player, IBlockPlacerPlayer entry) {
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
    private void setBar(IPlayerEntry player, IBlockPlacerPlayer entry, boolean bypass) {
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
    private void showProgress(IPlayerEntry playerEntry, IBlockPlacerPlayer entry,
            int placedBlocks, final long timeDelte,
            final boolean talk) {
        entry.updateSpeed(placedBlocks, timeDelte);

        final IPermissionGroup group = playerEntry.getPermissionGroup();
        boolean bypass = playerEntry.isAllowed(Permission.QUEUE_BYPASS);
        if (entry.getQueue().isEmpty()) {
            if (group.isBarApiProgressEnabled()) {
                hideProgressBar(playerEntry, entry);
            }
        } else {
            if (talk && group.isChatProgressEnabled()) {
                playerEntry.say(MessageType.CMD_JOBS_PROGRESS_MSG.format(getPlayerMessage(entry, group, bypass)));
            }

            if (group.isBarApiProgressEnabled()) {
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
    public void performAsAsyncJob(final IThreadSafeEditSession editSession,
            final IPlayerEntry player, final String jobName,
            final IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException> action) {
        final int jobId = getJobId(player);
        final CancelabeEditSession session = new CancelabeEditSession(editSession, editSession.getMask(), jobId);
        final JobEntry job = new JobEntry(player, session, jobId, jobName);
        addJob(player, job);
        m_scheduler.runTaskAsynchronously(m_plugin, new AsyncTask(session, player, jobName,
                this, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        return action.execute(session);
                    }
                });
    }

    @Override
    public void performAsAsyncJob(IThreadSafeEditSession editSession, IAsyncCommand asyncCommand) {
        performAsAsyncJob(editSession, asyncCommand.getPlayer(), asyncCommand.getName(), asyncCommand);
    }
}
