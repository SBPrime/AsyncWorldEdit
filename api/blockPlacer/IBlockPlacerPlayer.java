/*
 * AsyncWorldEdit Premium is a commercial version of AsyncWorldEdit. This software 
 * has been sublicensed by the software original author according to p7 of
 * AsyncWorldEdit license.
 *
 * AsyncWorldEdit Premium - donation version of AsyncWorldEdit, a performance 
 * improvement plugin for Minecraft WorldEdit plugin.
 *
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 *
 * All rights reserved.
 *
 * 1. You may: 
 *    install and use AsyncWorldEdit in accordance with the Software documentation
 *    and pursuant to the terms and conditions of this license
 * 2. You may not:
 *    sell, redistribute, encumber, give, lend, rent, lease, sublicense, or otherwise
 *    transfer Software, or any portions of Software, to anyone without the prior 
 *    written consent of Licensor
 * 3. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 4. The original author of the software is allowed to sublicense the software 
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
