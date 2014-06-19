/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerJobEntry;
import org.primesoft.asyncworldedit.utils.SessionCanceled;
import org.primesoft.asyncworldedit.worldedit.history.InjectedArrayListHistory;

/**
 *
 * @author SBPrime
 */
public abstract class BaseTask extends BukkitRunnable {

    /**
     * Command name
     */
    protected final String m_command;

    /**
     * The player
     */
    protected final UUID m_player;

    /**
     * Cancelable edit session
     */
    protected final CancelabeEditSession m_cancelableEditSession;

    /**
     * Async edit session
     */
    protected final AsyncEditSession m_asyncEditSession;

    /**
     * The edit session
     */
    protected final EditSession m_editSession;

    /**
     * The blocks placer
     */
    protected final BlockPlacer m_blockPlacer;

    /**
     * Job instance
     */
    protected final BlockPlacerJobEntry m_job;

    public BaseTask(final EditSession editSession, final UUID player,
            final String commandName, BlockPlacer blocksPlacer, BlockPlacerJobEntry job) {

        m_editSession = editSession;
        m_cancelableEditSession = (editSession instanceof CancelabeEditSession) ? (CancelabeEditSession) editSession : null;
        
        m_player = player;
        m_command = commandName;
        m_blockPlacer = blocksPlacer;
        m_job = job;

        if (m_cancelableEditSession != null) {
            m_asyncEditSession = m_cancelableEditSession.getParent();
        } else {
            m_asyncEditSession = (editSession instanceof AsyncEditSession) ? (AsyncEditSession) editSession : null;
        }
        
        if (m_asyncEditSession != null) {
            m_asyncEditSession.addAsync(job);
        }
    }

    @Override
    public void run() {
        Object result = null;
        try {
            m_job.setStatus(BlockPlacerJobEntry.JobStatus.Preparing);
            if (ConfigProvider.isTalkative()) {
                PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Running " + ChatColor.WHITE
                        + m_command + ChatColor.LIGHT_PURPLE + " in full async mode.");
            }
            m_blockPlacer.addTasks(m_player, m_job);
            if (m_cancelableEditSession == null || !m_cancelableEditSession.isCanceled()) {
                result = doRun();
            }

            if (m_editSession != null) {
                if (m_editSession.isQueueEnabled()) {
                    m_editSession.flushQueue();
                } else if (m_cancelableEditSession != null) {
                    m_cancelableEditSession.resetAsync();
                } else if (m_asyncEditSession != null) {
                    m_asyncEditSession.resetAsync();
                }
            }

            m_job.setStatus(BlockPlacerJobEntry.JobStatus.Waiting);
            m_blockPlacer.addTasks(m_player, m_job);
            doPostRun(result);
        } catch (MaxChangedBlocksException ex) {
            PluginMain.say(m_player, ChatColor.RED + "Maximum block change limit.");
        } catch (IllegalArgumentException ex) {
            if (ex.getCause() instanceof SessionCanceled) {
                PluginMain.say(m_player, ChatColor.LIGHT_PURPLE + "Job canceled.");
            }
        }

        m_job.taskDone();
        if (m_cancelableEditSession != null) {
            AsyncEditSession parent = m_cancelableEditSession.getParent();
            copyChangeSet(m_cancelableEditSession, parent);
            parent.removeAsync(m_job);
        } else if (m_asyncEditSession != null) {
            m_asyncEditSession.removeAsync(m_job);
        }        
    }

    /**
     * Copy changed items to parent edit session This works best when change set
     * is set to ArrayListHistory
     *
     * @param destination
     */
    private void copyChangeSet(EditSession source, EditSession destination) {
        ChangeSet csSource = source.getChangeSet();
        ChangeSet csDestination = destination.getChangeSet();

        if (csSource.size() == 0) {
            return;
        }

        if ((csSource instanceof InjectedArrayListHistory)) {
            for (Iterator<Change> it = csSource.forwardIterator(); it.hasNext();) {
                csDestination.add(it.next());
            }
            return;
        }

        PluginMain.log("Warning: ChangeSet is not set to ArrayListHistory, rebuilding...");
        HashMap<BlockVector, BaseBlock> oldBlocks = new HashMap<BlockVector, BaseBlock>();

        for (Iterator<Change> it = csSource.backwardIterator(); it.hasNext();) {
            Change chg = it.next();
            if (chg instanceof BlockChange) {
                BlockChange bchg = (BlockChange) chg;
                BlockVector pos = bchg.getPosition();
                BaseBlock oldBlock = bchg.getPrevious();

                if (oldBlocks.containsKey(pos)) {
                    oldBlocks.remove(pos);
                }
                oldBlocks.put(pos, oldBlock);
            }
        }

        for (Iterator<Change> it = csSource.forwardIterator(); it.hasNext();) {
            Change chg = it.next();
            if (chg instanceof BlockChange) {
                BlockChange bchg = (BlockChange) chg;
                BlockVector pos = bchg.getPosition();
                BaseBlock block = bchg.getCurrent();
                BaseBlock oldBlock = oldBlocks.get(pos);

                if (oldBlock != null) {
                    csDestination.add(new BlockChange(pos, oldBlock, block));
                }
            } else {
                csDestination.add(chg);
            }
        }
    }

    protected abstract Object doRun() throws MaxChangedBlocksException;

    protected abstract void doPostRun(Object result);
}
