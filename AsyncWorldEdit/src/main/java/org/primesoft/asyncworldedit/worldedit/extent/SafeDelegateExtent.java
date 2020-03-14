/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.worldedit.extent;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.List;
import java.util.function.Function;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;
import org.primesoft.asyncworldedit.utils.SessionCanceled;

/**
 *
 * @author SBPrime
 */
public class SafeDelegateExtent extends AbstractDelegateExtent {    
    @FunctionalInterface
    private static interface FunctionEx<T extends Object, R extends Object, Ex extends Throwable> {
        public R apply(T t) throws Ex;
    }
    
    private final Extent m_falback;
    private final Extent m_target;

    public SafeDelegateExtent(Extent extent, Extent falbackExtent) {
        super(extent);

        m_falback = falbackExtent;
        m_target = extent;
    }
    
    private <T> T safeDelegate(Function<Extent, T> action) {
        if (m_target != null) {
            try {
                return action.apply(getExtent());
            } catch (Throwable ex) {
                if ((ex instanceof IllegalArgumentException) && (ex.getCause() instanceof SessionCanceled)) {
                    throw ex;
                }
                
                ExceptionHelper.printException(ex, "Unable to delegate extent action.");
                
                if (m_falback != null) {
                    return action.apply(m_falback);
                }
            }
        } else if (m_falback != null) {
            return action.apply(m_falback);
        }
        
        throw new NullPointerException();
    }
    
    private <T> T safeDelegateEx(FunctionEx<Extent, T, WorldEditException> action) throws WorldEditException {
        if (m_target != null) {
            try {
                return action.apply(getExtent());
            } catch (WorldEditException ex) {
                throw ex;
            } catch (Throwable ex) {
                if ((ex instanceof IllegalArgumentException) && (ex.getCause() instanceof SessionCanceled)) {
                    throw ex;
                }
                
                ExceptionHelper.printException(ex, "Unable to delegate extent action.");
                
                if (m_falback != null) {
                    return action.apply(m_falback);
                }
            }
        } else if (m_falback != null) {
            return action.apply(m_falback);
        }
        
        throw new NullPointerException();
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return safeDelegate(i -> i.createEntity(location, entity));
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        return safeDelegate(i -> i.getBiome(position));
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return safeDelegate(i -> i.getBlock(position));
    }

    @Override
    public List<? extends Entity> getEntities() {
        return safeDelegate(i -> i.getEntities());
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return safeDelegate(i -> i.getEntities(region));
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return safeDelegate(i -> i.getFullBlock(position));
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return safeDelegate(i -> i.getMaximumPoint());
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return safeDelegate(i -> i.getMinimumPoint());
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return safeDelegate(i -> i.setBiome(position, biome));
    }

    @Override
    public boolean setBlock(BlockVector3 location, BlockStateHolder block) throws WorldEditException {
        return safeDelegateEx(i -> i.setBlock(location, block));
    }
}
