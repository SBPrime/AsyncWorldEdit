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
package org.primesoft.asyncworldedit.changesetSerializer.iterators;

import com.sk89q.worldedit.history.change.Change;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import org.primesoft.asyncworldedit.api.changesetSerializer.IMemoryStorage;
import org.primesoft.asyncworldedit.api.inner.IChunkCacheStream;
import org.primesoft.asyncworldedit.api.inner.IInnerSerializerManager;
import org.primesoft.asyncworldedit.api.utils.IDisposable;
import org.primesoft.asyncworldedit.changesetSerializer.StreamProvider;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.io.ChunkCacheStream;
import org.primesoft.asyncworldedit.worldedit.history.changeset.ThreadSafeChangeSet;

/**
 *
 * @author SBPrime
 */
public abstract class BaseFileIterator implements Iterator<Change>, ThreadSafeChangeSet.IThreadSafeIterator, IDisposable {

    protected final IInnerSerializerManager m_changesetSerializer;

    protected int m_position = 0;

    /**
     * The MTA mutex
     */
    protected final Object m_mutex = new Object();

    /**
     * Is the iterator disposed
     */
    protected boolean m_isDisposed = false;

    protected File m_file;
    protected RandomAccessFile m_stream;
    protected IChunkCacheStream m_dataStream;
    protected int m_storageSize;
    protected Change[] m_memoryChanges;
    protected IMemoryStorage m_memoryStorage;

    protected BaseFileIterator(IInnerSerializerManager changesetSerializer) {
        m_changesetSerializer = changesetSerializer;
    }

    @Override
    protected void finalize() throws Throwable {
        dispose();

        super.finalize();
    }

    /**
     * Initialize the iterator stream
     *
     * @param storageFile
     * @return
     */
    public boolean initializeStream(File storageFile) {
        if (storageFile == null) {
            return false;
        }

        final StreamProvider sp = StreamProvider.getInstance();
        final RandomAccessFile stream;
        final IChunkCacheStream dataStream;

        sp.reserve();
        sp.addReference(storageFile);

        try {
            stream = new RandomAccessFile(storageFile, "r");
            dataStream = new ChunkCacheStream(stream);

        } catch (IOException ex) {
            ExceptionHelper.printException(ex, String.format("Unable to create undo iterator for %1$s", storageFile.getName()));

            sp.removeReference(storageFile);
            sp.release();

            return false;
        }

        synchronized (m_mutex) {
            if (m_isDisposed) {
                sp.removeReference(storageFile);
                sp.release();

                return false;
            }

            m_file = storageFile;
            m_stream = stream;
            m_dataStream = dataStream;
            m_memoryStorage = m_changesetSerializer.getMemoryStorage(storageFile);
        }

        return true;
    }

    /**
     * Initialize iterator data
     *
     * @param size
     * @param memoryChanges
     */
    public void initializeData(int size, Change[] memoryChanges) {
        m_storageSize = Math.max(0, size);
        m_memoryChanges = memoryChanges == null ? new Change[0] : memoryChanges;
    }

    /**
     * Close all iterator data
     */
    @Override
    public void dispose() {
        final StreamProvider sp = StreamProvider.getInstance();

        synchronized (m_mutex) {
            if (m_isDisposed) {
                return;
            }

            if (m_file == null) {
                return;
            }

            try {
                m_stream.close();
            } catch (IOException ioe) {

            }

            sp.removeReference(m_file);
            sp.release();

            m_isDisposed = true;
        }
    }

    @Override
    public boolean hasNext() {
        int tmp = m_position - m_storageSize;

        if (tmp < 0) {
            return true;
        }

        boolean result = tmp < m_memoryChanges.length;
        if (!result) {
            dispose();
        }

        return result;
    }
    
    
    @Override
    public Change next() {
        int tmp = m_position - m_storageSize;
        Change item;

        if (tmp < 0) {
            item = readFromFile(m_position);
        } else if (tmp < m_memoryChanges.length) {
            item = getFromMemory(tmp);
        } else {
            dispose();
            return null;
        }

        m_position++;
        return item;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("This operation is not supported.");
    }

    
    /**
     * Try to load change from file
     *
     * @param position
     * @return
     */
    protected abstract Change readFromFile(int position);

    /**
     * Try to get change from memory
     * @param position
     * @return 
     */
    protected abstract Change getFromMemory(int position);
}
