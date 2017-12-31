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

import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.IInnerSerializerManager;
import org.primesoft.asyncworldedit.api.playerManager.IPlayerEntry;
import org.primesoft.asyncworldedit.api.utils.IAction;
import org.primesoft.asyncworldedit.api.utils.IFunc;
import org.primesoft.asyncworldedit.api.worldedit.ICancelabeEditSession;
import org.primesoft.asyncworldedit.changesetSerializer.StreamProvider;
import org.primesoft.asyncworldedit.changesetSerializer.iterators.BaseFileIterator;
import org.primesoft.asyncworldedit.changesetSerializer.iterators.UndoFileBackwordIterator;
import org.primesoft.asyncworldedit.changesetSerializer.iterators.UndoFileForwardIterator;
import org.primesoft.asyncworldedit.configuration.ConfigProvider;
import org.primesoft.asyncworldedit.configuration.ConfigUndo;
import org.primesoft.asyncworldedit.strings.MessageType;

/**
 *
 * @author SBPrime
 */
public class FileChangeSet implements ChangeSet {

    private final static int MAX_SAVE = 100;
    private final static Iterator<Change> EMPTY_ITERATOR = new Iterator<Change>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Change next() {
            return null;
        }

        @Override
        public void remove() {
        }
    };

    private final Queue<Change> m_queuedChanges = new LinkedList<Change>();
    private boolean m_isDisposed = false;
    private int m_size = 0;
    private File m_storageFile;

    private final IInnerSerializerManager m_changesetSerializer;
    private ICancelabeEditSession m_cancelable;
    private final IPlayerEntry m_player;
    private final Object m_saveMutex = new Object();
    private boolean m_savePending = false;

    public FileChangeSet(IAsyncWorldEditCore aweCore, IPlayerEntry player) {
        m_changesetSerializer = aweCore.getInnerChangesetSerializer();
        m_player = player;
    }

    public void setCancelable(ICancelabeEditSession editSession) {
        m_cancelable = editSession;
    }

    @Override
    public void add(Change change) {
        if (change == null && !m_isDisposed) {
            return;
        }
        synchronized (m_queuedChanges) {
            m_queuedChanges.add(change);
        }

        FileChangeSetManager.notiffy();
    }

    @Override
    public Iterator<Change> backwardIterator() {
        if (m_player != null) {
            m_player.say(MessageType.LOADING_UNDO.format());
        }

        return initializeIterator(new UndoFileBackwordIterator(m_changesetSerializer));
    }

    @Override
    public Iterator<Change> forwardIterator() {
        if (m_player != null) {
            m_player.say(MessageType.LOADING_UNDO.format());
        }

        return initializeIterator(new UndoFileForwardIterator(m_changesetSerializer));
    }

    @Override
    public int size() {
        synchronized (m_queuedChanges) {
            return m_size + m_queuedChanges.size();
        }
    }

    public void initialize(IPlayerEntry player, int newId) {
        m_storageFile = m_changesetSerializer.open(player, newId, true);

        ConfigUndo undoConfig = ConfigProvider.undo();
        boolean removeFile = undoConfig != null && !undoConfig.keepUndoFile();

        final StreamProvider sp = StreamProvider.getInstance();
        if (!sp.initializeStream(m_storageFile, removeFile)
                || !sp.initializeStream(new File(m_storageFile.getPath() + ".idx"), removeFile)) {
            log(String.format("Warning: unable to reserve undo file %1$s.", m_storageFile));
        }

        FileChangeSetManager.start(this);
    }

    public void close() {
        synchronized (m_queuedChanges) {
            m_queuedChanges.clear();

            m_isDisposed = true;
        }
        m_changesetSerializer.close(m_storageFile);
        FileChangeSetManager.stop(this);

        if (m_storageFile == null) {
            return;
        }

        final StreamProvider sp = StreamProvider.getInstance();
        sp.disposeStream(m_storageFile);
        sp.disposeStream(new File(m_storageFile.getPath() + ".idx"));
    }

    /**
     * Save the change set
     *
     * @return
     */
    boolean save() {
        List<Change> dataToSave = new ArrayList<Change>();

        synchronized (m_saveMutex) {
            m_savePending = true;
        }

        synchronized (m_queuedChanges) {
            while (!m_queuedChanges.isEmpty() && !m_isDisposed /*&& dataToSave.size() < MAX_SAVE*/) {
                dataToSave.add(m_queuedChanges.poll());
                m_size++;
            }
        }

        try {
            if (dataToSave.isEmpty()) {
                return false;
            }

            m_changesetSerializer.save(m_storageFile, dataToSave);
            return true;
        } finally {
            synchronized (m_saveMutex) {
                m_savePending = false;
                m_saveMutex.notifyAll();
            }
        }
    }

    /**
     * Load all changes stored on disk and queued
     *
     * @return
     */
    private List<Change> load() {
        return performSaveSynced(new IFunc<List<Change>>() {
            @Override
            public List<Change> execute() {
                List<Change> list = m_changesetSerializer.load(m_storageFile, m_size, m_player, m_cancelable);

                synchronized (m_queuedChanges) {
                    for (Change change : m_queuedChanges) {
                        list.add(change);
                    }
                }

                return list;
            }
        });
    }

    /**
     * Perform an action in sync with the data saver. Waits for all save actions
     * to finish
     *
     * @param action
     */
    private void performSaveSynced(IAction action) {
        if (action == null) {
            return;
        }

        synchronized (m_saveMutex) {
            while (m_savePending) {
                try {
                    m_saveMutex.wait(100);
                } catch (InterruptedException ex) {
                }
            }

            action.execute();
        }
    }

    /**
     * Perform an action in sync with the data saver. Waits for all save actions
     * to finish
     *
     * @param action
     */
    private <T> T performSaveSynced(IFunc<T> action) {
        if (action == null) {
            return null;
        }

        T result;
        synchronized (m_saveMutex) {
            while (m_savePending) {
                try {
                    m_saveMutex.wait(100);
                } catch (InterruptedException ex) {
                }
            }

            result = action.execute();
        }

        return result;
    }

    /**
     * Initialize the file iterator
     *
     * @param fileIterator
     * @param file
     * @return
     */
    private Iterator<Change> initializeIterator(final BaseFileIterator fileIterator) {
        if (fileIterator == null) {
            return EMPTY_ITERATOR;
        }

        File storageFile = m_storageFile;
        if (storageFile == null) {
            log("Error: the undo storage file is not set, undo broken.");
            return EMPTY_ITERATOR;
        }

        performSaveSynced(new IAction() {

            @Override
            public void execute() {
                int size;
                Change[] memoryChanges;

                synchronized (m_queuedChanges) {
                    memoryChanges = m_queuedChanges.toArray(new Change[0]);
                    size = m_size;
                }

                fileIterator.initializeData(size, memoryChanges);
            }
        });

        if (!fileIterator.initializeStream(storageFile)) {
            return EMPTY_ITERATOR;
        }

        return fileIterator;
    }
}
