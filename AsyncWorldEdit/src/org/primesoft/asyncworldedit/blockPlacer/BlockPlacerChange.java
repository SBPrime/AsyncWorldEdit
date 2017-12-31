/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.change.Change;
import org.primesoft.asyncworldedit.api.blockPlacer.IBlockPlacer;
import org.primesoft.asyncworldedit.blockPlacer.entries.ActionEntryEx;
import org.primesoft.asyncworldedit.api.utils.IActionEx;
import org.primesoft.asyncworldedit.worldedit.CancelabeEditSession;
import org.primesoft.asyncworldedit.worldedit.history.ExtendedUndoContext;

/**
 * Block placer wrapper for WorldEdit Change
 *
 * @author SBPrime
 */
public class BlockPlacerChange implements Change {

    private final IBlockPlacer m_blockPlacer;
    private final Change m_change;
    private final boolean m_isDemanding;

    /**
     * Is the change demanding
     *
     * @return
     */
    public boolean isDemanding() {
        return m_isDemanding;
    }

    /**
     * Get the underying change
     *
     * @return
     */
    public Change getChange() {
        return m_change;
    }

    /**
     * Create new instance of the block placer change
     *
     * @param change
     * @param blockPlacer
     * @param isDemanding
     */
    public BlockPlacerChange(final Change change,
            IBlockPlacer blockPlacer, boolean isDemanding) {
        m_change = change;
        m_blockPlacer = blockPlacer;
        m_isDemanding = isDemanding;
    }

    /**
     * The undo operation
     *
     * @param uc
     * @throws WorldEditException
     */
    @Override
    public void undo(final UndoContext uc) throws WorldEditException {
        if (m_change == null) {
            return;
        }

        ExtendedUndoContext euc = (uc instanceof ExtendedUndoContext)
                ? (ExtendedUndoContext) uc : null;
        EditSession sender = euc != null ? euc.getSender() : null;
        CancelabeEditSession cEditSession = sender != null && sender instanceof CancelabeEditSession
                ? (CancelabeEditSession) sender : null;

        if (cEditSession == null) {
            m_change.undo(uc);
            return;
        }

        final IActionEx<WorldEditException> action = new IActionEx<WorldEditException>() {
            @Override
            public void execute() throws WorldEditException {
                m_change.undo(uc);
            }
        };

        BlockPlacerEntry entry = new ActionEntryEx(cEditSession.getJobId(), action, m_isDemanding);
        m_blockPlacer.addTasks(cEditSession.getPlayer(), entry);
    }

    /**
     * The redo operation
     *
     * @param uc
     * @throws WorldEditException
     */
    @Override
    public void redo(final UndoContext uc) throws WorldEditException {
        if (m_change == null) {
            return;
        }

        ExtendedUndoContext euc = (uc instanceof ExtendedUndoContext)
                ? (ExtendedUndoContext) uc : null;
        EditSession sender = euc != null ? euc.getSender() : null;
        CancelabeEditSession cEditSession = sender != null && sender instanceof CancelabeEditSession
                ? (CancelabeEditSession) sender : null;

        if (cEditSession == null) {
            m_change.redo(uc);
            return;
        }

        final IActionEx<WorldEditException> action = new IActionEx<WorldEditException>() {
            @Override
            public void execute() throws WorldEditException {
                m_change.redo(uc);
            }
        };

        BlockPlacerEntry entry = new ActionEntryEx(cEditSession.getJobId(), action, m_isDemanding);
        m_blockPlacer.addTasks(cEditSession.getPlayer(), entry);
    }

}
