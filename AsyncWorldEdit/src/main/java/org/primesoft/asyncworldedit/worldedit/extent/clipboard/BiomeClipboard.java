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
package org.primesoft.asyncworldedit.worldedit.extent.clipboard;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import java.util.List;

/**
 *
 * @author SBPrime
 */
public final class BiomeClipboard implements Clipboard {

    private final BiomeType[][] m_biomes;
    private final Region m_region;
    private final int m_minY;
    private final int m_minX;
    private final int m_minZ;
    private final Clipboard m_parent;

    public BiomeClipboard(Clipboard parent, Region region) {
        m_parent = parent;

        BlockVector3 dimensions = getDimensions();
        m_biomes = new BiomeType[dimensions.getBlockX()][dimensions.getBlockZ()];

        m_region = region.clone();
        BlockVector3 v = m_region.getMinimumPoint();
        m_minX = v.getBlockX();
        m_minY = v.getBlockY();
        m_minZ = v.getBlockZ();
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        BiomeType result = null;
        if (m_region.contains(position.toBlockVector3(m_minY))) {
            result = m_biomes[position.getBlockX() - m_minX][position.getBlockZ() - m_minZ];
        }

        if (result == null) {
            return BiomeTypes.PLAINS;
        }

        return result;
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        if (!m_region.contains(position.toBlockVector3(m_minY))) {
            return false;
        }
        
        m_biomes[position.getBlockX() - m_minX][position.getBlockZ() - m_minZ] = biome;
        return true;
    }

    @Override
    public Region getRegion() {
        return m_parent.getRegion();
    }

    @Override
    public BlockVector3 getDimensions() {
        return m_parent.getDimensions();
    }

    @Override
    public BlockVector3 getOrigin() {
        return m_parent.getOrigin();
    }

    @Override
    public void setOrigin(BlockVector3 origin) {
        m_parent.setOrigin(origin);
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return m_parent.getMinimumPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return m_parent.getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return m_parent.getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return m_parent.getEntities();
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return m_parent.createEntity(location, entity);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return m_parent.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return m_parent.getFullBlock(position);
    }

    @Override
    public boolean setBlock(BlockVector3 position, BlockStateHolder block) throws WorldEditException {
        return m_parent.setBlock(position, block);
    }

    @Override
    public Operation commit() {
        return m_parent.commit();
    }
}
