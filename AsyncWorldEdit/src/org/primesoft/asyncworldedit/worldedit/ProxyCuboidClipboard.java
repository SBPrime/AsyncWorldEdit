/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Countable;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 * This class is only a wrapper proxy Note: Do not use any operations from this
 * class, always use th parrent!
 *
 * @author SBPrime
 */
@Deprecated
public class ProxyCuboidClipboard extends CuboidClipboard {

    /**
     * The parrent clipboard
     */
    protected final CuboidClipboard m_parrent;

    public ProxyCuboidClipboard(CuboidClipboard parrent) {
        super(parrent.getSize(), parrent.getOrigin(), parrent.getOffset());

        m_parrent = parrent;
    }

    @Override
    public void copy(EditSession editSession) {
        synchronized (m_parrent) {
            m_parrent.copy(editSession);
        }
        updateProps();
    }

    @Override
    public void copy(EditSession editSession, Region region) {
        synchronized (m_parrent) {
            super.copy(editSession, region);
        }
        updateProps();
    }

    @Override
    public void flip(FlipDirection dir) {
        synchronized (m_parrent) {
            m_parrent.flip(dir);
        }
        updateProps();
    }

    @Override
    public void flip(FlipDirection dir, boolean aroundPlayer) {
        synchronized (m_parrent) {
            m_parrent.flip(dir, aroundPlayer);
        }
        updateProps();
    }

    @Override
    public List<Countable<Integer>> getBlockDistribution() {
        synchronized (m_parrent) {
            return m_parrent.getBlockDistribution();
        }
    }

    @Override
    public List<Countable<BaseBlock>> getBlockDistributionWithData() {
        synchronized (m_parrent) {
            return m_parrent.getBlockDistributionWithData();
        }
    }

    @Override
    public int getHeight() {
        synchronized (m_parrent) {
            return m_parrent.getHeight();
        }
    }

    @Override
    public int getLength() {
        synchronized (m_parrent) {
            return m_parrent.getLength();
        }
    }

    @Override
    public Vector getOffset() {
        synchronized (m_parrent) {
            return m_parrent.getOffset();
        }
    }

    @Override
    public Vector getOrigin() {
        synchronized (m_parrent) {
            return m_parrent.getOrigin();
        }
    }

    @Override
    public BaseBlock getPoint(Vector pos)
            throws ArrayIndexOutOfBoundsException {
        synchronized (m_parrent) {
            return m_parrent.getPoint(pos);
        }
    }

    @Override
    public Vector getSize() {
        synchronized (m_parrent) {
            return m_parrent.getSize();
        }
    }

    @Override
    public int getWidth() {
        synchronized (m_parrent) {
            return m_parrent.getWidth();
        }
    }

    @Override
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir, boolean entities) throws MaxChangedBlocksException {
        super.paste(editSession, newOrigin, noAir, entities);
        //No change needed this calls:
        //public void paste(EditSession editSession, Vector newOrigin, boolean noAir)
        //public LocalEntity[] pasteEntities(Vector pos)
    }

    @Override
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir)
            throws MaxChangedBlocksException {
        synchronized (m_parrent) {
            m_parrent.paste(editSession, newOrigin, noAir);
        }
        updateProps();
    }

    @Override
    public LocalEntity[] pasteEntities(Vector pos) {
        synchronized (m_parrent) {
            return m_parrent.pasteEntities(pos);
        }
    }

    @Override
    public void place(EditSession editSession, Vector pos, boolean noAir)
            throws MaxChangedBlocksException {
        synchronized (m_parrent) {
            m_parrent.place(editSession, pos, noAir);
        }
        updateProps();
    }

    @Override
    public void rotate2D(int angle) {
        synchronized (m_parrent) {
            m_parrent.rotate2D(angle);
        }
        updateProps();
    }

    @Override
    public void saveSchematic(File path)
            throws IOException, DataException, com.sk89q.worldedit.world.DataException {
        synchronized (m_parrent) {
            m_parrent.saveSchematic(path);
        }
    }

    @Override
    public void setBlock(Vector pt, BaseBlock block) {
        synchronized (m_parrent) {
            m_parrent.setBlock(pt, block);
        }
    }

    @Override
    public void setOffset(Vector offset) {
        synchronized (m_parrent) {
            m_parrent.setOffset(offset);

            super.setOffset(offset);
        }
    }

    @Override
    public void setOrigin(Vector origin) {
        synchronized (m_parrent) {
            m_parrent.setOrigin(origin);

            super.setOrigin(origin);
        }
    }

    @Override
    public void storeEntity(LocalEntity entity) {
        synchronized (m_parrent) {
            m_parrent.storeEntity(entity);
        }
    }

    /**
     * Inject a LocalSession wrapper factory using reflection
     *
     * @param size
     */
    public void setSize(Vector size) {
        Reflection.set(CuboidClipboard.class, this, "size", size, "Unable to set clipboard size");
    }

    /**
     * Update all properties based on the parrent
     */
    protected void updateProps() {
        synchronized (m_parrent) {
            setOffset(m_parrent.getOffset());
            setOrigin(m_parrent.getOrigin());
            setSize(m_parrent.getSize());
        }
    }
}
