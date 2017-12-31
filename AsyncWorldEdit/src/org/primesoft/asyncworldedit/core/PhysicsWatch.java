/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.core;

import org.primesoft.asyncworldedit.api.IPhysicsWatch;
import com.sk89q.worldedit.Vector;
import java.util.HashMap;

/**
 * This class is responsible for freezing all physics in edited regions
 *
 * @author SBPrime
 */
public abstract class PhysicsWatch implements IPhysicsWatch {

    /**
     * Is physics watch enabled
     */
    protected boolean m_isEnabled;

    /**
     * MTA mutex
     */
    private final Object m_mutex;

    /**
     * Locked blocks
     */
    private final HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>> m_locked;

    /**
     * Create new instanc of the class
     */
    public PhysicsWatch() {
        m_mutex = new Object();
        m_locked = new HashMap<String, HashMap<Integer, HashMap<Integer, HashMap<Integer, Integer>>>>();
    }

    /**
     * enable the physics freeze
     */
    @Override
    public void enable() {
        m_isEnabled = true;
    }

    /**
     * disable the physics freeze
     */
    @Override
    public void disable() {
        m_isEnabled = false;
        synchronized (m_mutex) {
            m_locked.clear();
        }
    }

    /**
     * Add new watched location
     *
     * @param name The world name
     * @param location The block location
     */
    @Override
    public void addLocation(String name, Vector location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        synchronized (m_mutex) {
            if (!m_isEnabled) {
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

    /**
     * Remove watched location
     *
     * @param name the world name
     * @param location the location
     */
    @Override
    public void removeLocation(String name, Vector location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        synchronized (m_mutex) {
            if (!m_isEnabled) {
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

            if (zhash.isEmpty()) {
                yhash.remove(y);
            }
            if (yhash.isEmpty()) {
                xhash.remove(x);
            }
            if (xhash.isEmpty()) {
                m_locked.remove(name);
            }
        }
    }
    
    /**
     * Perform test if block event shuld by canceled
     * @param name
     * @param x
     * @param y
     * @param z
     * @return 
     */
    protected boolean cancelEvent(String name, int x, int y, int z) {        
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
    
    
    /**
     * Register all physics events
     */
    public abstract void registerEvents();
}
