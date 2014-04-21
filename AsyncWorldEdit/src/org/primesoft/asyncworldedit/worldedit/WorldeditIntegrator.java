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

import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.PluginMain;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class WorldeditIntegrator {
    /**
     * THe world edit proxy
     */
    private WorldEditProxy m_worldeditProxy;

    /**
     * The original world edit
     */
    private WorldEdit m_worldEdit;

    /**
     * The parent plugin
     */
    private final PluginMain m_parent;

    /**
     * The world edit plugin
     */
    private final WorldEditPlugin m_worldEditPlugin;

    /**
     * Create new instance of world edit integration checker and start it
     *
     * @param plugin
     * @param worldEditPlugin
     */
    public WorldeditIntegrator(PluginMain plugin, WorldEditPlugin worldEditPlugin) {
        m_parent = plugin;
        m_worldEditPlugin = worldEditPlugin;

        if (m_parent == null) {
            return;
        }

        if (worldEditPlugin == null) {
            PluginMain.log("Error initializeing Worldedit integrator");
            return;
        }

        m_worldEdit = worldEditPlugin.getWorldEdit();
        
        Reflection.set(m_worldEdit, "sessions", 
                new SpyHashMap(m_worldEdit.getConfiguration(), m_parent.getPlayerManager()), 
                "Unable to inject LocalSession factory");
        
        m_worldeditProxy = createWorldEditProxy();
        m_worldeditProxy.initialize(m_worldEditPlugin, m_worldEdit, 
                new AsyncEditSessionFactory(m_parent));
        
        
        Reflection.set(worldEditPlugin, "controller", 
                m_worldeditProxy,
                "Unable to inject WorldEdit proxy");
        Reflection.set(WorldEdit.class, "instance", 
                m_worldeditProxy, "Unable to inject WorldEdit proxy instance");
    }

    
    /**
     * Create the world edit proxy class
     * @return 
     */
    private WorldEditProxy createWorldEditProxy() {
        WorldEditProxy result;

        Logger logger = Reflection.get(SimpleInjector.class, Logger.class,
                "logger", "Unable to get logger");   
        PrintStream oldErr = System.err;
        
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
        if (logger == null) {
            PluginMain.log("vvvvvvvvvvvvvvvvv  Please ignore  vvvvvvvvvvvvvvvvv");
        } else {            
            logger.setLevel(Level.OFF);            
        }
        result = new WorldEditProxy(m_worldEdit);
        if (logger == null) {
            PluginMain.log("^^^^^^^^^^^^^^^^^  Please ignore  ^^^^^^^^^^^^^^^^^");
        } else {
            logger.setLevel(Level.INFO);
        }
        System.setErr(oldErr);

        return result;
    }
    
    /**
     * Stop the wrapper
     */
    public void queueStop() {
        Reflection.set(m_worldEditPlugin, "controller", 
                m_worldEdit,
                "Unable to restore WorldEdit");
        Reflection.set(WorldEdit.class, "instance", 
                m_worldEdit, "Unable to restore WorldEdit instance");
        
        Reflection.set(m_worldEdit, "sessions", 
                new HashMap<String, LocalSession>(), 
                "Unable to restore LocalSession factory");   
    }
}
