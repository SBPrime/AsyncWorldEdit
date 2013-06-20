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
package org.primesoft.asyncworldedit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.blocklogger.IBlockLogger;
import org.primesoft.asyncworldedit.blocklogger.NoneLogger;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import java.util.*;

/**
 *
 * @author SBPrime
 */
public class BlockPlacer implements Runnable {

    /**
     * Bukkit scheduler
     */
    private BukkitScheduler m_scheduler;
    /**
     * Current scheduler task
     */
    private BukkitTask m_task;
    /**
     * Logged events queue (per player)
     */
    private HashMap<String, Queue<BlockPlacerEntry>> m_blocks;
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
     * Player block queue soft limit (minimum number of blocks 
     * before queue is unlocked)
     */
    private int m_queueSoftLimit;
    
    
    /**
     * Global queue max size
     */
    private int m_queueMaxSize;

    /**
     * Initialize new instance of the block placer
     *
     * @param plugin parent
     * @param blockLogger instance block logger
     */
    public BlockPlacer(PluginMain plugin) {
        m_blocks = new HashMap<String, Queue<BlockPlacerEntry>>();
        m_lockedQueues = new HashSet<String>();
        m_scheduler = plugin.getServer().getScheduler();
        m_task = m_scheduler.runTaskTimer(plugin, this,
                ConfigProvider.getInterval(), ConfigProvider.getInterval());

        m_queueHardLimit = ConfigProvider.getQueueHardLimit();
        m_queueSoftLimit = ConfigProvider.getQueueSoftLimit();
        m_queueMaxSize = ConfigProvider.getQueueMaxSize();
    }

    /**
     * Block placer main loop
     */
    @Override
    public void run() {
        List<BlockPlacerEntry> entries = new ArrayList<BlockPlacerEntry>(ConfigProvider.getBlockCount() + ConfigProvider.getVipBlockCount());
        boolean added = false;
        synchronized (this) {
            final String[] keys = m_blocks.keySet().toArray(new String[0]);
            final String[] vipKeys = getVips(keys);

            added |= fetchBlocks(ConfigProvider.getBlockCount(), keys, entries);
            added |= fetchBlocks(ConfigProvider.getVipBlockCount(), vipKeys, entries);

            if (!added && m_shutdown) {
                stop();
            }
        }

        for (BlockPlacerEntry entry : entries) {
            process(entry);
        }
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
            List<BlockPlacerEntry> entries) {
        if (blockCnt <= 0 || playerNames == null || playerNames.length == 0) {
            return false;
        }

        int keyPos = 0;
        boolean added = playerNames.length > 0;
        for (int i = 0; i < blockCnt && added; i++) {
            added = false;

            String player = playerNames[keyPos];
            Queue<BlockPlacerEntry> queue = m_blocks.get(player);
            if (queue != null) {
                if (!queue.isEmpty()) {
                    entries.add(queue.poll());
                    added = true;
                }
                int size = queue.size();
                if (size < m_queueSoftLimit && m_lockedQueues.contains(player)) {
                    PluginMain.Say(PluginMain.getPlayer(player), "Your block queue is unlocked. You can use WorldEdit.");
                    m_lockedQueues.remove(player);
                }
                if (size == 0) {
                    m_blocks.remove(playerNames[keyPos]);
                }
            } else if (m_lockedQueues.contains(player)) {
                PluginMain.Say(PluginMain.getPlayer(player), "Your block queue is unlocked. You can use WorldEdit.");
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
    }

    /**
     * Add task to perform in async mode
     *
     */
    public boolean addTasks(BlockPlacerEntry entry) {
        synchronized (this) {
            AsyncEditSession editSesson = entry.getEditSession();
            String player = editSesson.getPlayer();
            Queue<BlockPlacerEntry> queue;
            if (!m_blocks.containsKey(player)) {
                queue = new ArrayDeque<BlockPlacerEntry>();
                m_blocks.put(player, queue);
            } else {
                queue = m_blocks.get(player);
            }

            if (m_lockedQueues.contains(player)) {
                return false;
            }

            boolean bypass = !PermissionManager.isAllowed(PluginMain.getPlayer(player), PermissionManager.Perms.QueueBypass);
            int size = 0;
            for (Map.Entry<String, Queue<BlockPlacerEntry>> queueEntry : m_blocks.entrySet()) {
                size += queueEntry.getValue().size();
            }
            
            if (m_queueMaxSize > 0 && size > m_queueMaxSize && !bypass) {
                PluginMain.Say(PluginMain.getPlayer(player), "Out of space on AWE block queue.");
                return false;
            } else {
                queue.add(entry);
                if (queue.size() >= m_queueHardLimit && bypass) {
                    m_lockedQueues.add(player);
                    PluginMain.Say(PluginMain.getPlayer(player), "Your block queue is full. Wait for items to finish drawing.");
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Remove all entries for player
     *
     * @param player
     */
    public void purge(String player) {
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                m_blocks.remove(player);
            }
            if (m_lockedQueues.contains(player)) {
                m_lockedQueues.remove(player);
            }
        }
    }

    /**
     * Remove all entries
     */
    public void purgeAll() {
        synchronized (this) {
            for (String user : getAllPlayers()) {
                purge(user);
            }
        }
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
    public int getPlayerEvents(String player) {
        synchronized (this) {
            if (m_blocks.containsKey(player)) {
                return m_blocks.get(player).size();
            }

            return 0;
        }
    }

    /**
     * Process logged event
     *
     * @param entry event to process
     */
    private void process(BlockPlacerEntry entry) {
        if (entry == null) {
            return;
        }

        Vector location = entry.getLocation();
        BaseBlock block = entry.getNewBlock();
        AsyncEditSession eSession = entry.getEditSession();        
        eSession.doRawSetBlock(location, block);        
    }

    /**
     * Filter player names for vip players (AWE.user.vip-queue)
     *
     * @param playerNames
     * @return
     */
    private String[] getVips(String[] playerNames) {
        if (playerNames == null || playerNames.length == 0) {
            return new String[0];
        }

        List<String> result = new ArrayList<String>(playerNames.length);

        for (String login : playerNames) {
            Player player = PluginMain.getPlayer(login);
            if (player == null) {
                continue;
            }

            if (PermissionManager.isAllowed(player, PermissionManager.Perms.QueueVip)) {
                result.add(login);
            }
        }

        return result.toArray(new String[0]);
    }
}