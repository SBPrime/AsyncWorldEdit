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

import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.BarAPIntegrator;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.PhysicsWatch;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.PlayerManager;
import org.primesoft.asyncworldedit.PlayerWrapper;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.permissions.Permission;
import org.primesoft.asyncworldedit.permissions.PermissionManager;
import org.primesoft.asyncworldedit.utils.FuncParamEx;
import org.primesoft.asyncworldedit.utils.InOutParam;
import org.primesoft.asyncworldedit.worldedit.AsyncTask;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.ThreadSafeEditSession;

/**
 *
 * @author SBPrime
 */
public class BlockPlacer implements Runnable {

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
    private final PhysicsWatch m_physicsWatcher;

    /**
     * Current scheduler task
     */
    private final BukkitTask m_task;

    /**
     * Logged events queue (per player)
     */
    private final HashMap<UUID, PlayerEntry> m_blocks;

    /**
     * All locked queues
     */
    private final HashSet<UUID> m_lockedQueues;

    /**
     * Should block places shut down
     */
    private boolean m_shutdown;

    /**
     * Global queue max size
     */
    private final int m_queueMaxSize;

    /**
     * Block placing interval (in ticks)
     */
    private final long m_interval;

    /**
     * Talk interval
     */
    private final int m_talkInterval;

    /**
     * Run number
     */
    private int m_runNumber;

    /**
     * Last run time
     */
    private long m_lastRunTime;

    /**
     * The bar API
     */
    private final BarAPIntegrator m_barAPI;

    /**
     * List of all job added listeners
     */
    private final List<IBlockPlacerListener> m_jobAddedListeners;

    /**
     * Parent plugin main
     */
    private final AsyncWorldEditMain m_plugin;

    /**
     * The player manager
     */
    private final PlayerManager m_playerManager;

    /**
     * Get the physics watcher
     *
     * @return
     */
    public PhysicsWatch getPhysicsWatcher() {
        return m_physicsWatcher;
    }

    /**
     * Initialize new instance of the block placer
     *
     * @param plugin parent
     */
    public BlockPlacer(AsyncWorldEditMain plugin) {
        m_jobAddedListeners = new ArrayList<IBlockPlacerListener>();
        m_lastRunTime = System.currentTimeMillis();
        m_runNumber = 0;
        m_blocks = new HashMap<UUID, PlayerEntry>();
        m_lockedQueues = new HashSet<UUID>();
        m_scheduler = plugin.getServer().getScheduler();
        m_barAPI = plugin.getBarAPI();
        m_interval = ConfigProvider.getInterval();
        m_task = m_scheduler.runTaskTimer(plugin, this,
                m_interval, m_interval);
        m_plugin = plugin;

        m_talkInterval = ConfigProvider.getQueueTalkInterval();
        m_queueMaxSize = ConfigProvider.getQueueMaxSize();
        m_physicsWatcher = plugin.getPhysicsWatcher();
        m_playerManager = plugin.getPlayerManager();
    }

    /**
     * Add event listener
     *
     * @param listener
     */
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
    @Override
    public void run() {
        long enterFunctionTime = System.currentTimeMillis();
        final long timeDelte = enterFunctionTime - m_lastRunTime;

        boolean talk = false;
        final List<JobEntry> jobsToCancel = new ArrayList<JobEntry>();
        //Number of blocks placed for player
        final HashMap<UUID, Integer> blocksPlaced = new HashMap<UUID, Integer>();
        final HashMap<PermissionGroup, HashSet<UUID>> groups = new HashMap<PermissionGroup, HashSet<UUID>>();

        synchronized (this) {
            final UUID[] keys = m_blocks.keySet().toArray(new UUID[0]);
            for (UUID key : keys) {
                PlayerWrapper player = m_playerManager.getPlayer(key);
                PermissionGroup group = PermissionManager.getPermissionGroup(player != null ? player.getPlayer() : null);

                HashSet<UUID> uuids;
                if (!groups.containsKey(group)) {
                    uuids = new HashSet<UUID>();
                    uuids.add(key);
                    groups.put(group, uuids);
                } else {
                    uuids = groups.get(group);
                    if (!uuids.contains(key)) //Should not happen but better be safe then sorry ;)
                    {
                        uuids.add(key);
                    }
                }
            }
            m_runNumber++;
        }
        if (m_runNumber > m_talkInterval) {
            m_runNumber = 0;
            talk = true;
        }

        synchronized (this) {
            if (m_shutdown) {
                stop();
            }
        }

        for (Map.Entry<PermissionGroup, HashSet<UUID>> entry : groups.entrySet()) {
            PermissionGroup permissionGroup = entry.getKey();
            UUID[] keys = entry.getValue().toArray(new UUID[0]);

            processQueue(keys, permissionGroup, blocksPlaced, jobsToCancel);
        }

        synchronized (this) {
            for (Map.Entry<UUID, PlayerEntry> queueEntry : m_blocks.entrySet()) {
                UUID playerUUID = queueEntry.getKey();
                PlayerEntry entry = queueEntry.getValue();
                Integer cnt = blocksPlaced.get(playerUUID);

                showProgress(playerUUID, entry, cnt != null ? cnt : 0, timeDelte, talk);
            }
        }

        for (JobEntry job : jobsToCancel) {
            job.setStatus(JobEntry.JobStatus.Done);
            onJobRemoved(job);
        }

        m_lastRunTime = enterFunctionTime;
    }

    /**
     * Process queued blocks
     *
     * @param playerUUID players to process
     * @param maxTime maximum time spend placing blocks
     * @param maxBlocksCount maximum blocks placed
     * @param blocksPlaced number of blocksplaced for players
     * @param jobsToCancel canceled blocks
     */
    private void processQueue(final UUID[] playerUUID,
            PermissionGroup permissionGroup,
            final HashMap<UUID, Integer> blocksPlaced, final List<JobEntry> jobsToCancel) {
        InOutParam<Integer> seqNumber = InOutParam.Ref(0);
        long startTime = System.currentTimeMillis();
        int blocks = 0;
        boolean process = true;

        int maxTime = permissionGroup.getRendererTime();
        int maxBlocksCount = permissionGroup.getRendererBlocks();

        while (process) {
            BlockPlacerEntry entry;
            synchronized (this) {
                entry = fetchBlocks(playerUUID, permissionGroup,
                        seqNumber, blocksPlaced, jobsToCancel);
            }

            if (entry != null) {
                entry.Process(this);
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
    private BlockPlacerEntry fetchBlocks(final UUID[] playerNames,
            PermissionGroup permissionGroup,
            InOutParam<Integer> seqNumber,
            final HashMap<UUID, Integer> blocksPlaced,
            final List<JobEntry> jobsToCancel) {
        if (playerNames == null || playerNames.length == 0) {
            return null;
        }

        int keyPos = seqNumber.getValue();
        BlockPlacerEntry result = null;

        for (int retry = playerNames.length; result == null && retry > 0; retry--) {
            final UUID player = playerNames[keyPos];
            final PlayerEntry playerEntry = m_blocks.get(player);
            if (playerEntry != null) {
                Queue<BlockPlacerEntry> queue = playerEntry.getQueue();
                synchronized (queue) {
                    if (!queue.isEmpty()) {
                        BlockPlacerEntry entry = queue.poll();
                        if (entry != null) {
                            result = entry;

                            if (blocksPlaced.containsKey(player)) {
                                blocksPlaced.put(player, blocksPlaced.get(player) + 1);
                            } else {
                                blocksPlaced.put(player, 1);
                            }
                        }
                    } else {
                        for (JobEntry job : playerEntry.getJobs()) {
                            JobEntry.JobStatus jStatus = job.getStatus();
                            if (jStatus == JobEntry.JobStatus.Done
                                    || jStatus == JobEntry.JobStatus.Waiting) {
                                jobsToCancel.add(job);
                            }
                        }

                        for (JobEntry job : jobsToCancel) {
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
                    Player p = AsyncWorldEditMain.getPlayer(player);
                    if (permissionGroup.isBarApiProgressEnabled()) {
                        hideProgressBar(p, playerEntry);
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
     * Queue stop command
     */
    public void queueStop() {
        m_shutdown = true;
    }

    /**
     * stop block logger
     */
    public void stop() {
        m_task.cancel();
    }

    /**
     * Get next job id for player
     *
     * @param player
     * @return
     */
    public int getJobId(UUID player) {
        PlayerEntry playerEntry;
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                playerEntry = m_blocks.get(player);
            } else {
                playerEntry = new PlayerEntry();
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
    public JobEntry getJob(UUID player, int jobId) {
        synchronized (this) {
            if (!m_blocks.containsKey(player)) {
                return null;
            }
            PlayerEntry playerEntry = m_blocks.get(player);
            return playerEntry.getJob(jobId);
        }
    }

    /**
     * Add new job for player
     *
     * @param player player UUID
     * @param job the job
     */
    public void addJob(UUID player, JobEntry job) {
        synchronized (this) {
            PlayerEntry playerEntry;

            if (!m_blocks.containsKey(player)) {
                playerEntry = new PlayerEntry();
                m_blocks.put(player, playerEntry);
            } else {
                playerEntry = m_blocks.get(player);
            }
            playerEntry.addJob(job);
        }

        synchronized (m_jobAddedListeners) {
            for (IBlockPlacerListener listener : m_jobAddedListeners) {
                listener.jobAdded(job);
            }
        }
    }

    /**
     * Add task to perform in async mode
     *
     * @param player
     * @param entry
     * @return
     */
    public boolean addTasks(UUID player, BlockPlacerEntry entry) {
        synchronized (this) {
            PlayerEntry playerEntry;

            if (!m_blocks.containsKey(player)) {
                playerEntry = new PlayerEntry();
                m_blocks.put(player, playerEntry);
            } else {
                playerEntry = m_blocks.get(player);
            }
            Queue<BlockPlacerEntry> queue = playerEntry.getQueue();

            if (m_lockedQueues.contains(player)) {
                return false;
            }

            boolean bypass = !PermissionManager.isAllowed(AsyncWorldEditMain.getPlayer(player), Permission.QUEUE_BYPASS);
            PermissionGroup group = PermissionManager.getPermissionGroup(AsyncWorldEditMain.getPlayer(player));

            int size = 0;
            for (Map.Entry<UUID, PlayerEntry> queueEntry : m_blocks.entrySet()) {
                size += queueEntry.getValue().getQueue().size();
            }

            bypass |= entry instanceof JobEntry;
            if (m_queueMaxSize > 0 && size > m_queueMaxSize && !bypass) {
                if (player == null) {
                    return false;
                }

                if (!playerEntry.isInformed()) {
                    playerEntry.setInformed(true);
                    AsyncWorldEditMain.say(player, "Out of space on AWE block queue.");
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
                if (entry instanceof JobEntry) {
                    playerEntry.addJob((JobEntry) entry);
                }
                if (queue.size() >= group.getQueueHardLimit() && bypass) {
                    m_lockedQueues.add(player);
                    AsyncWorldEditMain.say(player, "Your block queue is full. Wait for items to finish drawing.");
                    return false;
                }
            }

            return true;
        }
    }

    /**
     * Cancel job
     *
     * @param player
     * @param job
     */
    public void cancelJob(UUID player, JobEntry job) {
        if (job instanceof UndoJob) {
            AsyncWorldEditMain.say(player, "Warning: Undo jobs shuld not by canceled, ingoring!");
            return;
        }
        cancelJob(player, job.getJobId());
    }

    /**
     * Wait for job to finish
     *
     * @param job
     */
    private void waitForJob(JobEntry job) {
        if (job instanceof UndoJob) {
            AsyncWorldEditMain.log("Warning: Undo jobs shuld not by canceled, ingoring!");
            return;
        }

        final int SLEEP = 10;
        int maxWaitTime = 1000 / SLEEP;
        JobEntry.JobStatus status = job.getStatus();
        while (status != JobEntry.JobStatus.Initializing
                && !job.isTaskDone() && maxWaitTime > 0) {
            try {
                Thread.sleep(10);
                maxWaitTime--;
            } catch (InterruptedException ex) {
            }
            status = job.getStatus();
        }

        if (status != JobEntry.JobStatus.Done
                && !job.isTaskDone()) {
            AsyncWorldEditMain.log("-----------------------------------------------------------------------");
            AsyncWorldEditMain.log("Warning: timeout waiting for job to finish. Manual job cancel.");
            AsyncWorldEditMain.log("Job Id: " + job.getJobId() + ", " + job.getName() + " Done:" + job.isTaskDone() + " Status: " + job.getStatus());
            AsyncWorldEditMain.log("Send this message to the author of the plugin!");
            AsyncWorldEditMain.log("-----------------------------------------------------------------------");
            job.cancel();
            job.setStatus(JobEntry.JobStatus.Done);
        }
    }

    /**
     * Cancel job
     *
     * @param player
     * @param jobId
     * @return
     */
    public int cancelJob(UUID player, int jobId) {
        int newSize, result;
        PlayerEntry playerEntry;
        Queue<BlockPlacerEntry> queue;
        JobEntry job;
        synchronized (this) {
            if (!m_blocks.containsKey(player)) {
                return 0;
            }
            playerEntry = m_blocks.get(player);
            job = playerEntry.getJob(jobId);
            if (job instanceof UndoJob) {
                AsyncWorldEditMain.say(player, "Warning: Undo jobs shuld not by canceled, ingoring!");
                return 0;
            }

            queue = playerEntry.getQueue();
            playerEntry.removeJob(job);
            onJobRemoved(job);
        }
        waitForJob(job);

        synchronized (this) {
            Queue<BlockPlacerEntry> filtered = new ArrayDeque<BlockPlacerEntry>();
            synchronized (queue) {
                for (BlockPlacerEntry entry : queue) {
                    if (entry.getJobId() == jobId) {
                        if (entry instanceof IBlockPlacerLocationEntry) {
                            IBlockPlacerLocationEntry bpEntry = (IBlockPlacerLocationEntry) entry;
                            String worldName = bpEntry.getWorldName();
                            if (worldName != null) {
                                m_physicsWatcher.removeLocation(worldName, bpEntry.getLocation());
                            }
                        } else if (entry instanceof JobEntry) {
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
            Player p = AsyncWorldEditMain.getPlayer(player);
            PermissionGroup group = PermissionManager.getPermissionGroup(p);
            if (newSize > 0) {
                playerEntry.updateQueue(filtered);
            } else {
                m_blocks.remove(player);
                if (group.isBarApiProgressEnabled()) {
                    hideProgressBar(p, playerEntry);
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
    public int purge(UUID player) {
        int result = 0;
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                PlayerEntry playerEntry = m_blocks.get(player);
                Queue<BlockPlacerEntry> queue = playerEntry.getQueue();
                synchronized (queue) {
                    for (BlockPlacerEntry entry : queue) {
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

                Collection<JobEntry> jobs = playerEntry.getJobs();
                for (JobEntry job : jobs.toArray(new JobEntry[0])) {
                    playerEntry.removeJob(job.getJobId());
                    onJobRemoved(job);
                }
                result = queue.size();
                m_blocks.remove(player);
                Player p = AsyncWorldEditMain.getPlayer(player);
                PermissionGroup group = PermissionManager.getPermissionGroup(p);
                if (group.isBarApiProgressEnabled()) {
                    hideProgressBar(p, playerEntry);
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
    public int purgeAll() {
        int result = 0;
        synchronized (this) {
            for (UUID user : getAllPlayers()) {
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
    public UUID[] getAllPlayers() {
        synchronized (this) {
            return m_blocks.keySet().toArray(new UUID[0]);
        }
    }

    /**
     * Gets the number of events for a player
     *
     * @param player player login
     * @return number of stored events
     */
    public PlayerEntry getPlayerEvents(UUID player) {
        synchronized (this) {
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
    public String getPlayerMessage(UUID player) {
        PlayerEntry entry = null;
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                entry = m_blocks.get(player);
            }
        }

        boolean bypass = PermissionManager.isAllowed(AsyncWorldEditMain.getPlayer(player),
                Permission.QUEUE_BYPASS);
        PermissionGroup group = PermissionManager.getPermissionGroup(AsyncWorldEditMain.getPlayer(player));
        return getPlayerMessage(entry, group, bypass);
    }

    /**
     * Gets the player message string
     *
     * @param player player login
     * @return
     */
    private String getPlayerMessage(PlayerEntry player, PermissionGroup group, boolean bypass) {
        final String format = ChatColor.WHITE + "%d"
                + ChatColor.YELLOW + " out of " + ChatColor.WHITE + "%d"
                + ChatColor.YELLOW + " blocks (" + ChatColor.WHITE + "%.2f%%"
                + ChatColor.YELLOW + ") queued. Placing speed: " + ChatColor.WHITE + "%.2fbps"
                + ChatColor.YELLOW + ", " + ChatColor.WHITE + "%.2fs"
                + ChatColor.YELLOW + " left.";
        final String formatShort = ChatColor.WHITE + "%d"
                + ChatColor.YELLOW + " blocks queued. Placing speed: " + ChatColor.WHITE + "%.2fbps"
                + ChatColor.YELLOW + ", " + ChatColor.WHITE + "%.2fs"
                + ChatColor.YELLOW + " left.";

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
            return String.format(formatShort, blocks, speed, time);
        }

        int queueHardLimit = group.getQueueHardLimit();
        return String.format(format, blocks, queueHardLimit, 100.0 * blocks / queueHardLimit, speed, time);
    }

    /**
     * Remove the player job
     *
     * @param player
     * @param jobEntry
     */
    public void removeJob(final UUID player, JobEntry jobEntry) {
        PlayerEntry playerEntry;
        synchronized (this) {
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
     * @param p
     */
    private void hideProgressBar(Player player, PlayerEntry entry) {
        if (entry != null) {
            entry.setMaxQueueBlocks(0);
        }

        m_barAPI.disableMessage(player);
    }

    /**
     * Set progress bar value
     *
     * @param player
     * @param entry
     * @param bypass
     */
    private void setBar(Player player, PlayerEntry entry, boolean bypass) {
        final String format = ChatColor.YELLOW + "Jobs: " + ChatColor.WHITE + "%d"
                + ChatColor.YELLOW + ", Placing speed: " + ChatColor.WHITE + "%.2fbps"
                + ChatColor.YELLOW + ", " + ChatColor.WHITE + "%.2fs"
                + ChatColor.YELLOW + " left.";

        int blocks = 0;
        int maxBlocks = 0;
        int jobs = 0;
        double speed = 0;
        double time = 0;
        double percentage = 100;

        if (entry != null) {
            jobs = entry.getJobs().size();
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

        String message = String.format(format, jobs, speed, time);
        m_barAPI.setMessage(player, message, percentage);
    }

    /**
     * Fire job removed event
     *
     * @param job
     */
    private void onJobRemoved(JobEntry job) {
        synchronized (m_jobAddedListeners) {
            for (IBlockPlacerListener listener : m_jobAddedListeners) {
                listener.jobRemoved(job);
            }
        }
    }

    /**
     * Show player operation progress and update speed
     *
     * @param playerUuid the player UUID
     * @param entry Player entry
     * @param placedBlocks number of blocks placed in this run
     * @param timeDelte ellapsed time from last run
     * @param talk "tell" the stats on chat
     */
    private void showProgress(UUID playerUuid, PlayerEntry entry,
            int placedBlocks, final long timeDelte,
            final boolean talk) {
        entry.updateSpeed(placedBlocks, timeDelte);

        final Player p = AsyncWorldEditMain.getPlayer(playerUuid);        
        final PermissionGroup group = PermissionManager.getPermissionGroup(p);
        boolean bypass = PermissionManager.isAllowed(p, Permission.QUEUE_BYPASS);
        if (entry.getQueue().isEmpty()) {            
            if (group.isBarApiProgressEnabled()) {
                hideProgressBar(p, entry);
            }
        } else {
            if (talk && group.isChatProgressEnabled()) {
                AsyncWorldEditMain.say(p, ChatColor.YELLOW + "[AWE] You have "
                        + getPlayerMessage(entry, group, bypass));
            }

            if (group.isBarApiProgressEnabled()) {
                setBar(p, entry, bypass);
            }
        }
    }

    /**
     * Unlock player queue
     *
     * @param player
     */
    private void unlockQueue(final UUID player, boolean talk) {
        if (m_lockedQueues.contains(player)) {
            if (talk) {
                AsyncWorldEditMain.say(player, "Your block queue is unlocked. You can use WorldEdit.");
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
    public void PerformAsAsyncJob(final ThreadSafeEditSession editSession,
            final UUID player, final String jobName,
            final FuncParamEx<Integer, CancelabeEditSession, MaxChangedBlocksException> action) {
        final int jobId = getJobId(player);
        final CancelabeEditSession session = new CancelabeEditSession(editSession, editSession.getMask(), jobId);
        final JobEntry job = new JobEntry(player, session, jobId, jobName);
        addJob(player, job);
        m_scheduler.runTaskAsynchronously(m_plugin, new AsyncTask(session, player, jobName,
                this, job) {
                    @Override
                    public int task(CancelabeEditSession session)
                    throws MaxChangedBlocksException {
                        return action.Execute(session);
                    }
                });
    }
}