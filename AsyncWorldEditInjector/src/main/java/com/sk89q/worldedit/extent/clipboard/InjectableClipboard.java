/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * AsyncWorldEdit Injector a hack plugin that allows AsyncWorldEdit to integrate with
 * the WorldEdit plugin.
 *
 * Copyright (c) 2016, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 * Copyright (c) AsyncWorldEdit injector contributors
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
package com.sk89q.worldedit.extent.clipboard;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BaseBiome;
import java.util.List;
import org.primesoft.asyncworldedit.injector.core.InjectorCore;

/**
 *
 * @author SBPrime
 */
public abstract class InjectableClipboard implements Clipboard {
    /**
     * The injected clipboard
     */
    private final Clipboard m_injected;
    
    protected InjectableClipboard(Region region) {
        m_injected = InjectorCore.getInstance().getClassFactory().createClipboard(region);
    }
    
    
    @Override
    public Region getRegion() {
        return m_injected.getRegion();
    }

    @Override
    public Vector getDimensions() {
        return m_injected.getDimensions();
    }

    @Override
    public Vector getOrigin() {
        return m_injected.getOrigin();
    }

    @Override
    public void setOrigin(Vector origin) {
        m_injected.setOrigin(origin);
    }

    @Override
    public Vector getMinimumPoint() {
        return m_injected.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return m_injected.getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return m_injected.getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return m_injected.getEntities();
    }

    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return m_injected.createEntity(location, entity);
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return m_injected.getBlock(position);
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return m_injected.getLazyBlock(position);
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        return m_injected.getBiome(position);
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block) throws WorldEditException {
        return m_injected.setBlock(position, block);
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return m_injected.setBiome(position, biome);
    }

    @Override
    public Operation commit() {
        return m_injected.commit();
    }
    
}
