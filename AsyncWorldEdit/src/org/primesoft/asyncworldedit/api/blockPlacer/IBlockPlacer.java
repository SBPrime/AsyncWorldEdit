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

import com.sk89q.worldedit.MaxChangedBlocksException;
import org.primesoft.asyncworldedit.api.blockPlacer.entries.IJobEntry;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IAsyncCommand;
import org.primesoft.asyncworldedit.api.utils.IFuncParamEx;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;

/**
 *
 * @author SBPrime
 */
public interface IBlockPlacer {

    /**
     * Add new job for player
     *
     * @param player player UUID
     * @param job the job
     * @return
     */
    boolean addJob(IPlayerEntry player, IJobEntry job);

    /**
     * Add event listener
     *
     * @param listener
     */
    void addListener(IBlockPlacerListener listener);

    /**
     * Add task to perform in async mode
     *
     * @param player
     * @param entry
     * @return
     */
    boolean addTasks(IPlayerEntry player, IBlockPlacerEntry entry);

    /**
     * Cancel job
     *
     * @param player
     * @param jobId
     * @return
     */
    int cancelJob(IPlayerEntry player, int jobId);

    /**
     * Get all players in log
     *
     * @return players list
     */
    IPlayerEntry[] getAllPlayers();

    /**
     * Get the player job
     *
     * @param player player uuid
     * @param jobId job ID
     * @return
     */
    IJobEntry getJob(IPlayerEntry player, int jobId);

    /**
     * Get next job id for player
     *
     * @param player
     * @return
     */
    int getJobId(IPlayerEntry player);

    /**
     * Gets the number of events for a player
     *
     * @param player player login
     * @return number of stored events
     */
    IBlockPlacerPlayer getPlayerEvents(IPlayerEntry player);

    /**
     * Is the blocks placer paused
     * @return
     */
    boolean isPaused();

    /**
     * Wrap action into AsyncWorldEdit job and perform it asynchronously
     *
     * @param editSession
     * @param player
     * @param jobName
     * @param action
     */
    void performAsAsyncJob(final IThreadSafeEditSession editSession, final IPlayerEntry player, final String jobName, 
            final IFuncParamEx<Integer, ICancelabeEditSession, MaxChangedBlocksException> action);
    
    /**
     * Perform an async command asynchronously
     *
     * @param editSession
     * @param asyncCommand
     */
    void performAsAsyncJob(final IThreadSafeEditSession editSession, final IAsyncCommand asyncCommand);
    

    /**
     * Remove all entries for player
     *
     * @param player
     * @return
     */
    int purge(IPlayerEntry player);

    /**
     * Remove all entries
     *
     * @return Number of purged job entries
     */
    int purgeAll();

    /**
     * Remove the player job
     *
     * @param player
     * @param jobEntry
     */
    void removeJob(final IPlayerEntry player, IJobEntry jobEntry);

    /**
     * Remove event listener
     *
     * @param listener
     */
    void removeListener(IBlockPlacerListener listener);

    /**
     * Set pause on blocks placer
     * @param pause
     */
    void setPause(boolean pause);
    
}
