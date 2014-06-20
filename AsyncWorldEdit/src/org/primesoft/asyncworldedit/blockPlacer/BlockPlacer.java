/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
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
package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.BarAPIntegrator;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PhysicsWatch;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.Permission;
import org.primesoft.asyncworldedit.PermissionManager;
import org.primesoft.asyncworldedit.utils.InOutParam;

/**
 *
 * @author SBPrime
 */
public class BlockPlacer implements Runnable {
    /**
     * Maximum number of retries for get blocks.
     * If no block get is exeuted for X the get task stops.
     * If a block get is executed, this is the number of retries
     * for dequeuing operations.
     */
    private final int MAX_RETRIES = 200;

    /**
     * MTA mutex
     */
    private final Object m_mutex = new Object();

    /**
     * The physics watcher
     */
    private final PhysicsWatch m_physicsWatcher;

    /**
     * Bukkit scheduler
     */
    private final BukkitScheduler m_scheduler;

    /**
     * Current scheduler task
     */
    private final BukkitTask m_task;

    /**
     * Current scheduler get task
     */
    private BukkitTask m_getTask;

    /**
     * Number of get task run remaining
     */
    private int m_getTaskRunsRemaining;

    /**
     * Logged events queue (per player)
     */
    private final HashMap<UUID, PlayerEntry> m_blocks;

    /**
     * Get blocks requests
     */
    private final List<BlockPlacerEntry> m_getBlocks = new ArrayList<BlockPlacerEntry>();

    /**
     * All locked queues
     */
    private final HashSet<UUID> m_lockedQueues;

    /**
     * Should block places shut down
     */
    private boolean m_shutdown;

    /**
     * Player block queue hard limit (max bloks count)
     */
    private final int m_queueHardLimit;

    /**
     * Player block queue soft limit (minimum number of blocks before queue is
     * unlocked)
     */
    private final int m_queueSoftLimit;

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
     * The main thread
     */
    private Thread m_mainThread;

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
    private final PluginMain m_plugin;

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
    public BlockPlacer(PluginMain plugin) {
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

        startGetTask();

        m_talkInterval = ConfigProvider.getQueueTalkInterval();
        m_queueHardLimit = ConfigProvider.getQueueHardLimit();
        m_queueSoftLimit = ConfigProvider.getQueueSoftLimit();
        m_queueMaxSize = ConfigProvider.getQueueMaxSize();
        m_physicsWatcher = plugin.getPhysicsWatcher();
    }

    
    /**
     * Start the get task
     */
    private void startGetTask() {
        final Runnable func = new Runnable() {
                @Override
                public void run() {
                    m_mainThread = Thread.currentThread();
                    processGet();
                }
        };
        synchronized (m_mutex) {
            m_getTaskRunsRemaining = MAX_RETRIES;
            if (m_getTask != null) {
                return;
            }        
            m_getTask = m_scheduler.runTaskTimer(m_plugin, func, 1, 1);
        }
    }

    
    /**
     * Add event listener
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
     * Process the get requests
     */
    public void processGet() {
        boolean run = true;

        boolean processed = false;        
        for (int i = 0; i < MAX_RETRIES && run; i++) {
            run = false;
            
            final BlockPlacerEntry[] tasks;
            synchronized (m_getBlocks) {
                tasks = m_getBlocks.toArray(new BlockPlacerEntry[0]);
                m_getBlocks.clear();
            }

            for (BlockPlacerEntry t : tasks) {
                t.Process(this);
            }
            if (tasks.length > 0) {
                processed = true;
                run = true;
                try {
                    //Force thread release!
                    Thread.sleep(1);                    
                }
                catch (InterruptedException ex) {
                }
            }
        }

        if (!processed) {            
            synchronized (m_mutex) {
                m_getTaskRunsRemaining--;
                if (m_getTaskRunsRemaining <= 0 && m_getTask != null) {
                    m_getTask.cancel();
                    m_getTask = null;
                }
            }
        }
    }

    /**
     * Block placer main loop
     */
    @Override
    public void run() {
        m_mainThread = Thread.currentThread();

        long enterFunctionTime = System.currentTimeMillis();
        final long timeDelte = enterFunctionTime - m_lastRunTime;

        boolean talk = false;
        final List<JobEntry> jobsToCancel = new ArrayList<JobEntry>();

        final UUID[] keys, vipKeys;
        final HashSet<UUID> vips;
        final int blockCount, blockCountVip;
        final int time, timeVip;
        //Number of blocks placed for player
        final HashMap<UUID, Integer> blocksPlaced = new HashMap<UUID, Integer>();

        synchronized (this) {
            keys = m_blocks.keySet().toArray(new UUID[0]);

            vips = getVips(keys);
            vipKeys = vips.toArray(new UUID[0]);

            blockCount = ConfigProvider.getBlockCount();
            blockCountVip = ConfigProvider.getVipBlockCount();
            time = ConfigProvider.getBlockPlacingTime();
            timeVip = ConfigProvider.getVipBlockPlacingTime();

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
        
        processQueue(keys, time, blockCount, blocksPlaced, jobsToCancel);
        processQueue(vipKeys, timeVip, blockCountVip, blocksPlaced, jobsToCancel);

        synchronized (this) {
            for (Map.Entry<UUID, PlayerEntry> queueEntry : m_blocks.entrySet()) {
                UUID playerUUID = queueEntry.getKey();
                PlayerEntry entry = queueEntry.getValue();
                Integer cnt = blocksPlaced.get(playerUUID);

                showProgress(playerUUID, entry, cnt != null ? cnt : 0, timeDelte, talk);
            }
        }

        for (JobEntry job
                : jobsToCancel) {
            job.setStatus(JobEntry.JobStatus.Done);
            onJobRemoved(job);
        }

        m_lastRunTime = enterFunctionTime;
    }

    
    /**
     * Process queued blocks
     * @param playerUUID players to process
     * @param maxTime maximum time spend placing blocks
     * @param maxBlocksCount maximum blocks placed
     * @param blocksPlaced number of blocksplaced for players
     * @param jobsToCancel canceled blocks
     */
    private void processQueue(final UUID[] playerUUID, 
            final int maxTime, final int maxBlocksCount,
            final HashMap<UUID, Integer> blocksPlaced, final List<JobEntry> jobsToCancel) {
        InOutParam<Integer> seqNumber = InOutParam.Ref(0);
        long startTime = System.currentTimeMillis();
        int blocks = 0;
        boolean process = true;
        while (process) {
            BlockPlacerEntry entry;
            synchronized (this) {
                entry = fetchBlocks(playerUUID, seqNumber, blocksPlaced, jobsToCancel);
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
                                         InOutParam<Integer> seqNumber,
                                         final HashMap<UUID, Integer> blocksPlaced,
                                         final List<JobEntry> jobsToCancel) {
        if (playerNames == null || playerNames.length == 0) {
            return null;
        }

        int keyPos = seqNumber.getValue();
        BlockPlacerEntry result = null;

        for (int retry = playerNames.length; result == null && retry > 0; retry --) {
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
                if (size < m_queueSoftLimit) {
                    unlockQueue(player, true);
                }
                if (size == 0 && !playerEntry.hasJobs()) {
                    m_blocks.remove(playerNames[keyPos]);
                    Player p = PluginMain.getPlayer(player);
                    if (PermissionManager.isAllowed(p, Permission.PROGRESS_BAR)) {
                        m_barAPI.disableMessage(p);
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
        synchronized (m_mutex) {
            if (m_getTask != null) {
                m_getTask.cancel();
                m_getTask = null;
            }
        }

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
            playerEntry.addJob((JobEntry) job);
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

            boolean bypass = !PermissionManager.isAllowed(PluginMain.getPlayer(player), Permission.QUEUE_BYPASS);
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
                    PluginMain.say(player, "Out of space on AWE block queue.");
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
                if (queue.size() >= m_queueHardLimit && bypass) {
                    m_lockedQueues.add(player);
                    PluginMain.say(player, "Your block queue is full. Wait for items to finish drawing.");
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
            PluginMain.say(player, "Warning: Undo jobs shuld not by canceled, ingoring!");
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
            PluginMain.log("Warning: Undo jobs shuld not by canceled, ingoring!");
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
            }
            catch (InterruptedException ex) {
            }
            status = job.getStatus();
        }

        if (status != JobEntry.JobStatus.Done
                && !job.isTaskDone()) {
            PluginMain.log("-----------------------------------------------------------------------");
            PluginMain.log("Warning: timeout waiting for job to finish. Manual job cancel.");
            PluginMain.log("Job Id: " + job.getJobId() + ", " + job.getName() + " Done:" + job.isTaskDone() + " Status: " + job.getStatus());
            PluginMain.log("Send this message to the author of the plugin!");
            PluginMain.log("-----------------------------------------------------------------------");
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
                PluginMain.say(player, "Warning: Undo jobs shuld not by canceled, ingoring!");
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
            if (newSize > 0) {
                playerEntry.updateQueue(filtered);
            } else {
                m_blocks.remove(player);
                Player p = PluginMain.getPlayer(player);
                if (PermissionManager.isAllowed(p, Permission.PROGRESS_BAR)) {
                    m_barAPI.disableMessage(p);
                }
            }
            if (newSize == 0 || newSize < m_queueSoftLimit) {
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
                Player p = PluginMain.getPlayer(player);
                if (PermissionManager.isAllowed(p, Permission.PROGRESS_BAR)) {
                    m_barAPI.disableMessage(p);
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

        boolean bypass = PermissionManager.isAllowed(PluginMain.getPlayer(player),
                Permission.QUEUE_BYPASS);
        return getPlayerMessage(entry, bypass);
    }

    /**
     * Gets the player message string
     *
     * @param player player login
     * @return
     */
    private String getPlayerMessage(PlayerEntry player, boolean bypass) {
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

        return String.format(format, blocks, m_queueHardLimit, 100.0 * blocks / m_queueHardLimit, speed, time);
    }

    /**
     * Filter player names for vip players (AWE.user.vip-queue)
     *
     * @param playerNames
     * @return
     */
    private HashSet<UUID> getVips(UUID[] playerNames) {
        if (playerNames == null || playerNames.length == 0) {
            return new HashSet<UUID>();
        }

        HashSet<UUID> result = new HashSet<UUID>(playerNames.length);

        for (UUID uuid : playerNames) {
            Player player = PluginMain.getPlayer(uuid);
            if (player == null) {
                continue;
            }

            if (PermissionManager.isAllowed(player, Permission.QUEUE_VIP)
                    && !result.contains(uuid)) {
                result.add(uuid);
            }
        }

        return result;
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
     * Add new get block task (high priority tasks!)
     *
     * @param block
     */
    public void addGetTask(BlockPlacerEntry block) {
        synchronized (m_getBlocks) {
            m_getBlocks.add(block);
        }

        startGetTask();
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
     * Set progress bar value
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
        int jobs = 0;
        double speed = 0;
        double time = 0;
        double percentage = 100;

        if (entry != null) {
            jobs = entry.getJobs().size();
            blocks = entry.getQueue().size();
            speed = entry.getSpeed();
        }
        if (speed > 0) {
            time = blocks / speed;
            double max = 60;
            while (time > max * 1.05) {
                max *= 2;
            }
            percentage = 100 - Math.min(100, 100 * time / max);
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

        final Player p = PluginMain.getPlayer(playerUuid);
        boolean bypass = PermissionManager.isAllowed(p, Permission.QUEUE_BYPASS);
        if (entry.getQueue().isEmpty()) {
            if (PermissionManager.isAllowed(p, Permission.PROGRESS_BAR)) {
                m_barAPI.disableMessage(p);
            }
        } else {
            if (talk && PermissionManager.isAllowed(p, Permission.TALKATIVE_QUEUE)) {
                PluginMain.say(p, ChatColor.YELLOW + "[AWE] You have "
                        + getPlayerMessage(entry, bypass));
            }

            if (PermissionManager.isAllowed(p, Permission.PROGRESS_BAR)) {
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
                PluginMain.say(player, "Your block queue is unlocked. You can use WorldEdit.");
            }
            m_lockedQueues.remove(player);
        }
    }
}
