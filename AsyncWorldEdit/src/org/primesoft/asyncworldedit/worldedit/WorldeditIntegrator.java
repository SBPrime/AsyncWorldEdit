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
import com.sk89q.worldedit.extension.platform.CommandManager;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.session.SessionManager;
import com.sk89q.worldedit.util.command.Dispatcher;
import org.primesoft.asyncworldedit.AsyncWorldEditMain;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.utils.command.DispatcherWrapper;

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

    private EditSessionFactory m_oldEditSessionFactory;
    private SessionManager m_oldSessions;
    private PlatformManager m_platformManager;
    private CommandManager m_commandManager;
    private Dispatcher m_oldDispatcher;

    /**
     * Create new instance of world edit integration checker and start it
     *
     * @param plugin
     * @param worldEditPlugin
     */
    public WorldeditIntegrator(AsyncWorldEditMain plugin, WorldEditPlugin worldEditPlugin) {
        m_parent = plugin;
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

        m_oldDispatcher = Reflection.get(m_commandManager, Dispatcher.class,
                "dispatcher", "Unable to get the dispatcher");
        if (m_oldDispatcher != null) {
            Reflection.set(m_commandManager, "dispatcher", new DispatcherWrapper(m_oldDispatcher),
                "Unable to inject new commands manager");
        }
    }

    /**
     * Stop the wrapper
     */
    public void queueStop() {
        Reflection.set(m_commandManager, "dispatcher", m_oldDispatcher,
                "Unable to restore dispatcher");

        Reflection.set(m_worldEdit, "editSessionFactory", m_oldEditSessionFactory, "Unable to restore edit session factory");
        Reflection.set(m_worldEdit, "sessions", m_oldSessions, "Unable to restore sessions");
    }
}