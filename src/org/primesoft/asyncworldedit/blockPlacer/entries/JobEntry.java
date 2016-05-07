package org.primesoft.asyncworldedit.blockPlacer.entries;

import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.blockPlacer.BlockPlacerEntry;
import org.primesoft.asyncworldedit.api.blockPlacer.IJobEntryListener;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 * Stub for the API to compile
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

        JobStatus(int seqNumber) {
            throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
        }

        public int getSeqNumber() {
            throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
        }
    }

    /**
     * Get the player UUID
     *
     * @return
     */
    public PlayerEntry getPlayer() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
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
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
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

        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Add job state change listener
     *
     * @param listener
     */
    public void addStateChangedListener(IJobEntryListener listener) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Remove the change state listener
     *
     * @param listener
     */
    public void removeStateChangedListener(IJobEntryListener listener) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Is the async task done
     *
     * @return
     */
    public boolean isTaskDone() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Async task has finished
     */
    public void taskDone() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Is the job started
     *
     * @return
     */
    public JobStatus getStatus() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Get the operation name
     *
     * @return
     */
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Set the job state
     *
     * @param newStatus
     */
    public void setStatus(JobStatus newStatus) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Cancel the job
     */
    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    /**
     * Convert job status to string
     *
     * @return
     */
    public String getStatusString() {                
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public String toString() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }

    @Override
    public boolean process(IBlockPlacer bp) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
}
