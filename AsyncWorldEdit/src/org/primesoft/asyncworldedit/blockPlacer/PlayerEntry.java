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
package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import org.bukkit.ChatColor;

/**
 * Operation queue player entry
 *
 * @author SBPrime
 */
public class PlayerEntry {
    /**
     * Maximum job number
     */
    private final int MAX_JOBS = 1024;
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
     * Job id
     */
    private int m_jobId;
    /**
     * List of jobs
     */
    private final HashMap<Integer, JobEntry> m_jobs;

    
    /**
     * Is the player informed about queue limit reached
     */
    private boolean m_isInformed;

    /**
     * Create new player entry
     */
    public PlayerEntry() {
        m_queue = new ArrayDeque();
        m_speed = 0;
        m_jobId = 0;
        m_jobs = new HashMap<Integer, JobEntry>();
    }

    /**
     * Get block entries queue
     * @return
     */
    public Queue<BlockPlacerEntry> getQueue() {
        return m_queue;
    }

    
    /**
     * Change current queue to new queue
     * @param newQueue 
     */
    public void updateQueue(Queue<BlockPlacerEntry> newQueue) {
        m_queue = newQueue;
    }

    
    /**
     * Get block placing speed (blocks per second)
     * @return 
     */
    public double getSpeed() {
        return m_speed;
    }

    /**
     * Update block placing speed
     * @param blocks number of blocks
     * @param timeDelta time spend
     */
    public void updateSpeed(double blocks, long timeDelta) {
        double delta = timeDelta / 1000.0;        
        m_speed = (m_speed * (AVG_SAMPLES - 1) + (blocks / delta)) / AVG_SAMPLES;
    }

    /**
     * Get next job id
     * @return 
     */
    public int getNextJobId() {
        int result;
        synchronized (this) {
            result = m_jobId;
            m_jobId = (m_jobId + 1) % MAX_JOBS;
        }
        return result;
    }

    
    /**
     * Add new job
     * @param job 
     */
    public void addJob(JobEntry job) {
        synchronized (m_jobs) {
            int id = job.getJobId();
            if (m_jobs.containsKey(id)) {
                m_jobs.remove(id);
            }

            m_jobs.put(id, job);
        }
    }

    
    /**
     * Remove job
     * @param job 
     */
    public void removeJob(JobEntry job) {
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
     * @return 
     */    
    public Collection<JobEntry> getJobs() {
        synchronized (m_jobs) {
            return m_jobs.values();
        }
    }

    
    /**
     * Print jobs message
     * @param lines
     */
    public void printJobs(List<String> lines) {
        synchronized (m_jobs) {
            if (m_jobs.isEmpty()) {
                return;
            }
            lines.add(ChatColor.YELLOW + "Jobs: ");
            for (JobEntry job : m_jobs.values()) {
                lines.add(ChatColor.YELLOW + " * " + job.toString()
                        + ChatColor.YELLOW + " - " + job.getStatusString());
            }
        }
    }

    
    /**
     * Has any job entries
     * @return 
     */
    public boolean hasJobs() {
        synchronized (m_jobs) {
            return !m_jobs.isEmpty();
        }
    }

    
    /**
     * Get job ID
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
     * @return 
     */
    public boolean isInformed() {
        return m_isInformed;
    }


    /**
     * Set isInformed state
     * @param state 
     */
    public void setInformed(boolean state) {
        m_isInformed = state;
    }
}
