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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.Countable;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.data.DataException;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class is only a wrapper proxy
 * Note: Do not use any operations from this class, always use th parrent!
 * @author SBPrime
 */
public class ProxyCuboidClipboard extends CuboidClipboard {    
    /**
     * The parrent clipboard
     */
    protected CuboidClipboard m_parrent;
    
    
    public ProxyCuboidClipboard(CuboidClipboard parrent) {
        super(new Vector());
        
        m_parrent = parrent;
    }

    @Override
    public void copy(EditSession editSession) {
        m_parrent.copy(editSession);
    }

    @Override
    public void flip(FlipDirection dir) {
        m_parrent.flip(dir);
    }

    @Override
    public void flip(FlipDirection dir, boolean aroundPlayer) {
        m_parrent.flip(dir, aroundPlayer);
    }

    @Override
    public List<Countable<Integer>> getBlockDistribution() {
        return m_parrent.getBlockDistribution();
    }

    @Override
    public List<Countable<BaseBlock>> getBlockDistributionWithData() {
        return m_parrent.getBlockDistributionWithData();
    }

    @Override
    public int getHeight() {
        return m_parrent.getHeight();
    }

    @Override
    public int getLength() {
        return m_parrent.getLength();
    }

    @Override
    public Vector getOffset() {
        return m_parrent.getOffset();
    }

    @Override
    public Vector getOrigin() {
        return m_parrent.getOrigin();
    }

    @Override
    public BaseBlock getPoint(Vector pos)
            throws ArrayIndexOutOfBoundsException {
        return m_parrent.getPoint(pos);
    }

    @Override
    public Vector getSize() {
        return m_parrent.getSize();
    }

    @Override
    public int getWidth() {
        return m_parrent.getWidth();
    }

    @Override
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir)
            throws MaxChangedBlocksException {
        System.out.println("! Hello from asyncCuboidClipboard paste");
        m_parrent.paste(editSession, newOrigin, noAir);
    }

    @Override
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir,
                      boolean entities)
            throws MaxChangedBlocksException {
        System.out.println("! Hello from asyncCuboidClipboard paste");
        m_parrent.paste(editSession, newOrigin, noAir, entities);
    }

    @Override
    public LocalEntity[] pasteEntities(Vector pos) {
        return m_parrent.pasteEntities(pos);
    }

    @Override
    public void place(EditSession editSession, Vector pos, boolean noAir)
            throws MaxChangedBlocksException {
        System.out.println("! Hello from asyncCuboidClipboard place");
        m_parrent.place(editSession, pos, noAir);
    }

    @Override
    public void rotate2D(int angle) {
        m_parrent.rotate2D(angle);
    }

    @Override
    public void saveSchematic(File path)
            throws IOException, DataException {
        m_parrent.saveSchematic(path);
    }

    @Override
    public void setBlock(Vector pt, BaseBlock block) {
        m_parrent.setBlock(pt, block);
    }

    @Override
    public void setOffset(Vector offset) {
        m_parrent.setOffset(offset);
    }

    @Override
    public void setOrigin(Vector origin) {
        m_parrent.setOrigin(origin);
    }

    @Override
    public void storeEntity(LocalEntity entity) {
        m_parrent.storeEntity(entity);
    }
}
