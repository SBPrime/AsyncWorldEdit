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
package org.primesoft.asyncworldedit.blockPlacer.entries;

import java.util.ArrayList;
import java.util.List;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.configuration.PermissionGroup;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 * Job description empty
 *
 * @author SBPrime
 */
public class JobEntry extends BlockPlacerEntry {

    /**
     * Job status
     */
    public enum JobStatus {

        Initializing(0),
        Preparing(1),
        Waiting(2),
        PlacingBlocks(3),
        Done(4),
        Canceled(5);

        /**
         * The sequence number
         */
        private final int m_seqNumber;

        JobStatus(int seqNumber) {
            m_seqNumber = seqNumber;
        }

        public int getSeqNumber() {
            return m_seqNumber;
        }
    }

    /**
     * Job name
     */
    private final String m_name;

    /**
     * Is the job status
     */
    private JobStatus m_status;

    /**
     * Cancelable edit session
     */
    private final CancelabeEditSession m_cEditSession;

    /**
     * The player name
     */
    private final PlayerEntry m_player;

    /**
     * Is the async task done
     */
    private boolean m_taskDone;

    /**
     * All job state changed events
     */
    private final List<IJobEntryListener> m_jobStateChanged;

    /**
     * Get the player UUID
     *
     * @return
     */
    public PlayerEntry getPlayer() {
        return m_player;
    }

    /**
     * Create new instance of the class
     *
     * @param player player uuid
     * @param jobId job id
     * @param name operation name
     */
    public JobEntry(PlayerEntry player, int jobId, String name) {
        super(jobId, false);
        m_player = player;
        m_name = name;
        m_status = JobStatus.Initializing;
        m_cEditSession = null;
        m_jobStateChanged = new ArrayList<IJobEntryListener>();
    }

    /**
     * Create new instance of the class
     *
     * @param player player uuid
     * @param jobId job id
     * @param name operation name
     * @param cEditSession the cancelable edit session
     */
    public JobEntry(PlayerEntry player,
            CancelabeEditSession cEditSession,
            int jobId, String name) {
        super(jobId, false);

        m_player = player;
        m_name = name;
        m_status = JobStatus.Initializing;
        m_cEditSession = cEditSession;
        m_jobStateChanged = new ArrayList<IJobEntryListener>();
    }

    /**
     * Add job state change listener
     *
     * @param listener
     */
    public void addStateChangedListener(IJobEntryListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (m_jobStateChanged) {
            if (!m_jobStateChanged.contains(listener)) {
                m_jobStateChanged.add(listener);
            }
        }
    }

    /**
     * Remove the change state listener
     *
     * @param listener
     */
    public void removeStateChangedListener(IJobEntryListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (m_jobStateChanged) {
            if (m_jobStateChanged.contains(listener)) {
                m_jobStateChanged.remove(listener);
            }
        }
    }

    /**
     * Is the async task done
     *
     * @return
     */
    public boolean isTaskDone() {
        return m_taskDone;
    }

    /**
     * Async task has finished
     */
    public void taskDone() {
        m_taskDone = true;

        callStateChangedEvents();
    }

    /**
     * Is the job started
     *
     * @return
     */
    public JobStatus getStatus() {
        return m_status;
    }

    /**
     * Get the operation name
     *
     * @return
     */
    public String getName() {
        return m_name;
    }

    /**
     * Set the job state
     *
     * @param newStatus
     */
    public void setStatus(JobStatus newStatus) {
        int newS = newStatus.getSeqNumber();
        int oldS = m_status.getSeqNumber();

        if (newS < oldS) {
            return;
        }
        m_status = newStatus;
        callStateChangedEvents();
    }

    /**
     * Cancel the job
     */
    public void cancel() {
        JobStatus status = getStatus();
        if (status != JobStatus.Done && !m_taskDone) {
            setStatus(JobStatus.Canceled);
        }
        if (m_cEditSession != null) {
            m_cEditSession.cancel();
        }
    }

    /**
     * Convert job status to string
     *
     * @return
     */
    public String getStatusString() {                
        switch (m_status) {
            case Done:
                return MessageType.CMD_JOBS_STATUS_DONE.format();
            case Canceled:
                return MessageType.CMD_JOBS_STATUS_CANCELED.format();
            case Initializing:
                return MessageType.CMD_JOBS_STATUS_INITIALIZING.format();
            case PlacingBlocks:
                return MessageType.CMD_JOBS_STATUS_PLACING_BLOCKS.format();
            case Preparing:
                return MessageType.CMD_JOBS_STATUS_PREPARING.format();
            case Waiting:
                return MessageType.CMD_JOBS_STATUS_WAITING.format();
        }

        return "";
    }

    @Override
    public String toString() {
        return MessageType.CMD_JOBS_FORMAT.format(getJobId(), getName());
    }

    @Override
    public boolean process(IBlockPlacer bp) {
        final PlayerEntry player = m_player;

        switch (m_status) {
            case Canceled:
            case Done:
                bp.removeJob(player, this);
                return true;
            case PlacingBlocks:
                setStatus(JobEntry.JobStatus.Done);
                bp.removeJob(player, this);
                break;
            case Initializing:
            case Preparing:
            case Waiting:
                setStatus(JobEntry.JobStatus.PlacingBlocks);
                break;
        }

        PermissionGroup group = player.getPermissionGroup();
        if (group.isTalkative()) {
            player.say(MessageType.CMD_JOBS_STATUS.format(toString(), getStatusString()));
        }

        return true;
    }

    /**
     * Inform the listener of state changed
     */
    private void callStateChangedEvents() {
        synchronized (m_jobStateChanged) {
            for (IJobEntryListener listener : m_jobStateChanged) {
                listener.jobStateChanged(this);
            }
        }
    }
}
