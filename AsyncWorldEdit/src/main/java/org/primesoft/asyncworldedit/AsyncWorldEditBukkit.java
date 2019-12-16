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

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import static org.primesoft.asyncworldedit.LoggerProvider.log;
import org.primesoft.asyncworldedit.api.inner.IAsyncWorldEditCore;
import org.primesoft.asyncworldedit.api.inner.ILogger;
import org.primesoft.asyncworldedit.injector.core.IInjectorPlatform;
import org.primesoft.asyncworldedit.utils.ClassLoaderHelper;
import org.primesoft.asyncworldedit.utils.Reflection;

/**
 *
 * @author SBPrime
 */
public class AsyncWorldEditBukkit extends AsyncWorldEditMain {

    static {
        s_log = Logger.getLogger("Minecraft.AWE");

        ClassLoader classLoaderPlugin = ClassLoaderHelper.getPluginClassLoader(AsyncWorldEditBukkit.class);
        if (classLoaderPlugin == null) {
            throw new RuntimeException("Unable to initialize. Unable to find PluginClassLoader for AsyncWorldEditBukkit.");
        }
                
        try {
            ClassLoaderHelper.addLoader(classLoaderPlugin);
            
            initializeLogger();
            detectFawe();
            inject();
            
            
        } finally {
            ClassLoaderHelper.removeLoader(classLoaderPlugin);
        }
    }
    
    private static final String FAWE = "com.boydti.fawe.";

    private static final String CLS_CORE = "org.primesoft.asyncworldedit.platform.bukkit.core.BukkitAsyncWorldEditCore";
    private static final String CLS_INJECTOR = "org.primesoft.asyncworldedit.injector.InjectorBukkit";

    private static final Logger s_log;

    private static void detectFawe() {
        PluginManager pm = Bukkit.getPluginManager();
        Class<?> clsPluginManager = pm.getClass();
        Field[] fields = clsPluginManager.getDeclaredFields();
        Field fPlugins = null;
        Field fLookupNames = null;

        Object faweClass = null;

        boolean canContinue = true;

        for (Field f : fields) {
            Object o = Reflection.get(pm, f, "Unable to get field " + f.getName());

            if (o == null) {
                continue;
            }

            Class<?> cls = o.getClass();
            if (cls == null) {
                continue;
            }

            if (cls.getName().startsWith(FAWE)) {
                String name = f.getName();
                if ("plugins".equals(name)) {
                    fPlugins = f;
                    faweClass = o;
                } else if ("lookupNames".equals(name)) {
                    fLookupNames = f;
                    faweClass = o;
                } else {
                    canContinue = false;
                }
            }
        }

        if (faweClass == null) {
            return;
        }

        s_log.log(Level.SEVERE, String.format("%s ==========================================", LoggerProvider.PREFIX));
        s_log.log(Level.SEVERE, String.format("%s = fawe detected, trying to disable...    =", LoggerProvider.PREFIX));

        try {
            boolean jarDeleted = false;
            CodeSource src = faweClass.getClass().getProtectionDomain().getCodeSource();
            if (src != null) {
                URL jar = src.getLocation();
                String fileName = jar != null ? jar.getFile() : null;

                if (fileName != null) {
                    try {
                        File jarFile = new File(fileName);
                        jarDeleted = jarFile.renameTo(new File(fileName + ".disabled"));
                    } catch (Exception ex) {

                    }
                }
            }

            if (!jarDeleted) {
                s_log.log(Level.SEVERE, String.format("%s = Unable to remove fawe jar.             =", LoggerProvider.PREFIX));
            }

            if (canContinue) {
                List<Plugin> plugins = (List<Plugin>) Reflection.get(pm, List.class, fPlugins, "Getting plugins");
                Map<String, Plugin> lookupNames = (Map<String, Plugin>) Reflection.get(pm, Map.class, fLookupNames, "Getting lookupNames");

                if (plugins != null && lookupNames != null) {
                    List<Plugin> newPlugins = new ArrayList<>(plugins);
                    Map<String, Plugin> newLookupNames = new ConcurrentHashMap<>(lookupNames);

                    canContinue = Reflection.set(pm, fPlugins, newPlugins, "Set plugins")
                            && Reflection.set(pm, fLookupNames, newLookupNames, "Set lookupNames");

                    Optional<Plugin> fawePlugin = plugins.stream()
                            .filter(i -> i.getDescription().getMain().startsWith(FAWE))
                            .findAny();

                    if (fawePlugin.isPresent()) {
                        newPlugins.remove(fawePlugin.get());
                        newLookupNames.remove(fawePlugin.get().getName());
                    }
                } else {
                    canContinue = false;
                }
            }

            if (!canContinue) {
                s_log.log(Level.SEVERE, String.format("%s = Pleas make up your mind.               =", LoggerProvider.PREFIX));
                s_log.log(Level.SEVERE, String.format("%s = Choose: one or the other               =", LoggerProvider.PREFIX));

                try {
                    Thread.sleep(20000);
                } catch (InterruptedException ex) {
                }
            }

        } finally {
            s_log.log(Level.SEVERE, String.format("%s ==========================================", LoggerProvider.PREFIX));
        }
    }

    private static void inject() {
        PluginManager pm = Bukkit.getPluginManager();
        if (pm.isPluginEnabled("WorldEdit")) {
            log("WARNING: WorldEdit plugin detected running. Trying to disable. Plugins that might stop working: " +
                    Stream.of(pm.getPlugins()).map(i -> i.getName())
                            .filter(i -> !"WorldEdit".equals(i) && !"AsyncWorldEdit".equals(i))
                            .collect(Collectors.joining(", "))
            );            
            pm.disablePlugin(WorldEditPlugin.getPlugin(WorldEditPlugin.class));            
        }
        
        LoaderBukkit loader = new LoaderBukkit(AsyncWorldEditBukkit.class);

        if (!loader.checkDependencies()) {
            log("ERROR: Missing plugin dependencies. Plugin disabled.");
            return;
        }

        IInjectorPlatform injector = createInjector(loader);
        if (injector == null) {
            log("ERROR: Injector not found.");
            return;
        }

        if (!injector.onEnable()) {
            log("ERROR: Unable to enable the injector.");
            return;
        }

        s_api = createCore(loader);
        s_api.initializeBridge();
        
        s_loader = loader;
    }

    private static void initializeLogger() {
        final Server server = Bukkit.getServer();
        final ConsoleCommandSender console = server.getConsoleSender();

        ILogger log = new ILogger() {
            @Override
            public void log(String msg) {
                if (s_log == null || msg == null) {
                    return;
                }

                s_log.log(Level.INFO, LoggerProvider.PREFIX + " "+ msg);
            }

            @Override
            public void sayConsole(String msg) {
                console.sendRawMessage(LoggerProvider.PREFIX + " "+ msg);
            }
        };

        LoggerProvider.setLogger(log);
    }

    private static LoaderBukkit s_loader;
    
    private Loader m_loader;

    @Override
    public void onLoad() {
        super.onLoad();

        final LoaderBukkit loader = s_loader;
        s_loader = null;

        if (loader != null) {
            loader.init(this);
            
            if (!loader.install()) {
                log("ERROR: Unable to install the plugin.");
                return;
            }
        } else {
            log("ERROR: Unable to install the plugin.");
            return;
        }

        m_loader = loader;
    }

    @Override
    public void onEnable() {
        super.onEnable();

        if (m_loader == null) {
            return;
        }        

        if (s_api != null) {
            s_api.initializePlatform(this);
            s_api.initialize();
            s_api.onEnable();

            getServer().getScheduler().runTaskLater(this, () -> m_loader.loadPlugins(s_api), 0);
        }

        log("Enabled");
    }

    private static IAsyncWorldEditCore createCore(final Loader loader) {
        Constructor<?> ctor;
        try {
            Class<?> cls = loader.loadClass(CLS_CORE);
            ctor = Reflection.findConstructor(cls, "Unable to find core constructor");
        } catch (ClassNotFoundException ex) {
            log("ERROR: Unable to create AWE core, plugin disabled");

            return null;
        }

        return Reflection.create(IAsyncWorldEditCore.class, ctor, "Unable to create AWE Core");
    }

    private static IInjectorPlatform createInjector(final Loader loader) {
        Constructor<?> ctor;
        try {
            Class<?> clsAweCore = loader.loadClass(CLS_INJECTOR);
            ctor = Reflection.findConstructor(clsAweCore, "Unable to find ijector constructor");
        } catch (ClassNotFoundException ex) {
            log("ERROR: Unable to create AWE Injector, plugin disabled");

            return null;
        }

        return Reflection.create(IInjectorPlatform.class, ctor, "Unable to create AWE Injector");
    }

    @Override
    public void onDisable() {
        if (s_api != null) {
            m_loader.unloadPlugins();
            s_api.onDisable();
        }
        log("Disable");
    }
}
