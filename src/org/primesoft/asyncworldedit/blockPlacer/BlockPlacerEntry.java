package org.primesoft.asyncworldedit.blockPlacer;

import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;

/**
 * Stub for the API to compile
 * @author SBPrime
 */
public abstract class BlockPlacerEntry {
    /**
     * Is this task demanding, only one demanding task is allowed
     * @return 
     */
    public boolean isDemanding() {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
    
    /**
     * The job ID
     * @return 
     */
    public int getJobId(){
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
    
    
    /**
     * New instance of block placer entry
     * @param jobId THe job id
     * @param isDemanding
     */
    public BlockPlacerEntry(int jobId, boolean isDemanding) {
        throw new UnsupportedOperationException("Not supported yet. This is only a STUB");
    }
    
    
    /**
     * process the entry
     * @param bp
     * @return true if operation was successful
     */
    public abstract boolean process(IBlockPlacer bp);
}