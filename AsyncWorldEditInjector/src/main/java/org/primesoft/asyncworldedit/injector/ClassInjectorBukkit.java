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
import java.lang.reflect.Method;
import java.security.CodeSource;
import java.security.SecureClassLoader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.ClassReader;
import org.primesoft.asyncworldedit.injector.core.IClassInjectorBridge;
import org.primesoft.asyncworldedit.injector.core.spigot.v1_13_Rx.InjectorNmsCore;
import org.primesoft.asyncworldedit.injector.utils.ConsumerException;
import org.primesoft.asyncworldedit.utils.ClassLoaderHelper;

/**
 *
 * @author SBPrime
 */
final class ClassInjectorBukkit implements IClassInjector {
    private final static Pattern NMS_VERSION = Pattern.compile("(org\\.bukkit\\.craftbukkit\\.)([^.]+)(\\.CraftServer)");

    private final ClassLoader m_classLoaderWe;
    private final ClassLoader m_classLoaderNms;
    private final CodeSource m_codeSourceWe;
    private final CodeSource m_codeSourceNms;
    private final Method m_miDefineClass;
    private final Plugin m_worldEdit;

    private Map<String, Class<?>> m_classesWe;
    
    private final String m_nmsVersion;

    public ClassInjectorBukkit() {
        final Server server = Bukkit.getServer();
        final Class<?> serverCls = server.getClass();
        
        m_classLoaderNms = serverCls.getClassLoader();
        m_codeSourceNms = serverCls.getProtectionDomain().getCodeSource();
        m_nmsVersion = getNmsVersion(serverCls);
        
        m_worldEdit = server.getPluginManager().getPlugin("WorldEdit");        

        ClassLoader worldEditClassLoader = ClassLoaderHelper.getPluginClassLoader(com.sk89q.worldedit.WorldEditException.class);
        if (worldEditClassLoader == null) {
            throw new IllegalStateException("Unable to initialize 'ClassInjector'. Matching class loader not found.");
        }

        m_classLoaderWe = worldEditClassLoader;
        m_codeSourceWe = com.sk89q.worldedit.WorldEditException.class.getProtectionDomain().getCodeSource();

        try {
            m_miDefineClass = SecureClassLoader.class.getDeclaredMethod("defineClass",
                    String.class, byte[].class, int.class, int.class, CodeSource.class);
            m_miDefineClass.setAccessible(true);

            Field fiClasses = ClassLoaderHelper.getPluginClassLoader().getDeclaredField("classes");
            fiClasses.setAccessible(true);

            m_classesWe = (Map<String, Class<?>>) fiClasses.get(m_classLoaderWe);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to initialize 'ClassInjector'. Unable to initialize method bridge.", ex);
        }
    }

    private static String getNmsVersion(final Class<?> serverCls) {
        Matcher m = NMS_VERSION.matcher(serverCls.getCanonicalName());
        m.find();
        return m.group(2);
    }

    private Class<?> injectClass(
            final ClassLoader cl,
            final String name,
            final byte[] bin,
            final int off,
            final int len,
            final CodeSource codeSource) throws ClassFormatError {
        
        try {
            return (Class<?>) m_miDefineClass.invoke(cl, name, bin, off, len, codeSource);
        } catch (Exception ex) {
            throw (ClassFormatError) (new ClassFormatError("Unable to inject class.").initCause(ex));
        }
    }        
    
    @Override
    public Class<?> injectNMSClass(String name, byte[] bin, int off, int len) throws ClassFormatError {
        return injectClass(m_classLoaderNms, 
                name, bin, off, len, m_codeSourceNms);
        
    }
    
    @Override
    public Class<?> injectWorldEditClass(String name, byte[] bin, int off, int len) throws ClassFormatError {
        final Class<?> result = injectClass(m_classLoaderWe, name, bin, off, len, m_codeSourceWe);
        m_classesWe.put(name, result);
        
        return result;
    }

    @Override
    public ClassReader getWorldEditClassReader(String name) throws IOException {
        String resource = String.format("%1$s.class", name.replace('.', '/'));
        InputStream is = m_worldEdit.getResource(resource);

        return new ClassReader(is);
    }

    @Override
    public ClassReader getNMSClassReader(String name) throws IOException {        
        String resource = String.format("%1$s.class", name.replace('.', '/'));
        InputStream is = m_classLoaderNms.getResourceAsStream(resource);

        return new ClassReader(is);
    }

    @Override
    public String correctNmsName(String name) {
        return String.format(name, m_nmsVersion);
    }
    
    @Override
    public ConsumerException<IClassInjectorBridge, IOException> getNmsInjection() {
        if ("v1_13_R1".equals(m_nmsVersion) || 
            "v1_13_R2".equals(m_nmsVersion)) {
            return InjectorNmsCore::injectClasses;
        }
        
        return this::noOpInjector;
    }
    
    private void noOpInjector(IClassInjectorBridge ci) throws IOException {}
}
