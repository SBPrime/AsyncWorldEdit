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
package org.primesoft.asyncworldedit.platform.bukkit;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.plugin.Plugin;
import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import org.primesoft.asyncworldedit.core.PhysicsWatch;

/**
 *
 * @author prime
 */
class BykkitPhysicsWatch extends PhysicsWatch implements IPhysicsWatch, Listener {
    private final Plugin m_plugin;

    public BykkitPhysicsWatch(Plugin plugin) {
        m_plugin = plugin;
    }

    @Override
    public void registerEvents() {
        m_plugin.getServer().getPluginManager().registerEvents(this, m_plugin);
    }
    
    
    @EventHandler
    public void odEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onLeavesDecayEvent(LeavesDecayEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockBurnEvent(BlockBurnEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockDispenseEvent(BlockDispenseEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockFadeEvent(BlockFadeEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockGrowEvent(BlockGrowEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onEntityBlockFormEvent(EntityBlockFormEvent event) {
        processEvent(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockSpreadEvent(BlockSpreadEvent event) {
        processEvent(event.getBlock(), event);
    }
    
    /**
     * Perform test if block event shuld by canceled
     */
    private void processEvent(Block block, Cancellable event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        
        Location location = block.getLocation();
        String name = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        
        if (cancelEvent(name, x, y, z)) {
            event.setCancelled(true);
        }
    }
}
