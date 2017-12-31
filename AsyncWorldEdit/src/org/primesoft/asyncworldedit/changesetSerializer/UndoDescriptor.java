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
package org.primesoft.asyncworldedit.changesetSerializer;

import com.sk89q.worldedit.history.change.Change;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.primesoft.asyncworldedit.api.changesetSerializer.IMemoryStorage;

/**
 *
 * @author SBPrime
 */
public class UndoDescriptor implements IMemoryStorage {

    private final File m_file;
    private boolean m_isClosed = false;
    private final Object m_mutex = new Object();
    private long m_entryIdx;

    private final Map<UUID, Change> m_memoryChanges = new LinkedHashMap<UUID, Change>();

    /**
     * The current entry IDX
     *
     * @return
     */
    public long getIdx() {
        return m_entryIdx;
    }

    /**
     * The current entry IDX increment it
     *
     * @return
     */
    public long getAndIncrementIdx() {
        return m_entryIdx++;
    }

    /**
     * Increase the Idx
     *
     * @return
     */
    public long incIdx() {
        return (++m_entryIdx);
    }

    UndoDescriptor(File file) throws FileNotFoundException {
        m_file = file;
    }

    /**
     * Get the MTA synchronization mutex
     *
     * @return
     */
    public Object getMutex() {
        return m_mutex;
    }

    /**
     * Get the undo file
     *
     * @return
     */
    File getFile() {
        return m_file;
    }

    /**
     * Store the change in memory
     *
     * @param change
     */
    @Override
    public UUID storeInMemory(Change change) {
        if (change == null) {
            return null;
        }

        UUID result = UUID.randomUUID();
        synchronized (m_memoryChanges) {
            m_memoryChanges.put(result, change);
        }

        return result;
    }

    /**
     * Get the change from memory
     *
     * @param uuid
     * @return
     */
    @Override
    public Change getFromMemory(UUID uuid) {
        if (uuid == null) {
            return null;
        }

        synchronized (m_memoryChanges) {
            if (m_memoryChanges.containsKey(uuid)) {
                return m_memoryChanges.get(uuid);
            }
        }

        return null;
    }

    @Override
    public void removeFromMemory(UUID uuid) {
        if (uuid == null) {
            return;
        }

        synchronized (m_memoryChanges) {
            if (m_memoryChanges.containsKey(uuid)) {
                m_memoryChanges.remove(uuid);
            }
        }
    }

    void close() throws IOException {
        m_isClosed = true;
    }

    boolean isClosed() {
        return m_isClosed;
    }
}
