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
package org.primesoft.asyncworldedit.worldedit.history.changeset;

import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author SBPrime
 */
public class ThreadSafeChangeSet implements ChangeSet {
    /**
     * If a iterator implements this interface it will be forwarded
     * without any transformation.
     */
    public interface IThreadSafeIterator {}
    

    /**
     * The parent change set
     */
    private final ChangeSet m_parent;

    /**
     * The MTA mutex
     */
    private final Object m_mutex;

    public ThreadSafeChangeSet(ChangeSet changeSet) {
        if (changeSet == null) {
            throw new IllegalArgumentException("Change set is null");
        }

        m_parent = changeSet;

        m_mutex = new Object();
    }

    @Override
    public void add(Change change) {
        synchronized (m_mutex) {
            m_parent.add(change);
        }
    }

    @Override
    public Iterator<Change> backwardIterator() {
        final Iterator<Change> iterator;

        synchronized (m_mutex) {
            iterator = wrapIterator(m_parent.backwardIterator());
        }
        
        return iterator;
    }

    @Override
    public Iterator<Change> forwardIterator() {
        final Iterator<Change> iterator;

        synchronized (m_mutex) {
            iterator = wrapIterator(m_parent.forwardIterator());
        }
        
        return iterator;
    }

    @Override
    public int size() {
        synchronized (m_mutex) {
            return m_parent.size();
        }
    }

    
    /**
     * Checks if the provided iterator is thread safe.
     * If not it caches all items and provides a thread safe iterator
     * @param iterator
     * @return 
     */
    private Iterator<Change> wrapIterator(Iterator<Change> iterator) {
        if (iterator == null) {
            return null;
        }
        
        if (iterator instanceof IThreadSafeIterator) {
            return iterator;
        }
        
        List<Change> list = new ArrayList<Change>();
        while (iterator.hasNext()) {
            list.add((Change) iterator.next());
        }
        
        return list.iterator();
    }
}
