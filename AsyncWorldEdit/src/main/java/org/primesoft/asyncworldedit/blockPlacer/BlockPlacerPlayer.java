/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 *
 * All rights reserved.
 *
 * Redistribution in source, use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1.  Redistributions of source code must retain the above copyright notice, this
 *     list of conditions and the following disclaimer.
 * 2.  Redistributions of source code, with or without modification, in any form
 *     other then free of charge is not allowed,
 * 3.  Redistributions of source code, with tools and/or scripts used to build the 
 *     software is not allowed,
 * 4.  Redistributions of source code, with information on how to compile the software
 *     is not allowed,
 * 5.  Providing information of any sort (excluding information from the software page)
 *     on how to compile the software is not allowed,
 * 6.  You are allowed to build the software for your personal use,
 * 7.  You are allowed to build the software using a non public build server,
 * 8.  Redistributions in binary form in not allowed.
 * 9.  The original author is allowed to redistrubute the software in bnary form.
 * 10. Any derived work based on or containing parts of this software must reproduce
 *     the above copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided with the
 *     derived work.
 * 11. The original author of the software is allowed to change the license
 *     terms or the entire license of the software as he sees fit.
 * 12. The original author of the software is allowed to sublicense the software
 *     or its parts using any license terms he sees fit.
 * 13. By contributing to this project you agree that your contribution falls under this
 *     license.
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
package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerPlayer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacerEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.entries.RedoJob;
import org.primesoft.asyncworldedit.blockPlacer.entries.UndoJob;
import org.primesoft.asyncworldedit.strings.MessageType;

/**
 * Operation queue player entry
 *
 * @author SBPrime
 */
public class BlockPlacerPlayer implements IBlockPlacerPlayer {

    /**
     * Number of samples used in AVG count
     */
    private final int AVG_SAMPLES = 5;
    /**
     * The queue
     */
    private Queue<IBlockPlacerEntry> m_queue;
    /**
     * Current block placing speed (blocks per second)
     */
    private double m_speed;

    /**
     * List of jobs
     */
    private final HashMap<Integer, IJobEntry> m_jobs;

    /**
     * Is the player informed about queue limit reached
     */
    private boolean m_isInformed;

    /**
     * Maximum number of blocks on queue Used to display the progress bar
     */
    private int m_maxBlocksOnQueue;

    /**
     * The player
     */
    private final IPlayerEntry m_player;

    /**
     * Create new player entry
     *
     * @param player
     */
    public BlockPlacerPlayer(IPlayerEntry player) {
        m_player = player;
        m_queue = new LinkedList<IBlockPlacerEntry>();
        m_speed = 0;
        m_jobs = new HashMap<Integer, IJobEntry>();
    }

    /**
     * Maximum number of blocks on queue
     *
     * @return
     */
    @Override
    public int getMaxQueueBlocks() {
        return m_maxBlocksOnQueue;
    }

    /**
     * Set the maximum number of blocks on queue
     *
     * @param val
     */
    @Override
    public void setMaxQueueBlocks(int val) {
        m_maxBlocksOnQueue = val;
    }

    /**
     * Get block entries queue
     *
     * @return
     */
    @Override
    public Queue<IBlockPlacerEntry> getQueue() {
        return m_queue;
    }

    /**
     * Change current queue to new queue
     *
     * @param newQueue
     */
    @Override
    public void updateQueue(Queue<IBlockPlacerEntry> newQueue) {
        m_queue = newQueue;
    }

    /**
     * Get block placing speed (blocks per second)
     *
     * @return
     */
    @Override
    public double getSpeed() {
        return m_speed;
    }

    /**
     * Update block placing speed
     *
     * @param blocks number of blocks
     * @param timeDelta time spend
     */
    @Override
    public void updateSpeed(double blocks, long timeDelta) {
        if (timeDelta == 0) {
            if (blocks > 0) {
                return;
            }
            
            m_speed = (m_speed * (AVG_SAMPLES - 1)) / AVG_SAMPLES;
            
            return;
        }
        double delta = timeDelta / 1000.0;
        m_speed = (m_speed * (AVG_SAMPLES - 1) + (blocks / delta)) / AVG_SAMPLES;
    }

    /**
     * Get next job id
     *
     * @return
     */
    @Override
    public int getNextJobId() {
        int maxId = -1;
        synchronized (m_jobs) {
            for (Integer id : m_jobs.keySet()) {
                if (maxId < id) {
                    maxId = id;
                }
            }
        }
        return maxId + 1;
    }

    /**
     * Add new job
     *
     * @param job
     * @param force
     * @return
     */
    @Override
    public boolean addJob(IJobEntry job, boolean force) {
        boolean add;
        int maxJobs = m_player.getPermissionGroup().getMaxJobs();
        synchronized (m_jobs) {
            int id = job.getJobId();
            int count = m_jobs.size();
            boolean contains = m_jobs.containsKey(id);
            add = contains || force || 
                    job instanceof UndoJob || job instanceof RedoJob
                    || (count + 1) <= maxJobs || maxJobs < 0;

            if (contains) {
                m_jobs.remove(id);
            }

            if (add) {
                m_jobs.put(id, job);
            }
        }

        return add;
    }

    /**
     * Remove job
     *
     * @param job
     */
    @Override
    public void removeJob(IJobEntry job) {
        if (job == null) {
            return;
        }
        synchronized (m_jobs) {
            int id = job.getJobId();
            if (!m_jobs.containsKey(id)) {
                return;
            }
            m_jobs.get(id).cancel();
            m_jobs.remove(id);
        }
    }

    /**
     * Remove job
     *
     * @param jobId
     */
    @Override
    public void removeJob(int jobId) {
        synchronized (m_jobs) {
            if (!m_jobs.containsKey(jobId)) {
                return;
            }
            m_jobs.get(jobId).cancel();
            m_jobs.remove(jobId);
        }
    }

    /**
     * Get all jobs
     *
     * @return
     */
    @Override
    public IJobEntry[] getJobs() {
        synchronized (m_jobs) {
            return m_jobs.values().toArray(new IJobEntry[0]);
        }
    }

    /**
     * Print jobs message
     *
     * @param lines
     */
    @Override
    public void printJobs(List<String> lines) {
        synchronized (m_jobs) {
            if (m_jobs.isEmpty()) {
                return;
            }
            lines.add(MessageType.CMD_JOBS_HEADER.format());
            for (IJobEntry job : m_jobs.values()) {
                lines.add(MessageType.CMD_JOBS_LINE.format(job.toString(), job.getStatusString()));
            }
        }
    }

    /**
     * Has any job entries
     *
     * @return
     */
    @Override
    public boolean hasJobs() {
        synchronized (m_jobs) {
            return !m_jobs.isEmpty();
        }
    }

    /**
     * Get job ID
     *
     * @param jobId job ID
     * @return
     */
    @Override
    public IJobEntry getJob(int jobId) {
        synchronized (m_jobs) {
            return m_jobs.get(jobId);
        }
    }

    /**
     * Is the player informed about queue overload
     *
     * @return
     */
    @Override
    public boolean isInformed() {
        return m_isInformed;
    }

    /**
     * Set isInformed state
     *
     * @param state
     */
    @Override
    public void setInformed(boolean state) {
        m_isInformed = state;
    }
}
