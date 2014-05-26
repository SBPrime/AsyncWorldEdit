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
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerJobEntry;

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
     * Edit session
     */
    private final CancelabeEditSession m_editSession;
    /**
     * The player
     */
    private final UUID m_player;
    private final BlockPlacer m_blockPlacer;
    private final BlockPlacerJobEntry m_job;

    public AsyncTask(final CancelabeEditSession session, final UUID player,
            final String commandName, BlockPlacer blocksPlacer, BlockPlacerJobEntry job) {
        m_editSession = session;
        m_player = player;
        m_command = commandName;
        m_blockPlacer = blocksPlacer;
        m_job = job;

        session.getParent().addAsync(job);
    }

    @Override
    public void run() {
        try {
            m_job.setStatus(BlockPlacerJobEntry.JobStatus.Preparing);
            if (ConfigProvider.isTalkative()) {
                PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Running " + ChatColor.WHITE
                        + m_command + ChatColor.LIGHT_PURPLE + " in full async mode.");
            }
            m_blockPlacer.addTasks(m_player, m_job);
            int cnt = 0;
            if (!m_editSession.isCanceled()) {
                cnt = task(m_editSession);
            }

            if (!m_editSession.isQueueEnabled()) {
                m_editSession.resetAsync();
            } else {
                m_editSession.flushQueue();
            }

            m_job.setStatus(BlockPlacerJobEntry.JobStatus.Waiting);
            m_blockPlacer.addTasks(m_player, m_job);
            if (ConfigProvider.isTalkative()) {
                PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Blocks processed: " + ChatColor.WHITE + cnt);
            }
        } catch (MaxChangedBlocksException ex) {
            PluginMain.say(m_player, ChatColor.RED + "Maximum block change limit.");
        } catch (IllegalArgumentException ex) {
            if (ex.getCause() instanceof CancelabeEditSession.SessionCanceled) {
                PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Job canceled.");
                m_job.setStatus(BlockPlacerJobEntry.JobStatus.Done);
            }
        }

        m_job.taskDone();
        m_editSession.getParent().removeAsync(m_job);
    }

    /**
     * Task to run
     *
     * @param editSession
     * @return
     * @throws MaxChangedBlocksException
     */
    public abstract int task(CancelabeEditSession editSession) throws MaxChangedBlocksException;
}
