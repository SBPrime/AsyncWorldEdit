/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2018, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.injector;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;

/**
 *
 * @author SBPrime
 */
final class ClassInjectorBukkit implements IClassInjector {

    private final static String PLUGIN_CLASS_LOADER = "org.bukkit.plugin.java.PluginClassLoader";

    private final ClassLoader m_rootClassLoader;
    private final Method m_miDefineClass;
    private final Plugin m_worldEdit;

    private Map<String, Class<?>> m_classes;

    public ClassInjectorBukkit() {
        m_worldEdit = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
        ClassLoader worldEditClassLoader = com.sk89q.worldedit.WorldEditException.class.getClassLoader();
        Class<?> clsPluginClassLoader;

        try {
            clsPluginClassLoader = worldEditClassLoader.loadClass(PLUGIN_CLASS_LOADER);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Unable to initialize 'ClassInjector'. Unable to get the PluginClassLoader.", ex);
        }

        while (worldEditClassLoader != null && !clsPluginClassLoader.isInstance(worldEditClassLoader)) {
            worldEditClassLoader = worldEditClassLoader.getParent();
        }

        if (worldEditClassLoader == null) {
            throw new IllegalStateException("Unable to initialize 'ClassInjector'. Matching class loader not found.");
        }

        m_rootClassLoader = worldEditClassLoader;

        try {
            m_miDefineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            m_miDefineClass.setAccessible(true);

            Field fiClasses = clsPluginClassLoader.getDeclaredField("classes");
            fiClasses.setAccessible(true);

            m_classes = (Map<String, Class<?>>) fiClasses.get(m_rootClassLoader);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize 'ClassInjector'. Unable to initialize method bridge.", ex);
        }
    }

    @Override
    public Class<?> injectClass(String name, byte[] bin, int off, int len) throws ClassFormatError {
        try {
            Class<?> result = (Class<?>) m_miDefineClass.invoke(m_rootClassLoader, name, bin, off, len);
            m_classes.put(name, result);

            return result;
        } catch (ClassFormatError ex) {
            throw ex;
        } catch (InvocationTargetException ex) {
            Throwable innerEx = ex.getCause();
            throw new ClassFormatError("Unable to invoke dynamic method. " + (innerEx != null ? innerEx.getMessage() : ""));
        } catch (Exception ex) {
            throw new ClassFormatError("Unable to invoke dynamic method. " + ex.getMessage());
        }
    }

    @Override
    public ClassReader getClassReader(String name) throws IOException {
        String resource = String.format("%1$s.class", name.replace('.', '/'));
        InputStream is = m_worldEdit.getResource(resource);

        return new ClassReader(is);
    }
}
