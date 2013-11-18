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

import javax.print.attribute.standard.JobSheets;
import org.bukkit.ChatColor;
import org.primesoft.asyncworldedit.worldedit.AsyncEditSession;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;

/**
 *
 * @author SBPrime
 */
public class BlockPlacerJobEntry extends BlockPlacerEntry {
    public enum JobStatus {
        Initializing,
        Preparing,
        Waiting,
        PlacingBlocks,
        Done
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
    private CancelabeEditSession m_cEditSession;

    public BlockPlacerJobEntry(AsyncEditSession editSession,
                               CancelabeEditSession cEditSession,
                               int jobId, String name) {
        super(editSession, jobId);

        m_name = name;
        m_status = JobStatus.Initializing;
        m_cEditSession = cEditSession;
    }

    /**
     * Is the job started
     *
     * @return
     */
    public JobStatus getStatus() {
        return m_status;
    }

    public String getName() {
        return m_name;
    }

    public void setStatus(JobStatus newStatus) {
        int newS = getStatusId(newStatus);
        int oldS = getStatusId(m_status);
        if (newS < oldS) {
            return;
        }
        m_status = newStatus;
    }

    public void cancel() {
        if (m_cEditSession != null) {
            m_cEditSession.cancel();
        }
    }

    /**
     * Get job status order code
     *
     * @param status
     * @return
     */
    private int getStatusId(JobStatus status) {
        switch (status) {
            case Done:
                return 4;
            case Initializing:
                return 0;
            case PlacingBlocks:
                return 3;
            case Preparing:
                return 1;
            case Waiting:
                return 2;
            default:
                return -1;
        }
    }

    public String getStatusString() {
        switch (m_status) {
            case Done:
                return ChatColor.GREEN + "done";
            case Initializing:
                return ChatColor.WHITE + "initializing";
            case PlacingBlocks:
                return ChatColor.GREEN + "placing blocks";
            case Preparing:
                return ChatColor.RED + "preparing blocks";
            case Waiting:
                return ChatColor.YELLOW + "waiting";
        }

        return "";
    }

    @Override
    public String toString() {
        return ChatColor.WHITE + "[" + getJobId() + "] " + getName();
    }
}
