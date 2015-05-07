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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.SessionCanceled;

/**
 *
 * @author SBPrime
 */
public abstract class BaseTask implements Runnable {

    /**
     * Command name
     */
    protected final String m_command;

    /**
     * The player
     */
    protected final PlayerEntry m_player;

    /**
     * Cancelable edit session
     */
    protected final CancelabeEditSession m_cancelableEditSession;

    /**
     * Thread safe edit session
     */
    protected final ThreadSafeEditSession m_safeEditSession;

    /**
     * The edit session
     */
    protected final EditSession m_editSession;

    /**
     * The blocks placer
     */
    protected final IBlockPlacer m_blockPlacer;

    /**
     * Job instance
     */
    protected final JobEntry m_job;

    /**
     * The permission group
     */
    protected final PermissionGroup m_group;

    public BaseTask(final EditSession editSession, final PlayerEntry player,
            final String commandName, IBlockPlacer blocksPlacer, JobEntry job) {

        m_editSession = editSession;
        m_cancelableEditSession = (editSession instanceof CancelabeEditSession) ? (CancelabeEditSession) editSession : null;

        m_player = player;
        m_group = m_player.getPermissionGroup();
        m_command = commandName;
        m_blockPlacer = blocksPlacer;
        m_job = job;

        if (m_cancelableEditSession != null) {
            m_safeEditSession = m_cancelableEditSession.getParent();
        } else {
            m_safeEditSession = (editSession instanceof ThreadSafeEditSession) ? (ThreadSafeEditSession) editSession : null;
        }

        if (m_safeEditSession != null) {
            m_safeEditSession.addAsync(job);
        }
    }

    @Override
    public void run() {
        Object result = null;

        if (m_job.getStatus() == JobEntry.JobStatus.Canceled) {
            return;
        }

        m_job.setStatus(JobEntry.JobStatus.Preparing);
        if (m_group.isTalkative()) {
            m_player.say(MessageType.BLOCK_PLACER_RUN.format(m_command));
        }
        m_blockPlacer.addTasks(m_player, m_job);

        if ((m_cancelableEditSession == null || !m_cancelableEditSession.isCanceled())
                && (m_job.getStatus() != JobEntry.JobStatus.Canceled)) {
            try {
                result = doRun();
            } catch (MaxChangedBlocksException ex) {
                m_player.say(MessageType.BLOCK_PLACER_MAX_CHANGED.format());
            } catch (IllegalArgumentException ex) {
                if (ex.getCause() instanceof SessionCanceled) {
                    m_player.say(MessageType.BLOCK_PLACER_CANCELED.format());
                }
            }
        }

        if (m_editSession != null) {
            if (m_editSession.isQueueEnabled()) {
                m_editSession.flushQueue();
            } else if (m_cancelableEditSession != null) {
                m_cancelableEditSession.resetAsync();
            } else if (m_safeEditSession != null) {
                m_safeEditSession.resetAsync();
            }
        }

        m_job.setStatus(JobEntry.JobStatus.Waiting);
        m_blockPlacer.addTasks(m_player, m_job);
        doPostRun(result);

        postProcess();

        m_job.taskDone();
        if (m_cancelableEditSession != null) {
            ThreadSafeEditSession parent = m_cancelableEditSession.getParent();
            parent.removeAsync(m_job);
        } else if (m_safeEditSession != null) {
            m_safeEditSession.removeAsync(m_job);
        }
    }

    protected abstract Object doRun() throws MaxChangedBlocksException, IllegalArgumentException;

    protected abstract void doPostRun(Object result);

    protected void postProcess() {
    }
}
