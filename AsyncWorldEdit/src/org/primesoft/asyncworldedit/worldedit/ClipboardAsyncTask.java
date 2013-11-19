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

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerJobEntry;

/**
 *
 * @author SBPrime
 */
public abstract class ClipboardAsyncTask extends BukkitRunnable {

    /**
     * Command name
     */
    private final String m_command;
    /**
     * Clipboard
     */
    private final CuboidClipboard m_clipboard;
    /**
     * The player
     */
    private final String m_player;
    private final BlockPlacer m_blockPlacer;
    private final BlockPlacerJobEntry m_job;
    private final AsyncEditSession m_editSession;

    public ClipboardAsyncTask(final CuboidClipboard clipboard, final EditSession editSession,
            final String player, final String commandName, BlockPlacer blocksPlacer,
            BlockPlacerJobEntry job) {
        m_clipboard = clipboard;
        m_player = player;
        m_command = commandName;
        m_blockPlacer = blocksPlacer;
        m_job = job;
        m_editSession = (editSession instanceof AsyncEditSession) ? (AsyncEditSession) editSession : null;
        if (m_editSession != null) {
            m_editSession.addAsync(job);
        }
    }

    @Override
    public void run() {
        try {
            m_job.setStatus(BlockPlacerJobEntry.JobStatus.Preparing);
            PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Running " + ChatColor.WHITE
                    + m_command + ChatColor.LIGHT_PURPLE + " in full async mode.");

            m_blockPlacer.addTasks(m_player, m_job);
            task(m_clipboard);

            if (m_editSession != null && m_editSession.isQueueEnabled()) {
                m_editSession.flushQueue();
            }
            m_job.setStatus(BlockPlacerJobEntry.JobStatus.Waiting);
            m_blockPlacer.addTasks(m_player, m_job);
            PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Clipboard operation done.");
        } catch (MaxChangedBlocksException ex) {
            PluginMain.say(m_player, ChatColor.RED + "Maximum block change limit.");
        } catch (IllegalArgumentException ex) {
            if (ex.getCause() instanceof CancelabeEditSession.SessionCanceled) {
                PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Job canceled.");
            }
        }

        if (m_editSession != null) {
            m_editSession.removeAsync(m_job);
        }
    }

    /**
     * Task to run
     *
     * @return
     */
    public abstract void task(CuboidClipboard clipboard)
            throws MaxChangedBlocksException;
}
