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
package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.strings.MessageType;

/**
 * Operation queue player entry
 *
 * @author SBPrime
 */
public class BlockPlacerPlayer {
    /**
     * Number of samples used in AVG count
     */
    private final int AVG_SAMPLES = 5;
    /**
     * The queue
     */
    private Queue<BlockPlacerEntry> m_queue;
    /**
     * Current block placing speed (blocks per second)
     */
    private double m_speed;

    /**
     * List of jobs
     */
    private final HashMap<Integer, JobEntry> m_jobs;

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
    private final PlayerEntry m_player;
    

    /**
     * Create new player entry
     * @param player
     */
    public BlockPlacerPlayer(PlayerEntry player) {
        m_player = player;
        m_queue = new ArrayDeque();
        m_speed = 0;
        m_jobs = new HashMap<Integer, JobEntry>();
    }

    /**
     * Maximum number of blocks on queue
     *
     * @return
     */
    public int getMaxQueueBlocks() {
        return m_maxBlocksOnQueue;
    }

    /**
     * Set the maximum number of blocks on queue
     * @param val
     */
    public void setMaxQueueBlocks(int val) {
        m_maxBlocksOnQueue = val;
    }

    /**
     * Get block entries queue
     *
     * @return
     */
    public Queue<BlockPlacerEntry> getQueue() {
        return m_queue;
    }

    /**
     * Change current queue to new queue
     *
     * @param newQueue
     */
    public void updateQueue(Queue<BlockPlacerEntry> newQueue) {
        m_queue = newQueue;
    }

    /**
     * Get block placing speed (blocks per second)
     *
     * @return
     */
    public double getSpeed() {
        return m_speed;
    }

    /**
     * Update block placing speed
     *
     * @param blocks number of blocks
     * @param timeDelta time spend
     */
    public void updateSpeed(double blocks, long timeDelta) {
        double delta = timeDelta / 1000.0;
        m_speed = (m_speed * (AVG_SAMPLES - 1) + (blocks / delta)) / AVG_SAMPLES;
    }

    /**
     * Get next job id
     *
     * @return
     */
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
    public boolean addJob(JobEntry job, boolean force) {
        boolean add;
        int maxJobs = m_player.getPermissionGroup().getMaxJobs();
        synchronized (m_jobs) {
            int id = job.getJobId();
            int count = m_jobs.size();
            boolean contains = m_jobs.containsKey(id);
            add = contains || force ||
                    (count + 1) <= maxJobs || maxJobs < 0;
            
            if (contains)
            {
                m_jobs.remove(id);
            }

            if (add)
            {
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
    public void removeJob(JobEntry job) {
        if (job == null)
        {
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
    public Collection<JobEntry> getJobs() {
        synchronized (m_jobs) {
            return m_jobs.values();
        }
    }

    /**
     * Print jobs message
     *
     * @param lines
     */
    public void printJobs(List<String> lines) {
        synchronized (m_jobs) {
            if (m_jobs.isEmpty()) {
                return;
            }
            lines.add(MessageType.CMD_JOBS_HEADER.format());
            for (JobEntry job : m_jobs.values()) {
                lines.add(MessageType.CMD_JOBS_LINE.format(job.toString(), job.getStatusString()));
            }
        }
    }

    /**
     * Has any job entries
     *
     * @return
     */
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
    public JobEntry getJob(int jobId) {
        synchronized (m_jobs) {
            return m_jobs.get(jobId);
        }
    }

    /**
     * Is the player informed about queue overload
     *
     * @return
     */
    public boolean isInformed() {
        return m_isInformed;
    }

    /**
     * Set isInformed state
     *
     * @param state
     */
    public void setInformed(boolean state) {
        m_isInformed = state;
    }
}
