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

package org.primesoft.asyncworldedit.worldedit.world.biome;

import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.UUID;
import org.primesoft.asyncworldedit.worldedit.IAsyncWrapper;

/**
 *
 * @author SBPrime
 */
public class BaseBiomeWrapper extends BaseBiome implements IAsyncWrapper
{
    public static BaseBiomeWrapper wrap(BaseBiome biome, int jobId,
                                        boolean isAsync, UUID player) {
        BaseBiomeWrapper result;
        if (biome instanceof BaseBiomeWrapper) {
            result = (BaseBiomeWrapper) biome;
            result.setAsync(isAsync);
            result.setPlayer(player);
        } else {
            result = new BaseBiomeWrapper(biome, jobId, isAsync, player);
        }

        return result;
    }
    
    private final BaseBiome m_parent;

    private final int m_jobId;

    private boolean m_isAsync;

    private UUID m_player;
    
    @Override
    public int getJobId() {
        return m_jobId;
    }

    @Override
    public BaseBiome getParent() {
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
    
    private BaseBiomeWrapper(BaseBiome parent, int jobId, boolean isAsync,
                             UUID player) {
        super(0);

        m_jobId = jobId;
        m_parent = parent;
        m_isAsync = isAsync;
        m_player = player;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof BaseBiomeWrapper) {
            o = ((BaseBiomeWrapper) o).getParent();
        }
        return m_parent.equals(o);
    }

    @Override
    public int getId() {
        return m_parent.getId();
    }

    @Override
    public void setId(int id) {
        m_parent.setId(id);
    }        
}
