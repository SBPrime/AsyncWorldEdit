package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.blockPlacer.entries.JobEntry;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;

/**
 * Stub for the API to compile
 * Operation queue player entry
 *
 * @author SBPrime
 */
public class BlockPlacerPlayer {
    /**
     * Create new player entry
     * @param player
     */
    public BlockPlacerPlayer(PlayerEntry player) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Maximum number of blocks on queue
     *
     * @return
     */
    public int getMaxQueueBlocks() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Set the maximum number of blocks on queue
     * @param val
     */
    public void setMaxQueueBlocks(int val) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Get block entries queue
     *
     * @return
     */
    public Queue<BlockPlacerEntry> getQueue() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Change current queue to new queue
     *
     * @param newQueue
     */
    public void updateQueue(Queue<BlockPlacerEntry> newQueue) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Get block placing speed (blocks per second)
     *
     * @return
     */
    public double getSpeed() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Update block placing speed
     *
     * @param blocks number of blocks
     * @param timeDelta time spend
     */
    public void updateSpeed(double blocks, long timeDelta) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Get next job id
     *
     * @return
     */
    public int getNextJobId() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Add new job
     *
     * @param job
     * @param force
     * @return 
     */
    public boolean addJob(JobEntry job, boolean force) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Remove job
     *
     * @param job
     */
    public void removeJob(JobEntry job) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");

    }

    /**
     * Remove job
     *
     * @param jobId
     */
    public void removeJob(int jobId) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Get all jobs
     *
     * @return
     */
    public Collection<JobEntry> getJobs() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Print jobs message
     *
     * @param lines
     */
    public void printJobs(List<String> lines) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Has any job entries
     *
     * @return
     */
    public boolean hasJobs() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Get job ID
     *
     * @param jobId job ID
     * @return
     */
    public JobEntry getJob(int jobId) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Is the player informed about queue overload
     *
     * @return
     */
    public boolean isInformed() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Set isInformed state
     *
     * @param state
     */
    public void setInformed(boolean state) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
}
