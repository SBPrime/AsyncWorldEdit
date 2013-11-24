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

import java.util.*;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.BarAPIntegrator;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PermissionManager;
import org.primesoft.asyncworldedit.PhysicsWatch;
import org.primesoft.asyncworldedit.PluginMain;

/**
 *
 * @author SBPrime
 */
public class BlockPlacer implements Runnable {

    /**
     * Maximum number of retries
     */
    private final int MAX_RETRIES = 200;
    /**
     * The physics watcher
     */
    private final PhysicsWatch m_physicsWatcher;
    /**
     * Bukkit scheduler
     */
    private BukkitScheduler m_scheduler;
    /**
     * Current scheduler task
     */
    private BukkitTask m_task;
    /**
     * Current scheduler get task
     */
    private BukkitTask m_getTask;
    /**
     * Logged events queue (per player)
     */
    private HashMap<String, PlayerEntry> m_blocks;
    /**
     * Get blocks requests
     */
    private final List<BlockPlacerGetBlockEntry> m_getBlocks = new ArrayList<BlockPlacerGetBlockEntry>();
    /**
     * All locked queues
     */
    private HashSet<String> m_lockedQueues;
    /**
     * Should block places shut down
     */
    private boolean m_shutdown;
    /**
     * Player block queue hard limit (max bloks count)
     */
    private int m_queueHardLimit;
    /**
     * Player block queue soft limit (minimum number of blocks before queue is
     * unlocked)
     */
    private int m_queueSoftLimit;
    /**
     * Global queue max size
     */
    private int m_queueMaxSize;
    /**
     * Block placing interval (in ticks)
     */
    private long m_interval;
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
     * The main thread
     */
    private Thread m_mainThread;
    /**
     * The bar API
     */
    private final BarAPIntegrator m_barAPI;

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
     * @param blockLogger instance block logger
     */
    public BlockPlacer(PluginMain plugin) {
        m_lastRunTime = System.currentTimeMillis();
        m_runNumber = 0;
        m_blocks = new HashMap<String, PlayerEntry>();
        m_lockedQueues = new HashSet<String>();
        m_scheduler = plugin.getServer().getScheduler();
        m_barAPI = plugin.getBarAPI();
        m_interval = ConfigProvider.getInterval();
        m_task = m_scheduler.runTaskTimer(plugin, this,
                m_interval, m_interval);

        m_getTask = m_scheduler.runTaskTimer(plugin, new Runnable() {

            @Override
            public void run() {
                m_mainThread = Thread.currentThread();
                processGet();
            }
        }, 1, 1);

        m_talkInterval = ConfigProvider.getQueueTalkInterval();
        m_queueHardLimit = ConfigProvider.getQueueHardLimit();
        m_queueSoftLimit = ConfigProvider.getQueueSoftLimit();
        m_queueMaxSize = ConfigProvider.getQueueMaxSize();
        m_physicsWatcher = plugin.getPhysicsWatcher();
    }

    /**
     * Process the get requests
     */
    public void processGet() {
        boolean run = true;
        for (int i = 0; i < MAX_RETRIES && run; i++) {
            final BlockPlacerGetBlockEntry[] tasks;
            synchronized (m_getBlocks) {
                tasks = m_getBlocks.toArray(new BlockPlacerGetBlockEntry[0]);
                m_getBlocks.clear();
            }

            for (BlockPlacerGetBlockEntry t : tasks) {
                t.Process(this);
            }
            run = tasks.length > 0;
            try {
                //Force thread release!
                Thread.sleep(1);
            } catch (InterruptedException ex) {
            }
        }
    }

    /**
     * Block placer main loop
     */
    @Override
    public void run() {
        m_mainThread = Thread.currentThread();

        long now = System.currentTimeMillis();
        List<BlockPlacerEntry> entries = new ArrayList<BlockPlacerEntry>(ConfigProvider.getBlockCount() + ConfigProvider.getVipBlockCount());
        boolean added = false;
        boolean retry = true;

        synchronized (this) {
            final String[] keys = m_blocks.keySet().toArray(new String[0]);

            final HashSet<String> vips = getVips(keys);
            final String[] vipKeys = vips.toArray(new String[0]);

            final int blockCount = ConfigProvider.getBlockCount();
            final int blockCountVip = ConfigProvider.getVipBlockCount();
            final HashMap<String, Integer> blocksPlaced = new HashMap<String, Integer>();

            added |= fetchBlocks(blockCount, keys, entries, blocksPlaced);
            added |= fetchBlocks(blockCountVip, vipKeys, entries, blocksPlaced);

            if (!added && m_shutdown) {
                stop();
            }

            m_runNumber++;
            boolean talk = false;
            if (m_runNumber > m_talkInterval) {
                m_runNumber = 0;
                talk = true;
            }
            final long timeDelte = now - m_lastRunTime;

            for (Map.Entry<String, PlayerEntry> queueEntry : m_blocks.entrySet()) {
                String player = queueEntry.getKey();
                PlayerEntry entry = queueEntry.getValue();
                Integer cnt = blocksPlaced.get(player);

                entry.updateSpeed(cnt != null ? cnt : 0, timeDelte);

                final Player p = PluginMain.getPlayer(player);
                boolean bypass = PermissionManager.isAllowed(p, PermissionManager.Perms.QueueBypass);
                if (entry.getQueue().isEmpty()) {
                    if (PermissionManager.isAllowed(p, PermissionManager.Perms.ProgressBar)) {
                        m_barAPI.disableMessage(p);
                    }
                } else {
                    if (talk && PermissionManager.isAllowed(p, PermissionManager.Perms.TalkativeQueue)) {
                        PluginMain.say(p, ChatColor.YELLOW + "[AWE] You have "
                                + getPlayerMessage(entry, bypass));
                    }

                    if (PermissionManager.isAllowed(p, PermissionManager.Perms.ProgressBar)) {
                        setBar(p, entry, bypass);
                    }
                }
            }
        }

        for (BlockPlacerEntry entry : entries) {
            if (entry != null) {
                entry.Process(this);
            }
        }

        m_lastRunTime = now;
    }

    /**
     * Fetch the blocks that are going to by placed in this run
     *
     * @param blockCnt number of blocks to fetch
     * @param playerNames list of all players
     * @param entries destination blocks entrie
     * @return blocks fatched
     */
    private boolean fetchBlocks(final int blockCnt, final String[] playerNames,
            List<BlockPlacerEntry> entries, final HashMap<String, Integer> blocksPlaced) {
        if (blockCnt <= 0 || playerNames == null || playerNames.length == 0) {
            return false;
        }

        int keyPos = 0;
        boolean added = playerNames.length > 0;
        for (int i = 0; i < blockCnt && added; i++) {
            added = false;

            final String player = playerNames[keyPos];
            PlayerEntry playerEntry = m_blocks.get(player);
            if (playerEntry != null) {
                Queue<BlockPlacerEntry> queue = playerEntry.getQueue();
                synchronized (queue) {
                    if (!queue.isEmpty()) {
                        BlockPlacerEntry entry = queue.poll();
                        if (entry != null) {
                            entries.add(entry);
                            added = true;

                            if (blocksPlaced.containsKey(player)) {
                                blocksPlaced.put(player, blocksPlaced.get(player) + 1);
                            } else {
                                blocksPlaced.put(player, 1);
                            }
                        }
                    }
                }
                final int size = queue.size();
                if (size < m_queueSoftLimit && m_lockedQueues.contains(player)) {
                    PluginMain.say(player, "Your block queue is unlocked. You can use WorldEdit.");
                    m_lockedQueues.remove(player);
                }
                if (size == 0 && !playerEntry.hasJobs()) {
                    m_blocks.remove(playerNames[keyPos]);
                }
            } else if (m_lockedQueues.contains(player)) {
                PluginMain.say(player, "Your block queue is unlocked. You can use WorldEdit.");
                m_lockedQueues.remove(player);
            }
            keyPos = (keyPos + 1) % playerNames.length;
        }
        return added;
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
        m_getTask.cancel();
    }

    /**
     * Get next job id for player
     *
     * @param playerName
     * @return
     */
    public int getJobId(String player) {
        PlayerEntry playerEntry;
        synchronized (this) {
            if (!m_blocks.containsKey(player)) {
                playerEntry = new PlayerEntry();
                m_blocks.put(player, playerEntry);
            } else {
                playerEntry = m_blocks.get(player);
            }
        }

        return playerEntry.getNextJobId();
    }

    public void addJob(String player, BlockPlacerJobEntry job) {
        synchronized (this) {
            PlayerEntry playerEntry;

            if (!m_blocks.containsKey(player)) {
                playerEntry = new PlayerEntry();
                m_blocks.put(player, playerEntry);
            } else {
                playerEntry = m_blocks.get(player);
            }
            playerEntry.addJob((BlockPlacerJobEntry) job);
        }
    }

    /**
     * Add task to perform in async mode
     *
     */
    public boolean addTasks(String player, BlockPlacerEntry entry) {
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

            boolean bypass = !PermissionManager.isAllowed(PluginMain.getPlayer(player), PermissionManager.Perms.QueueBypass);
            int size = 0;
            for (Map.Entry<String, PlayerEntry> queueEntry : m_blocks.entrySet()) {
                size += queueEntry.getValue().getQueue().size();
            }

            bypass |= entry instanceof BlockPlacerJobEntry;
            if (m_queueMaxSize > 0 && size > m_queueMaxSize && !bypass) {
                PluginMain.say(player, "Out of space on AWE block queue.");
                return false;
            } else {
                synchronized (queue) {
                    queue.add(entry);
                }
                if (entry instanceof BlockPlacerBlockEntry) {
                    World world = entry.getEditSession().getCBWorld();
                    if (world != null) {
                        m_physicsWatcher.addLocation(world.getName(), ((BlockPlacerBlockEntry) entry).getLocation());
                    }
                }
                if (entry instanceof BlockPlacerJobEntry) {
                    playerEntry.addJob((BlockPlacerJobEntry) entry);
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
     * @param jobId
     */
    public int cancelJob(String player, int jobId) {
        int newSize = 0;
        int result = 0;
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                PlayerEntry playerEntry = m_blocks.get(player);
                Queue<BlockPlacerEntry> queue = playerEntry.getQueue();
                playerEntry.removeJob(jobId);
                Queue<BlockPlacerEntry> filtered = new ArrayDeque<BlockPlacerEntry>();
                synchronized (queue) {
                    for (BlockPlacerEntry entry : queue) {
                        if (entry.getJobId() == jobId) {
                            if (entry instanceof BlockPlacerBlockEntry) {
                                World world = entry.getEditSession().getCBWorld();
                                if (world != null) {
                                    m_physicsWatcher.removeLocation(world.getName(), ((BlockPlacerBlockEntry) entry).getLocation());
                                }
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
                    if (PermissionManager.isAllowed(p, PermissionManager.Perms.ProgressBar)) {
                        m_barAPI.disableMessage(p);
                    }
                }
            }
            if (m_lockedQueues.contains(player)) {
                if (newSize == 0) {
                    m_lockedQueues.remove(player);
                } else if (newSize < m_queueSoftLimit) {
                    PluginMain.say(player, "Your block queue is unlocked. You can use WorldEdit.");
                    m_lockedQueues.remove(player);
                }
            }
        }

        return result;
    }

    /**
     * Remove all entries for player
     *
     * @param player
     */
    public int purge(String player) {
        int result = 0;
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                PlayerEntry playerEntry = m_blocks.get(player);
                Queue<BlockPlacerEntry> queue = playerEntry.getQueue();
                Collection<BlockPlacerJobEntry> jobs = playerEntry.getJobs();
                synchronized (queue) {
                    for (BlockPlacerEntry entry : queue) {
                        if (entry instanceof BlockPlacerBlockEntry) {
                            World world = entry.getEditSession().getCBWorld();
                            if (world != null) {
                                m_physicsWatcher.removeLocation(world.getName(), ((BlockPlacerBlockEntry) entry).getLocation());
                            }
                        } else if (entry instanceof BlockPlacerJobEntry) {
                            playerEntry.removeJob((BlockPlacerJobEntry) entry);
                        }
                    }
                }

                for (BlockPlacerJobEntry job : jobs.toArray(new BlockPlacerJobEntry[0])) {
                    playerEntry.removeJob(job.getJobId());
                }
                result = queue.size();
                m_blocks.remove(player);
                Player p = PluginMain.getPlayer(player);
                if (PermissionManager.isAllowed(p, PermissionManager.Perms.ProgressBar)) {
                    m_barAPI.disableMessage(p);
                }
            }
            if (m_lockedQueues.contains(player)) {
                m_lockedQueues.remove(player);
            }
        }

        return result;
    }

    /**
     * Remove all entries
     */
    public int purgeAll() {
        int result = 0;
        synchronized (this) {
            for (String user : getAllPlayers()) {
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
    public String[] getAllPlayers() {
        synchronized (this) {
            return m_blocks.keySet().toArray(new String[0]);
        }
    }

    /**
     * Gets the number of events for a player
     *
     * @param player player login
     * @return number of stored events
     */
    public PlayerEntry getPlayerEvents(String player) {
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
    public String getPlayerMessage(String player) {
        PlayerEntry entry = null;
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                entry = m_blocks.get(player);
            }
        }

        boolean bypass = PermissionManager.isAllowed(PluginMain.getPlayer(player), PermissionManager.Perms.QueueBypass);
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
    private HashSet<String> getVips(String[] playerNames) {
        if (playerNames == null || playerNames.length == 0) {
            return new HashSet<String>();
        }

        HashSet<String> result = new HashSet<String>(playerNames.length);

        for (String login : playerNames) {
            Player player = PluginMain.getPlayer(login);
            if (player == null) {
                continue;
            }

            if (PermissionManager.isAllowed(player, PermissionManager.Perms.QueueVip)
                    && !result.contains(login)) {
                result.add(login);
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
    public void removeJob(final String player, BlockPlacerJobEntry jobEntry) {
        PlayerEntry playerEntry;
        synchronized (this) {
            playerEntry = m_blocks.get(player);
        }

        if (playerEntry != null) {
            playerEntry.removeJob(jobEntry);
        }
    }

    /**
     * Add new get block task (high priority tasks!)
     *
     * @param block
     */
    public void addGetTask(BlockPlacerGetBlockEntry block) {
        synchronized (m_getBlocks) {
            m_getBlocks.add(block);
        }
    }

    /**
     * Is this thread the main bukkit thread
     *
     * @return
     */
    public boolean isMainTask() {
        return m_mainThread == Thread.currentThread();
    }

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
}