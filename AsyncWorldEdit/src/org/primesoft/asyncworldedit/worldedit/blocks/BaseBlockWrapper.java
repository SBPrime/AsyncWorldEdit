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
package org.primesoft.asyncworldedit.worldedit.blocks;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.blocks.BaseBlock;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.IAsyncWrapper;

/**
 *
 * @author SBPrime
 */
public class BaseBlockWrapper extends BaseBlock implements IAsyncWrapper {
    public static BaseBlockWrapper wrap(BaseBlock block, int jobId,
                                        boolean isAsync, PlayerEntry player) {
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

    private PlayerEntry m_player;

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

    public void setPlayer(PlayerEntry player) {
        m_player = player;
    }

    @Override
    public PlayerEntry getPlayer() {
        return m_player;
    }

    private BaseBlockWrapper(BaseBlock parent, int jobId, boolean isAsync,
                             PlayerEntry player) {
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
    public void setNbtData(CompoundTag ct) {
        m_parent.setNbtData(ct);
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
