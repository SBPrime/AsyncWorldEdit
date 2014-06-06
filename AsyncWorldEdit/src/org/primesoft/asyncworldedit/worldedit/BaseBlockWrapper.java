/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
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

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.world.DataException;
import java.util.UUID;

/**
 *
 * @author SBPrime
 */
public class BaseBlockWrapper extends BaseBlock implements IAsyncWrapper {
    public static BaseBlockWrapper wrap(BaseBlock block, int jobId,
                                        boolean isAsync, UUID player) {
        BaseBlockWrapper result;
        if (block instanceof BaseBlockWrapper) {
            result = (BaseBlockWrapper) block;
            result.setAsync(isAsync);
            result.setPlayer(player);
        } else {
            result = new BaseBlockWrapper(block, jobId, isAsync, player);
        }

        return result;
    }

    private final BaseBlock m_parent;

    private final int m_jobId;

    private boolean m_isAsync;

    private UUID m_player;

    @Override
    public int getJobId() {
        return m_jobId;
    }

    @Override
    public BaseBlock getParent() {
        return m_parent;
    }

    @Override
    public boolean isAsync() {
        return m_isAsync;
    }

    public void setAsync(boolean async) {
        m_isAsync = async;
    }

    public void setPlayer(UUID player) {
        m_player = player;
    }

    @Override
    public UUID getPlayer() {
        return m_player;
    }

    private BaseBlockWrapper(BaseBlock parent, int jobId, boolean isAsync,
                             UUID player) {
        super(0);

        m_jobId = jobId;
        m_parent = parent;
        m_isAsync = isAsync;
        m_player = player;
    }

    @Override
    public int cycleData(int increment) {
        return m_parent.cycleData(increment);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BaseBlockWrapper) {
            o = ((BaseBlockWrapper) o).getParent();
        }
        return m_parent.equals(o);
    }

    @Override
    public boolean equalsFuzzy(BaseBlock o) {
        if (o instanceof BaseBlockWrapper) {
            o = ((BaseBlockWrapper) o).getParent();
        }
        return m_parent.equalsFuzzy(o);
    }

    @Override
    public BaseBlock flip() {
        return m_parent.flip();
    }

    @Override
    public BaseBlock flip(CuboidClipboard.FlipDirection direction) {
        return m_parent.flip(direction);
    }

    @Override
    public int getData() {
        return m_parent.getData();
    }

    @Override
    public int getId() {
        return m_parent.getId();
    }

    @Override
    public CompoundTag getNbtData() {
        return m_parent.getNbtData();
    }

    @Override
    public String getNbtId() {
        return m_parent.getNbtId();
    }

    @Override
    public int getType() {
        return m_parent.getType();
    }

    @Override
    public boolean hasNbtData() {
        return m_parent.hasNbtData();
    }

    @Override
    public boolean hasWildcardData() {
        return m_parent.hasWildcardData();
    }

    @Override
    public int hashCode() {
        return m_parent.hashCode();
    }

    @Override
    public boolean inIterable(Iterable<BaseBlock> iter) {
        return m_parent.inIterable(iter);
    }

    @Override
    public boolean isAir() {
        return m_parent.isAir();
    }

    @Override
    public int rotate90() {
        return m_parent.rotate90();
    }

    @Override
    public int rotate90Reverse() {
        return m_parent.rotate90Reverse();
    }

    @Override
    public void setData(int data) {
        m_parent.setData(data);
    }

    @Override
    public void setId(int id) {
        m_parent.setId(id);
    }

    @Override
    public void setIdAndData(int id, int data) {
        m_parent.setIdAndData(id, data);
    }

    @Override
    public void setNbtData(CompoundTag nbtData)
            throws DataException {
        m_parent.setNbtData(nbtData);
    }

    @Override
    public void setType(int type) {
        m_parent.setType(type);
    }

    @Override
    public String toString() {
        return m_parent.toString();
    }
}
