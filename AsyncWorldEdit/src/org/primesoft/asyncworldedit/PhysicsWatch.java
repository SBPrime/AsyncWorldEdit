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
package org.primesoft.asyncworldedit;

import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import com.sk89q.worldedit.Vector;
import java.util.HashMap;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;

/**
 * This class is responsible for freezing all physics in edited regions
 *
 * @author SBPrime
 */
public class PhysicsWatch implements Listener, IPhysicsWatch {

    /**
     * Is physics watch enabled
     */
    private boolean m_isEnabled;
    /**
     * MTA mutex
     */
    private final Object m_mutex;
    
    
    /**
     * Locked blocks
     */
    private final HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> m_locked;

    public PhysicsWatch() {
        m_mutex = new Object();
        m_locked = new HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>();
    }
    
    public void Enable()
    {
        m_isEnabled = true;
    }
    
    
    public void Disable()
    {
        m_isEnabled = false;
        synchronized (m_mutex)
        {
            m_locked.clear();
        }
    }
    

    @Override
    public void addLocation(String name, Vector location) {        
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        synchronized (m_mutex) {
            if (!m_isEnabled)
            {
                return;
            }
            
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> xhash;
            if (!m_locked.containsKey(name)) {
                xhash = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>();
                m_locked.put(name, xhash);
            } else {
                xhash = m_locked.get(name);
            }

            HashMap<Integer, HashMap<Integer, Integer>> yhash;
            if (!xhash.containsKey(x)) {
                yhash = new HashMap<Integer, HashMap<Integer, Integer>>();
                xhash.put(x, yhash);
            } else {
                yhash = xhash.get(x);
            }

            HashMap<Integer, Integer> zhash;
            if (!yhash.containsKey(y)) {
                zhash = new HashMap<Integer, Integer>();
                yhash.put(y, zhash);
            } else {
                zhash = yhash.get(y);
            }

            if (!zhash.containsKey(z)) {
                zhash.put(z, 1);
            } else {
                zhash.put(z, zhash.get(z) + 1);
            }
        }
    }

    @Override
    public void removeLocation(String name, Vector location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

               
        synchronized (m_mutex) {
            if (!m_isEnabled)
            {
                return;
            }
            
            if (!m_locked.containsKey(name)) {
                return;
            }
            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> xhash = m_locked.get(name);

            if (!xhash.containsKey(x)) {
                return;
            }
            HashMap<Integer, HashMap<Integer, Integer>> yhash = xhash.get(x);


            if (!yhash.containsKey(y)) {
                return;
            }
            HashMap<Integer, Integer> zhash = yhash.get(y);

            Integer val = zhash.get(z);
            if (val == null) {
                return;
            }

            val = val - 1;
            zhash.remove(z);
            if (val != 0) {
                zhash.put(z, val);
            }
        }
    }

    /**
     * Perform test if block event shuld by canceled
     */
    private boolean shuldCancel(Block block) {
        Location location = block.getLocation();
        String name = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        final int delta = 1;
                
        synchronized (m_mutex) {
            if (!m_locked.containsKey(name)) {
                return false;
            }

            HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>> xhash = m_locked.get(name);
            for (int px = x - delta; px <= x + delta; px++) {
                if (xhash.containsKey(px)) {
                    HashMap<Integer, HashMap<Integer, Integer>> yhash = xhash.get(px);

                    for (int py = y - delta; py <= y + delta; py++) {
                        if (yhash.containsKey(py)) {
                            HashMap<Integer, Integer> zhash = yhash.get(py);

                            for (int pz = z - delta; pz <= z + delta; pz++) {
                                if (zhash.containsKey(pz)) {
                                    return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @EventHandler
    public void onBlockPhysicsEvent(BlockPhysicsEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onLeavesDecayEvent(LeavesDecayEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockFormEvent(BlockFormEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockBurnEvent(BlockBurnEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockDispenseEvent(BlockDispenseEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockFadeEvent(BlockFadeEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockGrowEvent(BlockGrowEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }


    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onEntityBlockFormEvent(EntityBlockFormEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }

    @EventHandler
    public void onBlockSpreadEvent(BlockSpreadEvent event) {
        if (event.isCancelled() || !m_isEnabled) {
            return;
        }
        event.setCancelled(shuldCancel(event.getBlock()));
    }
}
