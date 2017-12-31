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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.primesoft.asyncworldedit.api.worldedit.IThreadSafeEditSession;
import org.primesoft.asyncworldedit.worldedit.history.changeset.FileChangeSet;

/**
 *
 * @author SBPrime
 */
public class SerializableSessionList extends LinkedList<EditSession> {

    private final Map<EditSession, Integer> m_usedId = new LinkedHashMap<EditSession, Integer>();
    private final LinkedList<Integer> m_availableIds = new LinkedList<Integer>();
    private final Object m_mutex = new Object();

    private int m_id = 0;

    public SerializableSessionList() {
    }

    private void assignSession(EditSession session) {
        final IThreadSafeEditSession tsSession = (session instanceof IThreadSafeEditSession) ? (IThreadSafeEditSession) session : null;
        if (tsSession == null) {
            return;
        }

        final ChangeSet tmpChangeSet = tsSession.getRootChangeSet();
        final FileChangeSet fileChangeSet = tmpChangeSet instanceof FileChangeSet ? (FileChangeSet) tmpChangeSet : null;

        if (fileChangeSet == null) {
            return;
        }

        final int newId;
        synchronized (m_mutex) {
            if (!m_availableIds.isEmpty()) {
                newId = m_availableIds.poll();
            } else {
                newId = m_id;
                m_id++;
            }

            m_usedId.put(session, newId);
        }

        fileChangeSet.initialize(tsSession.getPlayer(), newId);
    }

    private void releaseSession(EditSession session) {
        final IThreadSafeEditSession tsSession = (session instanceof IThreadSafeEditSession) ? (IThreadSafeEditSession) session : null;
        final ChangeSet tmpChangeSet = tsSession != null ? tsSession.getRootChangeSet() : null;
        final FileChangeSet fileChangeSet = tmpChangeSet instanceof FileChangeSet ? (FileChangeSet) tmpChangeSet : null;

        if (fileChangeSet == null) {
            return;
        }

        synchronized (m_mutex) {
            if (m_usedId.containsKey(session)) {
                int oldId = m_usedId.get(session);
                m_usedId.remove(session);

                m_availableIds.add(oldId);
            }
        }

        fileChangeSet.close();
    }

    @Override
    public void clear() {
        for (EditSession es : this) {
            releaseSession(es);
        }

        super.clear();
    }

    @Override
    public boolean add(EditSession e) {
        assignSession(e);
        return super.add(e);
    }

    @Override
    public void add(int index, EditSession element) {
        assignSession(element);
        super.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends EditSession> c) {
        List<EditSession> tmp = c == null ? new ArrayList<EditSession>() : new ArrayList<EditSession>(c);

        for (EditSession session : tmp) {
            assignSession(session);
        }

        return super.addAll(tmp);
    }

    @Override
    public boolean addAll(int index, Collection<? extends EditSession> c) {
        List<EditSession> tmp = c == null ? new ArrayList<EditSession>() : new ArrayList<EditSession>(c);

        for (EditSession session : tmp) {
            assignSession(session);
        }

        return super.addAll(index, tmp);
    }

    @Override
    public void addFirst(EditSession e) {
        assignSession(e);

        super.addFirst(e);
    }

    @Override
    public void addLast(EditSession e) {
        assignSession(e);

        super.addLast(e);
    }

    @Override
    public EditSession poll() {
        EditSession result = super.poll();
        releaseSession(result);

        return result;
    }

    @Override
    public EditSession pollFirst() {
        EditSession result = super.pollFirst();
        releaseSession(result);

        return result;
    }

    @Override
    public EditSession pollLast() {
        EditSession result = super.pollLast();
        releaseSession(result);

        return result;
    }

    @Override
    public EditSession pop() {
        EditSession result = super.pop();
        releaseSession(result);

        return result;
    }

    @Override
    public void push(EditSession e) {
        assignSession(e);
        super.push(e);
    }

    @Override
    public EditSession remove() {
        EditSession result = super.remove();
        releaseSession(result);

        return result;
    }

    @Override
    public boolean remove(Object o) {
        EditSession es = o instanceof EditSession ? (EditSession) o : null;
        releaseSession(es);
        return super.remove(o);
    }

    @Override
    public EditSession remove(int index) {
        EditSession result = super.remove(index);
        releaseSession(result);

        return result;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        final List tmp = new ArrayList(c);
        for (Object o : tmp) {
            EditSession es = o instanceof EditSession ? (EditSession) o : null;
            releaseSession(es);
        }

        return super.removeAll(tmp);
    }

    @Override
    public EditSession removeFirst() {
        EditSession result = super.removeFirst();
        releaseSession(result);

        return result;
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        EditSession es = o instanceof EditSession ? (EditSession) o : null;
        releaseSession(es);

        return super.removeFirstOccurrence(o);
    }

    @Override
    public EditSession removeLast() {
        EditSession result = super.removeLast();
        releaseSession(result);

        return result;
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        EditSession es = o instanceof EditSession ? (EditSession) o : null;
        releaseSession(es);

        return super.removeLastOccurrence(o);
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        for (EditSession es : subList(fromIndex, toIndex)) {
            releaseSession(es);
        }

        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List tmp = new ArrayList(c);

        for (EditSession es : this) {
            if (!tmp.contains(es)) {
                releaseSession(es);
            }
        }

        return super.retainAll(tmp);
    }

    @Override
    public EditSession set(int index, EditSession element) {
        EditSession old = get(index);

        releaseSession(old);
        assignSession(old);

        return super.set(index, element);
    }
}
