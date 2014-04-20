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
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
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
     * Should block places shut down
     */
    private boolean m_shutdown;

    /**
     * Current scheduler task
     */
    private BukkitTask m_task;

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
    private PluginMain m_parent;

    /**
     * The world edit plugin
     */
    private final WorldEditPlugin m_plugin;

    /**
     * Current world edit session factory
     */
    private AsyncEditSessionFactory m_factory;

    /**
     * Create new instance of world edit integration checker and start it
     *
     * @param plugin
     * @param worldEditPlugin
     */
    public WorldeditIntegrator(PluginMain plugin, WorldEditPlugin worldEditPlugin) {
        m_parent = plugin;
        m_plugin = worldEditPlugin;

        if (m_parent == null) {
            m_shutdown = true;
            return;
        }

        m_scheduler = plugin.getServer().getScheduler();
        if (worldEditPlugin == null || m_scheduler == null) {
            m_shutdown = true;
            PluginMain.log("Error initializeing Worldedit integrator");
            return;
        }

        m_factory = new AsyncEditSessionFactory(m_parent);

        m_task = m_scheduler.runTaskTimer(plugin, this,
                CHECK_INTERVAL, CHECK_INTERVAL);

        m_worldEdit = worldEditPlugin.getWorldEdit();
        setLocalSessionFactory(m_worldEdit, new SpyHashMap(m_worldEdit.getConfiguration(), m_parent.getPlayerManager()));
        m_worldeditProxy = createWorldEditProxy();

        m_worldeditProxy.initialize(m_plugin, m_worldEdit, m_factory);
        setController(worldEditPlugin, m_worldeditProxy);
    }

    private WorldEditProxy createWorldEditProxy() {
        WorldEditProxy result;

        Logger logger = getLogger();       
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

    private Logger getLogger() {
        try {
            Field field = SimpleInjector.class.getDeclaredField("logger");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);

            Object o = field.get(null);
            if (o instanceof Logger) {
                return (Logger) o;
            }
            return null;
        } catch (IllegalArgumentException ex) {
            PluginMain.log("Unable to set logger: unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            PluginMain.log("Unable to set logger: security exception.");
        } catch (NoSuchFieldException ex) {
            PluginMain.log("Unable to set logger: unsupported WorldEdit version.");
        } catch (SecurityException ex) {
            PluginMain.log("Unable to set logger: security exception.");
        }

        return null;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (m_shutdown) {
                stop();
                return;
            }

            EditSessionFactory factory = m_worldEdit.getEditSessionFactory();
            if (!(factory instanceof AsyncEditSessionFactory)) {
                PluginMain.log("World edit session not set to AsyncWorldedit. Fixing.");
                m_worldEdit.setEditSessionFactory(m_factory);
            }
        }
    }

    /**
     * Inject a LocalSession wrapper factory using reflection
     */
    private void setLocalSessionFactory(WorldEdit worldedit,
            final HashMap<String, LocalSession> sessionHash) {
        try {
            Field field = worldedit.getClass().getDeclaredField("sessions");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(worldedit, sessionHash);
        } catch (IllegalArgumentException ex) {
            PluginMain.log("Unable to inject LocalSession factory: unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            PluginMain.log("Unable to inject LocalSession factory: security exception.");
        } catch (NoSuchFieldException ex) {
            PluginMain.log("Unable to inject LocalSession factory: unsupported WorldEdit version.");
        } catch (SecurityException ex) {
            PluginMain.log("Unable to inject LocalSession factory: security exception.");
        }
    }

    /**
     * Try to wrap the world edit
     *
     * @param worldEditPlugin
     */
    private void setController(WorldEditPlugin worldEditPlugin, WorldEdit worldEdit) {
        try {
            Field field = worldEditPlugin.getClass().getDeclaredField("controller");
            field.setAccessible(true);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.set(worldEditPlugin, worldEdit);
        } catch (IllegalArgumentException ex) {
            PluginMain.log("Unable to inject WorldEdit wrapper factory: unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            PluginMain.log("Unable to inject WorldEdit wrapper: security exception.");
        } catch (NoSuchFieldException ex) {
            PluginMain.log("Unable to inject WorldEdit wrapper: unsupported WorldEdit version.");
        } catch (SecurityException ex) {
            PluginMain.log("Unable to inject WorldEdit wrapper: security exception.");
        }
    }

    /**
     * Stop the integrator
     */
    public void stop() {
        if (m_task != null) {
            m_task.cancel();
            m_task = null;
        }
    }

    /**
     * Stop the wrapper
     */
    public void queueStop() {
        m_shutdown = true;

        m_worldEdit.setEditSessionFactory(new EditSessionFactory());
        setController(m_plugin, m_worldEdit);
        setLocalSessionFactory(m_plugin.getWorldEdit(), new HashMap<String, LocalSession>());
    }
}
