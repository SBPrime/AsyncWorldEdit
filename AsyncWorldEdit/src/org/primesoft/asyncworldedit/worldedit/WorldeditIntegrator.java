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
package org.primesoft.asyncworldedit.worldedit;

import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.primesoft.asyncworldedit.BlockPlacer;
import org.primesoft.asyncworldedit.ConfigProvider;
import org.primesoft.asyncworldedit.PluginMain;

/**
 *
 * @author SBPrime
 */
public class WorldeditIntegrator implements Runnable {
    /**
     * How often check if world edit integration is successfull
     */
    private final static int CHECK_INTERVAL = 40;
    /**
     * Bukkit scheduler
     */
    private BukkitScheduler m_scheduler;
    /**
     * THe world edit plugin
     */
    private WorldEdit m_worldedit;
    /**
     * Should block places shut down
     */
    private boolean m_shutdown;
    /**
     * Current scheduler task
     */
    private BukkitTask m_task;
    
    /**
     * The parent plugin
     */
    private PluginMain m_parent;

    
    /**
     * Create new instance of world edit integration checker and start it
     *
     * @param plugin
     * @param wePlugin
     */
    public WorldeditIntegrator(PluginMain plugin, WorldEdit worldEdit) {
        m_worldedit = worldEdit;
        m_parent = plugin;
        m_scheduler = plugin.getServer().getScheduler();        
        
        if (m_parent == null)
        {
            m_shutdown = true;
            return;
        }
        if (m_worldedit == null || m_scheduler == null)
        {
            m_shutdown = true;
            PluginMain.Log("Error initializeing Worldedit integrator");
            return;
        }
        
        m_task = m_scheduler.runTaskTimer(plugin, this,
                CHECK_INTERVAL, CHECK_INTERVAL);
    }

    @Override
    public void run() {
        synchronized (this) {
            if (m_shutdown) {
                stop();
                return;
            }
            
            EditSessionFactory factory = m_worldedit.getEditSessionFactory();
            if (!(factory instanceof AsyncEditSessionFactory))
            {
                PluginMain.Log("World edit session not set to AsyncWorldedit. Fixing.");
                
                m_worldedit.setEditSessionFactory(new AsyncEditSessionFactory(m_parent));
            }
        }
    }

    /**
     * Queue stop command
     */
    public void queueStop() {
        m_shutdown = true;
    }

    /**
     * Stop the integrator
     */
    public void stop() {
        if (m_task != null)
        {
            m_task.cancel();
            m_task = null;
        }
    }
}
