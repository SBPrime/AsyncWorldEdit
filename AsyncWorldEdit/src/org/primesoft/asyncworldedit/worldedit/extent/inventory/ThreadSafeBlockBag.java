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
package org.primesoft.asyncworldedit.worldedit.extent.inventory;

import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagException;

/**
 *
 * @author SBPrime
 */
public class ThreadSafeBlockBag extends BlockBag {

    /**
     * Wrap the block bag
     *
     * @param parrent
     * @return
     */
    public static BlockBag warap(BlockBag parrent) {
        if (parrent == null) {
            return null;
        }

        return new ThreadSafeBlockBag(parrent);
    }

    private final BlockBag m_parrent;

    private final Object m_mutex = new Object();

    private ThreadSafeBlockBag(BlockBag parrent) {
        m_parrent = parrent;
    }

    @Override
    public void flushChanges() {
    }

    @Override
    public void addSourcePosition(WorldVector pos) {
        synchronized (m_mutex) {
            m_parrent.addSourcePosition(pos);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void addSingleSourcePosition(WorldVector pos) {
        synchronized (m_mutex) {
            m_parrent.addSingleSourcePosition(pos);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void fetchBlock(int id) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.fetchBlock(id);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void fetchItem(BaseItem item) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.fetchItem(item);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void fetchPlacedBlock(int id) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.fetchPlacedBlock(id);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void fetchPlacedBlock(int id, int data) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.fetchPlacedBlock(id, data);
            m_parrent.flushChanges();
        }
    }

    @Override
    public boolean peekBlock(int id) {
        synchronized (m_mutex) {
            return m_parrent.peekBlock(id);
        }
    }

    @Override
    public void storeBlock(int id) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.storeBlock(id);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void storeDroppedBlock(int id) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.storeDroppedBlock(id);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void storeDroppedBlock(int id, int data) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.storeDroppedBlock(id, data);
            m_parrent.flushChanges();
        }
    }

    @Override
    public void storeItem(BaseItem item) throws BlockBagException {
        synchronized (m_mutex) {
            m_parrent.storeItem(item);
            m_parrent.flushChanges();
        }
    }
}
