/*
 * The MIT License
 *
 * Copyright 2013 SBPrime.
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
package org.primesoft.asyncworldedit;

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
public class PhysicsWatch implements Listener {

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
