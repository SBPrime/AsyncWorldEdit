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
package org.primesoft.asyncworldedit;

import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.ILogger;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class AsyncWorldEditBukkit extends AsyncWorldEditMain implements ILogger {
    private static final String CORE = "org.primesoft.asyncworldedit.platform.bukkit.core.BukkitAsyncWorldEditCore";

    private static final Logger s_log = Logger.getLogger("Minecraft.AWE");

    private ConsoleCommandSender m_console;

    private Loader m_loader;

    /**
     * Send message to the log
     *
     * @param msg
     */
    @Override
    public void log(String msg) {
        if (s_log == null || msg == null) {
            return;
        }

        s_log.log(Level.INFO, String.format("%s %s", LoggerProvider.PREFIX, msg));
    }

    /**
     * Send message to the console
     *
     * @param msg
     */
    @Override
    public void sayConsole(String msg) {
        m_console.sendRawMessage(msg);
    }

    @Override
    public void onEnable() {
        super.onEnable();

        LoggerProvider.setLogger(this);

        final Server server = getServer();

        m_console = server.getConsoleSender();

        final Loader loader = new LoaderBukkit(this);

        if (!loader.install()) {
            log("ERROR: Unable to install the plugin.");
            return;
        }

        if (!loader.checkDependencies()) {
            log("ERROR: Missing plugin dependencies. Plugin disabled.");
            return;
        }

        Constructor<?> ctorCore = null;
        try {
            Class<?> clsAweCore = loader.loadClass(CORE);
            ctorCore = Reflection.findConstructor(clsAweCore, "Unable to find core constructor", Plugin.class);
        } catch (ClassNotFoundException ex) {
            log("ERROR: Unable to create AWE core, plugin disabled");
        }

        m_api = ctorCore != null ? Reflection.create(IAsyncWorldEditCore.class, ctorCore, "Unable to create AWE Core", this) : null;

        if (m_api != null) {
            m_api.initialize();
            m_api.onEnable();
        }

        server.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                loader.loadPlugins(m_api);
            }
        }, 0);

        m_loader = loader;

        log("Enabled");
    }

    @Override
    public void onDisable() {
        if (m_api != null) {
            m_loader.unloadPlugins();
            m_api.onDisable();
        }
        log("Disable");
    }
}
