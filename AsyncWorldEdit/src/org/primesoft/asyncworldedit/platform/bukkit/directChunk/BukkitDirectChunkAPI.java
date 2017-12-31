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
package org.primesoft.asyncworldedit.platform.bukkit.directChunk;

import org.bukkit.Material;
import org.primesoft.asyncworldedit.api.inner.IBlocksHubIntegration;
import org.primesoft.asyncworldedit.api.taskdispatcher.ITaskDispatcher;
import org.primesoft.asyncworldedit.directChunk.base.BaseDirectChunkAPI;

/**
 *
 * @author SBPrime
 */
public abstract class BukkitDirectChunkAPI extends BaseDirectChunkAPI {

    /**
     * Air block ID
     */
    protected final static int AIR = 0;

    protected BukkitDirectChunkAPI(ITaskDispatcher taskDispatcher, IBlocksHubIntegration blocksHub) {
        super(taskDispatcher, blocksHub);
    }

    /**
     * Get light emission level for material
     *
     * @param material
     * @return
     */
    public byte getLightEmissionLevel(Material material) {
        if (material == null) {
            return 0;
        }

        switch (material) {
            case BEACON:
            case ENDER_PORTAL:
            case FIRE:
            case GLOWSTONE:
            case JACK_O_LANTERN:
            case LAVA:
            case STATIONARY_LAVA:
            case REDSTONE_LAMP_ON:
            case SEA_LANTERN:
                return 15;
            case END_ROD:
            case TORCH:
                return 14;
            case BURNING_FURNACE:
                return 13;
            case PORTAL:
                return 11;
            case GLOWING_REDSTONE_ORE:
                return 9;
            case ENDER_CHEST:
            case REDSTONE_TORCH_ON:
                return 7;
            case MAGMA:
                return 3;
            case BREWING_STAND:
            case BROWN_MUSHROOM:
            case DRAGON_EGG:
            case ENDER_PORTAL_FRAME:
                return 1;
        }

        return 0;
    }
    
    /**
     * Get the block opacity level (how much it obscures light)
     * @param material
     * @return 
     */
    public short getOpacityLevel(Material material) {
        if (material == null) {
            return 0;
        }

        if (getLightEmissionLevel(material) > 0) {
            return 1;
        }
        
        switch (material) {
            case WATER:
            case STATIONARY_WATER:
                return 3;
            case WEB:
                return 1;
            case SNOW:
                return 0;
            case ICE:
                return 3;
            case CARPET:
                return 0;
            case FROSTED_ICE:
                return 3;
            case AIR:
                return 0;
        }

        
        if (!material.isSolid()) {
            return 0;
        }

        return 255;
    }
    
    
    /**
     * Get the block opacity level (how much it obscures light)
     * @param material
     * @return 
     */
    public short getOpacityLevelSkyLight(Material material) {
        if (material == null) {
            return 0;
        }

        if (getLightEmissionLevel(material) > 0) {
            return 1;
        }
        
        switch (material) {
            case WATER:
            case STATIONARY_WATER:
            case ICE:
                return 3;
            case WEB:
            case LEAVES:
            case LEAVES_2:
                return 1;
            case SNOW:
            case CARPET:
            case FROSTED_ICE:
                return 255;
            case AIR:
            case GLASS:
                return 0;
        }

        
        if (!material.isSolid()) {
            return 0;
        }

        return 255;
    }

    @Override
    public byte getLightEmissionLevel(int type, int data) {
        return getLightEmissionLevel(Material.getMaterial(type));
    }

    @Override
    public short getOpacityLevel(int type, int data) {
        return getOpacityLevel(Material.getMaterial(type));
    }

    @Override
    public short getOpacityLevelSkyLight(int type, int data) {
        return getOpacityLevelSkyLight(Material.getMaterial(type));
    }
    
    
}