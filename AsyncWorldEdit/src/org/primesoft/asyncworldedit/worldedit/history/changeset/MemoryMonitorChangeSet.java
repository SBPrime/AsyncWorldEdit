/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit.history.changeset;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import java.util.Iterator;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.configuration.ConfigMemory;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.ConfigUndo;
import org.primesoft.asyncworldedit.configuration.UndoBehaviour;
import org.primesoft.asyncworldedit.strings.MessageType;
import org.primesoft.asyncworldedit.utils.GCUtils;

/**
 *
 * @author SBPrime
 */
public class MemoryMonitorChangeSet implements IExtendedChangeSet {

    /**
     * The parrent change set
     */
    private final ChangeSet m_parent;

    /**
     * The async undo behaviour
     */
    private final UndoBehaviour m_behaviourAsync;

    /**
     * The main thread undo behaviour
     */
    private final UndoBehaviour m_behaviourMain;

    /**
     * The AWE task dispatcher
     */
    private final ITaskDispatcher m_taskDispatcher;

    /**
     * Minimum free memory
     */
    private final long m_minMemoryHard;

    /**
     * Minimum free memory
     */
    private final long m_minMemorySoft;

    /**
     * The wait mutex
     */
    private final Object m_mutex = new Object();
    
    /**
     * THe player
     */
    private final IPlayerEntry m_player;

    public MemoryMonitorChangeSet(IPlayerEntry player, ITaskDispatcher taskDispatcher, ChangeSet parent) {
        m_player = player;
        m_taskDispatcher = taskDispatcher;
        m_parent = parent;

        ConfigUndo uConfig = ConfigProvider.undo();
        ConfigMemory mConfig = ConfigProvider.memory();

        m_minMemoryHard = mConfig.getMinMemoryHard() * 1000;
        m_minMemorySoft = mConfig.getMinMemorySoft() * 1000;

        m_behaviourAsync = uConfig.getAsyncBehaviour();
        m_behaviourMain = uConfig.getMainBehaviour();
    }

    @Override
    public void addExtended(Change change, ICancelabeEditSession editSession) throws WorldEditException {
        UndoBehaviour behaviour = m_taskDispatcher.isMainTask() ? m_behaviourMain : m_behaviourAsync;
        if (editSession != null && editSession.isCanceled()) {
            return;
        }
        
        if (behaviour != UndoBehaviour.Off) {
            long memAvailable = GCUtils.getTotalAvailableMemory();
            boolean memLow = m_minMemoryHard > 0 && memAvailable < m_minMemoryHard;

            if (memLow) {
                m_player.say(MessageType.BLOCK_PLACER_MEMORY_LOW.format());
                
                if (behaviour == UndoBehaviour.Drop) {
                    return;
                } else if (behaviour == UndoBehaviour.Cancel) {
                    throw new MaxChangedBlocksException(0);
                } else if (behaviour == UndoBehaviour.Wait && editSession != null) {
                    do {
                        //TODO: This needs refactoring
                        synchronized (m_mutex) {
                            try {
                                m_mutex.wait(1000);
                            } catch (InterruptedException ex) {
                                return;
                            }
                        }

                        GCUtils.GC();
                    } while (GCUtils.getTotalAvailableMemory() < m_minMemorySoft && !editSession.isCanceled());
                } else if (editSession == null) {
                    throw new MaxChangedBlocksException(0);
                }
            }
        }
        m_parent.add(change);
    }

    @Override
    public void add(Change change) {
    }

    @Override
    public Iterator<Change> backwardIterator() {
        return m_parent.backwardIterator();
    }

    @Override
    public Iterator<Change> forwardIterator() {
        return m_parent.forwardIterator();
    }

    @Override
    public int size() {
        return m_parent.size();
    }

}
