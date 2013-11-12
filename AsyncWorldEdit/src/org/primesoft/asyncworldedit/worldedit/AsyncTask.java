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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.MaxChangedBlocksException;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.PluginMain;

/**
 *
 * @author SBPrime
 */
public abstract class AsyncTask extends BukkitRunnable {
    /**
     * Command name
     */
    private final String m_command;
    /**
     * Current task
     */
    private final List<BukkitTask> m_task;
    
    
    /**
     * Task queue
     */
    private final List<BukkitTask> m_taskQueue;
    
    
    /**
     * Edit session
     */
    private final AsyncEditSession m_editSession;
    /**
     * The player
     */
    private final Player m_player;

    
    public AsyncTask(final AsyncEditSession session, final Player player,
            final String commandName, final List<BukkitTask> taskQueue, 
            final List<BukkitTask> task)
    {
        m_editSession = session;
        m_player = player;
        m_command = commandName;
        m_taskQueue = taskQueue;
        m_task = task;
    }
    
    
    @Override
    public void run() {
        try {
            PluginMain.Say(m_player, ChatColor.LIGHT_PURPLE + "Running " + ChatColor.WHITE
                    + m_command + ChatColor.LIGHT_PURPLE + " in full async mode.");
            int cnt = task();

            if (!m_editSession.isQueueEnabled()) {
                m_editSession.resetAsync();
            } else {
                m_editSession.flushQueue();
            }

            PluginMain.Say(m_player, ChatColor.LIGHT_PURPLE + "Blocks processed: " + ChatColor.WHITE + cnt);
        } catch (MaxChangedBlocksException ex) {
            PluginMain.Say(m_player, ChatColor.RED + "Maximum block change limit.");
        }

        synchronized (m_taskQueue) {
            for (BukkitTask task : m_task) {
                m_taskQueue.remove(task);
            }
        }
    }

    
    /**
     * Task to run
     * @return
     * @throws MaxChangedBlocksException 
     */
    public abstract int task() throws MaxChangedBlocksException;
}
