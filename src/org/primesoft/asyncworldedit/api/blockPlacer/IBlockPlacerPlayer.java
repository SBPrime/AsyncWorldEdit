/*
 * AsyncWorldEdit API
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit API contributors
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
package org.primesoft.asyncworldedit.api.blockPlacer;

import java.util.List;
import java.util.Queue;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;

/**
 *
 * @author SBPrime
 */
public interface IBlockPlacerPlayer {

    /**
     * Add new job
     *
     * @param job
     * @param force
     * @return
     */
    boolean addJob(IJobEntry job, boolean force);

    /**
     * Get job ID
     *
     * @param jobId job ID
     * @return
     */
    IJobEntry getJob(int jobId);

    /**
     * Get all jobs
     *
     * @return
     */
    IJobEntry[] getJobs();

    /**
     * Maximum number of blocks on queue
     *
     * @return
     */
    int getMaxQueueBlocks();

    /**
     * Get next job id
     *
     * @return
     */
    int getNextJobId();

    /**
     * Get block entries queue
     *
     * @return
     */
    Queue<IBlockPlacerEntry> getQueue();

    /**
     * Get block placing speed (blocks per second)
     *
     * @return
     */
    double getSpeed();

    /**
     * Has any job entries
     *
     * @return
     */
    boolean hasJobs();

    /**
     * Is the player informed about queue overload
     *
     * @return
     */
    boolean isInformed();

    /**
     * Print jobs message
     *
     * @param lines
     */
    void printJobs(List<String> lines);

    /**
     * Remove job
     *
     * @param job
     */
    void removeJob(IJobEntry job);

    /**
     * Remove job
     *
     * @param jobId
     */
    void removeJob(int jobId);

    /**
     * Set isInformed state
     *
     * @param state
     */
    void setInformed(boolean state);

    /**
     * Set the maximum number of blocks on queue
     * @param val
     */
    void setMaxQueueBlocks(int val);

    /**
     * Change current queue to new queue
     *
     * @param newQueue
     */
    void updateQueue(Queue<IBlockPlacerEntry> newQueue);

    /**
     * Update block placing speed
     *
     * @param blocks number of blocks
     * @param timeDelta time spend
     */
    void updateSpeed(double blocks, long timeDelta);
    
}
