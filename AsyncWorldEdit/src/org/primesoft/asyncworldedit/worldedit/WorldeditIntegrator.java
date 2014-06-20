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

import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.minecraft.util.commands.SimpleInjector;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.extension.platform.PlatformRejectionException;
import com.sk89q.worldedit.session.SessionManager;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class WorldeditIntegrator {

    /**
     * The original world edit
     */
    private WorldEdit m_worldEdit;

    /**
     * The parent plugin
     */
    private final AsyncWorldEditMain m_parent;

    /**
     * The world edit plugin
     */
    private final WorldEditPlugin m_worldEditPlugin;
    
    private EditSessionFactory m_oldEditSessionFactory;
    private SessionManager m_oldSessions;
    private PlatformManager m_platformManager;
    private CommandManager m_commandManager;
    private Platform[] m_oldPlatforms;
    private CommandsManager<LocalPlayer> m_oldCommands;

    /**
     * Create new instance of world edit integration checker and start it
     *
     * @param plugin
     * @param worldEditPlugin
     */
    public WorldeditIntegrator(AsyncWorldEditMain plugin, WorldEditPlugin worldEditPlugin) {
        m_parent = plugin;
        m_worldEditPlugin = worldEditPlugin;
        
        if (m_parent == null) {
            return;
        }
        
        if (worldEditPlugin == null) {
            AsyncWorldEditMain.log("Error initializeing Worldedit integrator");
            return;
        }
        
        m_worldEdit = worldEditPlugin.getWorldEdit();
        
        m_oldEditSessionFactory = m_worldEdit.getEditSessionFactory();
        m_oldSessions = m_worldEdit.getSessionManager();
        
        Reflection.set(m_worldEdit, "editSessionFactory", new AsyncEditSessionFactory(m_parent, m_worldEdit.getEventBus()),
                "Unable to inject edit session factory");
        Reflection.set(m_worldEdit, "sessions", new SessionManagerWrapper(m_worldEdit, m_parent.getPlayerManager()),
                "Unable to inject sessions");
        
        m_platformManager = m_worldEdit.getPlatformManager();
        m_commandManager = m_platformManager.getCommandManager();
        
        m_oldPlatforms = m_platformManager.getPlatforms().toArray(new Platform[0]);        
        for (Platform p : m_oldPlatforms) {
            m_platformManager.unregister(p);
        }
        
        m_oldCommands = Reflection.get(m_commandManager, CommandsManager.class, 
                "commands", "Unable to store old commands");
        Reflection.set(m_commandManager, "commands", getCommandWrapper(), 
                "Unable to inject new commands manager");
        
        for (Platform p : m_oldPlatforms) {
            try {
                m_platformManager.register(p);
            } catch (PlatformRejectionException ex) {
                Logger.getLogger(WorldeditIntegrator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Stop the wrapper
     */
    public void queueStop() {
        List<Platform> platforms = m_platformManager.getPlatforms();
        for (Platform platform : platforms) {
            m_platformManager.unregister(platform);
        }        
        
        Reflection.set(m_commandManager, "commands", m_oldCommands, 
                "Unable to restore commands manager");
        for (Platform platform : m_oldPlatforms) {
            try {
                m_platformManager.register(platform);
            } catch (PlatformRejectionException ex) {
                AsyncWorldEditMain.log("Error restoring platform " + platform.getPlatformName());
            }
        }
        
        Reflection.set(m_worldEdit, "editSessionFactory", m_oldEditSessionFactory, "Unable to restore edit session factory");
        Reflection.set(m_worldEdit, "sessions", m_oldSessions, "Unable to restore sessions");
    }

    private CommandsManager<LocalPlayer> getCommandWrapper() {
        try {
            Class<?> innerClass = Class.forName("com.sk89q.worldedit.extension.platform.CommandManager$CommandsManagerImpl");
            Constructor<?> ctor = innerClass.getDeclaredConstructor(CommandManager.class);
            
            ctor.setAccessible(true);
            
            CommandsManager<LocalPlayer> parent = (CommandsManager<LocalPlayer>)ctor.newInstance(m_commandManager);
            parent.setInjector(new SimpleInjector(m_worldEdit));
            
            ctor.setAccessible(false);
            CommandsWrapper result = new CommandsWrapper(m_worldEdit, m_worldEditPlugin, parent);
            result.setInjector(new SimpleInjector(m_worldEdit));
            return result;
        } catch (ClassNotFoundException ex) {
            AsyncWorldEditMain.log("Unable to create commands manager: unsupported WorldEdit version.");
        } catch (NoSuchMethodException ex) {
            AsyncWorldEditMain.log("Unable to create commands manager: unsupported WorldEdit version.");
        } catch (SecurityException ex) {
            AsyncWorldEditMain.log("Unable to create commands manager: security exception.");
        } catch (InstantiationException ex) {
            AsyncWorldEditMain.log("Unable to create commands manager: unsupported WorldEdit version.");
        } catch (IllegalAccessException ex) {
            AsyncWorldEditMain.log("Unable to create commands manager: security exception.");
        } catch (IllegalArgumentException ex) {
            AsyncWorldEditMain.log("Unable to create commands manager: unsupported WorldEdit version.");
        } catch (InvocationTargetException ex) {
            AsyncWorldEditMain.log("Unable to create commands manager: unsupported WorldEdit version.");
        }
        
        return null;
    }
}
