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
package org.primesoft.asyncworldedit.worldedit.entity;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.entity.BaseEntity;
import org.primesoft.asyncworldedit.playerManager.PlayerEntry;
import org.primesoft.asyncworldedit.worldedit.IAsyncWrapper;

/**
 *
 * @author SBPrime
 */
public class BaseEntityWrapper extends BaseEntity implements IAsyncWrapper {
    public static BaseEntityWrapper wrap(BaseEntity entity, int jobId,
                                        boolean isAsync, PlayerEntry player) {
        BaseEntityWrapper result;
        if (entity instanceof BaseEntityWrapper) {
            result = (BaseEntityWrapper) entity;
            result.setAsync(isAsync);
            result.setPlayer(player);
        } else {
            result = new BaseEntityWrapper(entity, jobId, isAsync, player);
        }

        return result;
    }
    
    private final BaseEntity m_parent;

    private final int m_jobId;

    private boolean m_isAsync;

    private PlayerEntry m_player;
    
    @Override
    public int getJobId() {
        return m_jobId;
    }

    @Override
    public BaseEntity getParent() {
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
    
    private BaseEntityWrapper(BaseEntity parent, int jobId, boolean isAsync,
                             PlayerEntry player) {
        super("");

        m_jobId = jobId;
        m_parent = parent;
        m_isAsync = isAsync;
        m_player = player;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof BaseEntityWrapper) {
            o = ((BaseEntityWrapper) o).getParent();
        }
        
        if (m_parent == null) {
            return false;
        }
        return m_parent.equals(o);
    }

    @Override
    public CompoundTag getNbtData() {
        if (m_parent == null) {
            return null;
        }
        return m_parent.getNbtData(); 
    }

    @Override
    public String getTypeId() {
        if (m_parent == null) {
            return null;
        }
        return m_parent.getTypeId(); 
    }

    @Override
    public boolean hasNbtData() {
        if (m_parent == null) {
            return false;
        }
        return m_parent.hasNbtData(); 
    }

    @Override
    public void setNbtData(CompoundTag ct) {
        if (m_parent == null) {
            return;
        }
        m_parent.setNbtData(ct); 
    }

    @Override
    public void setTypeId(String id) {
        if (m_parent == null) {
            return;
        }
        m_parent.setTypeId(id); 
    }
}
