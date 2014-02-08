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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import org.primesoft.asyncworldedit.PluginMain;

/**
 *
 * @author SBPrime
 */
public class WorldeditIntegrator {

    /**
     * THe world edit plugin
     */
    private WorldEditProxy m_worldedit;

    /**
     * The parent plugin
     */
    private PluginMain m_parent;

    /**
     * The world edit plugin
     */
    private final WorldEditPlugin m_plugin;

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
            return;
        }

        WorldEdit we = worldEditPlugin.getWorldEdit();
        setLocalSessionFactory(we, new SpyHashMap(we.getConfiguration()));
        PluginMain.log("-----------------  Please ignore  --------------");
        m_worldedit = new WorldEditProxy(we);
        PluginMain.log("-----------------  Please ignore  --------------");
        m_worldedit.initialize(m_plugin, we, new AsyncEditSessionFactory(m_parent));

        setController(worldEditPlugin, m_worldedit);
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
     * Stop the wrapper
     */
    public void queueStop() {
        setController(m_plugin, new WorldEdit(m_worldedit.getServer(),
                m_worldedit.getConfiguration()));
        setLocalSessionFactory(m_plugin.getWorldEdit(), new HashMap<String, LocalSession>());
    }
}
