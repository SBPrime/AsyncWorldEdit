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
package org.PrimeSoft.AsyncWorldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.foundation.World;
import java.util.*;
import org.PrimeSoft.AsyncWorldedit.BlockLogger.IBlockLogger;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

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
    private HashMap<String, Queue<BlockLogerEntry>> m_blocks;
    /**
     * Should block places shut down
     */
    private boolean m_shutdown;

    /**
     * Initialize new instance of the block placer
     *
     * @param plugin parent
     * @param blockLogger instance block logger
     */
    public BlockPlacer(PluginMain plugin) {
        m_blocks = new HashMap<String, Queue<BlockLogerEntry>>();
        m_scheduler = plugin.getServer().getScheduler();
        m_task = m_scheduler.runTaskTimer(plugin, this,
                ConfigProvider.getInterval(), ConfigProvider.getInterval());
    }

    /**
     * Block placer main loop
     */
    @Override
    public void run() {
        List<BlockLogerEntry> entries = new ArrayList<BlockLogerEntry>(ConfigProvider.getBlockCount());
        synchronized (this) {
            String[] keys = m_blocks.keySet().toArray(new String[0]);
            int keyPos = 0;
            boolean added = keys.length > 0;
            final int blockCnt = ConfigProvider.getBlockCount();
            for (int i = 0; i < blockCnt && added; i++) {
                added = false;

                Queue<BlockLogerEntry> queue = m_blocks.get(keys[keyPos]);
                if (queue != null) {
                    if (!queue.isEmpty()) {
                        entries.add(queue.poll());
                        added = true;
                    }
                    if (queue.isEmpty()) {
                        m_blocks.remove(keys[keyPos]);
                    }
                }
                keyPos = (keyPos + 1) % keys.length;
            }

            if (!added && m_shutdown) {
                Stop();
            }
        }

        for (BlockLogerEntry entry : entries) {
            process(entry);
        }
    }

    /**
     * Queue stop command
     */
    public void queueStop() {
        m_shutdown = true;
    }

    /**
     * Stop block logger
     */
    public void Stop() {
        m_task.cancel();
    }

    /**
     * Add task to perform in async mode
     *
     */
    public void addTasks(BlockLogerEntry entry) {
        synchronized (this) {
            String player = entry.getPlayer();            
            Queue<BlockLogerEntry> queue;
            if (!m_blocks.containsKey(player)) {
                queue = new ArrayDeque<BlockLogerEntry>();
                m_blocks.put(player, queue);
            } else {
                queue = m_blocks.get(player);
            }
            queue.add(entry);

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
    private void process(BlockLogerEntry entry) {
        if (entry == null) {
            return;
        }

        Vector location = entry.getLocation();
        BaseBlock block = entry.getNewBlock();
        String player = entry.getPlayer();
        AsyncEditSession eSession = entry.getEditSession();
        
        if ((location != null && block != null)/* || entry.isFinalize()*/) {
            if (location != null && block != null) {
                eSession.doRawSetBlock(location, block);
            }
        }        
    }
}