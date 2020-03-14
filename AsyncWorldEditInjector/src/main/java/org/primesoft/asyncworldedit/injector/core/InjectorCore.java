/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * AsyncWorldEdit Injector a hack plugin that allows AsyncWorldEdit to integrate with
 * the WorldEdit plugin.
 *
 * Copyright (c) 2014, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) AsyncWorldEdit contributors
 * Copyright (c) AsyncWorldEdit injector contributors
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
package org.primesoft.asyncworldedit.injector.core;

import org.primesoft.asyncworldedit.injector.core.visitors.WrapGetWorldVisitor;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.primesoft.asyncworldedit.injector.IClassInjector;
import org.primesoft.asyncworldedit.injector.classfactory.IClassFactory;
import org.primesoft.asyncworldedit.injector.classfactory.base.BaseClassFactory;
import org.primesoft.asyncworldedit.injector.core.visitors.AsyncWrapperVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassCreator;
import org.primesoft.asyncworldedit.injector.core.visitors.extent.clipboard.BlockArrayClipboardClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.CreateActorFactory;
import org.primesoft.asyncworldedit.injector.core.visitors.CreateNoPermsActor;
import org.primesoft.asyncworldedit.injector.core.visitors.CreateNoPermsPlayer;
import org.primesoft.asyncworldedit.injector.core.visitors.CreatePlayerFactory;
import org.primesoft.asyncworldedit.injector.core.visitors.CreatePlayerWrapper;
import org.primesoft.asyncworldedit.injector.core.visitors.ICreateClass;
import org.primesoft.asyncworldedit.injector.core.visitors.InjectorClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.function.operation.OperationsClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.EditSessionClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.bukkit.BukkitEntityVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.CommandsRegistrationVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.RegionCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.SchematicCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.ScriptingCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.SnapshotUtilCommandsVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.BlockReplacerVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command.tool.BrushToolVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.extent.reorder.ResetableExtentVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.regions.RegionVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.util.collection.LocatedBlockListVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.worldedit.util.eventbus.EventBusVisitor;
import org.primesoft.asyncworldedit.utils.ExceptionHelper;

/**
 *
 * @author SBPrime
 */
public class InjectorCore {

    /**
     * Injector core instance
     */
    private static InjectorCore s_instance = null;

    /**
     * Instance MTA mutex
     */
    private final static Object s_mutex = new Object();

    /**
     * Get the static instance
     *
     * @return
     */
    public static InjectorCore getInstance() {
        if (s_instance == null) {
            synchronized (s_mutex) {
                if (s_instance == null) {
                    s_instance = new InjectorCore();
                }
            }
        }

        return s_instance;
    }

    /**
     * The platform specific API
     */
    private IInjectorPlatform m_platform;

    /**
     * The WorldEdit class factory
     */
    private IClassFactory m_classFactory = new BaseClassFactory();

    private IClassInjector m_classInjector;

    /**
     * The MTA access mutex
     */
    private final Object m_mutex = new Object();

    /**
     * Log a console message
     *
     * @param message
     */
    private void log(String message) {
        IInjectorPlatform platform = m_platform;
        if (platform == null) {
            return;
        }

        m_platform.log(message);
    }

    /**
     * Initialize the injector core
     *
     * @param platform
     * @param classInjector
     * @return 
     */
    public boolean initialize(IInjectorPlatform platform, IClassInjector classInjector) {
        synchronized (m_mutex) {
            if (m_platform != null) {
                log("Injector platform is already set to "
                        + m_platform.getPlatformName() + "."
                        + "Ignoring new platform " + platform.getPlatformName());
                return false;
            }

            m_platform = platform;
            m_classInjector = classInjector;

            log("Injector platform set to: " + platform.getPlatformName());
        }

        return injectClasses();
    }

    private boolean injectClasses() {
        try {
            log("Injecting NMS classes...");
            m_classInjector.getNmsInjection().consume(new NmsClassInjectorBridge());

            log("Injecting WorldEdit classes...");
            modifyClasses("com.sk89q.worldedit.util.eventbus.EventBus", c -> new EventBusVisitor(c));

            modifyClasses("com.sk89q.worldedit.math.BlockVector2", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.math.BlockVector3", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.math.Vector2", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.math.Vector3", c -> new AsyncWrapperVisitor(c));

            modifyClasses("com.sk89q.worldedit.world.block.BlockStateHolder", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.world.block.BaseBlock", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.world.block.BlockState", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.entity.BaseEntity", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.util.Location", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.world.biome.BiomeType", c -> new AsyncWrapperVisitor(c));
            modifyClasses("com.sk89q.worldedit.world.weather.WeatherType", c -> new AsyncWrapperVisitor(c));

            modifyClasses("com.sk89q.worldedit.EditSession", c -> new EditSessionClassVisitor(c));
            modifyClasses("com.sk89q.worldedit.function.operation.Operations", (c, cc) -> new OperationsClassVisitor(c, cc));
            modifyClasses("com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard", (c, cc) -> new BlockArrayClipboardClassVisitor(c, cc));
            modifyClasses("com.sk89q.worldedit.extension.platform.PlayerProxy", c -> new WrapGetWorldVisitor(c));

            modifyClasses("com.sk89q.worldedit.util.collection.LocatedBlockList", c -> new LocatedBlockListVisitor(c));
            modifyClasses("com.sk89q.worldedit.extent.reorder.MultiStageReorder", c -> new ResetableExtentVisitor(c));
            modifyClasses("com.sk89q.worldedit.extent.reorder.ChunkBatchingExtent", c -> new ResetableExtentVisitor(c));

            // Regions
            modifyClasses("com.sk89q.worldedit.regions.AbstractRegion", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.EllipsoidRegion", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.ConvexPolyhedralRegion", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.TransformRegion", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.RegionIntersection", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.NullRegion", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.Polygonal2DRegion", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.CylinderRegion", c -> new RegionVisitor(c));
            modifyClasses("com.sk89q.worldedit.regions.CuboidRegion", c -> new RegionVisitor(c));

            // Commands
            modifyClasses("com.sk89q.worldedit.command.SnapshotUtilCommands", (c, cc) -> new SnapshotUtilCommandsVisitor(c, cc));
            modifyClasses("com.sk89q.worldedit.command.ScriptingCommands", (c, cc) -> new ScriptingCommandsVisitor(c, cc));
            modifyClasses("com.sk89q.worldedit.command.SchematicCommands", (c, cc) -> new SchematicCommandsVisitor(c, cc));
            modifyClasses("com.sk89q.worldedit.command.RegionCommands", (c, cc) -> new RegionCommandsVisitor(c, cc));

            modifyClasses("com.sk89q.worldedit.command.UtilityCommandsRegistration", c -> new CommandsRegistrationVisitor(c));
            modifyClasses("com.sk89q.worldedit.bukkit.BukkitEntity", (c, cc) -> new BukkitEntityVisitor(c, cc));
            
            // Bukkit
            modifyClasses("com.sk89q.worldedit.command.RegionCommandsRegistration", c -> new CommandsRegistrationVisitor(c));
            
            // Reflection
            modifyClasses("com.sk89q.worldedit.command.tool.BrushTool", c -> new BrushToolVisitor(c));
            modifyClasses("com.sk89q.worldedit.command.tool.BlockReplacer", c -> new BlockReplacerVisitor(c));

            crateClass(cc-> new CreatePlayerWrapper(cc));
            crateClass(cc-> new CreateNoPermsPlayer(cc));
            crateClass(cc-> new CreateNoPermsActor(cc));
            crateClass(cc-> new CreatePlayerFactory(cc));
            crateClass(cc-> new CreateActorFactory(cc));

            return true;
        } catch (Throwable ex) {
            log("****************************");
            log("* CLASS INJECTION FAILED!! *");
            log("****************************");
            log("* AsyncWorldEdit won't work properly.");
            log("*");
            log("* >>>> Please make sure that you are using a supported version of world edit <<<< ");
            log("*");
            ExceptionHelper.printException(ex);
            log("****************************");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex1) {
            }
            return false;
        }
    }

    /**
     * Set new class factory
     *
     * @param factory
     */
    public void setClassFactory(IClassFactory factory) {
        synchronized (m_mutex) {
            if (factory == null) {
                factory = new BaseClassFactory();
                log("New class factory set to default factory.");
            } else {
                log("New class factory set to: " + factory.getClass().getName());
            }

            m_classFactory = factory;
        }
    }

    /**
     * Get the class factory
     *
     * @return
     */
    public IClassFactory getClassFactory() {
        return m_classFactory;
    }

    /**
     * getInjectorVersion The injector version
     *
     * @return
     */
    public double getVersion() {
        return 2.0000;
    }

    private void modifyClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getWorldEditClassReader(cn),
                (name, data) -> m_classInjector.injectWorldEditClass(name, data, 0, data.length));
    }
    
    private void modifyNMSClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getNMSClassReader(cn),
                (name, data) -> m_classInjector.injectNMSClass(name, data, 0, data.length));
    }
    
    private void modifyClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getWorldEditClassReader(cn),
                (name, data) -> m_classInjector.injectWorldEditClass(name, data, 0, data.length));
    }
    
    private void modifyNMSClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor) throws IOException {
        modifyClasses(className, classVisitor, 
                cn -> m_classInjector.getNMSClassReader(cn),
                (name, data) -> m_classInjector.injectNMSClass(name, data, 0, data.length));
    }
    
    private void modifyClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor,
            IGetClassReader getClassReader, IEmit emit) throws IOException {
        log("Modify class " + className);
        
        ClassReader classReader = getClassReader.get(className);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        InjectorClassVisitor icv = classVisitor.apply(classWriter);

        classReader.accept(icv, 0);
        icv.validate();

        byte[] data = classWriter.toByteArray();
        writeData(className , data);
        emit.emit(className, data);        
    }

    private void modifyClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor,
            IGetClassReader getClassReader, IEmit emit) throws IOException {
        log("Modify class " + className);
                
        ClassReader classReader = getClassReader.get(className);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        InjectorClassVisitor icv = classVisitor.apply(classWriter, (cn, cw) -> createClasses(cn, cw, emit));
        classReader.accept(icv, 0);
       
        icv.validate();

        byte[] data = classWriter.toByteArray();

        writeData(className , data);
        emit.emit(className, data);
    }
    
    private void crateClass(Function<ICreateClass, BaseClassCreator> factory) {
        crateClass(factory, 
                (name, data) -> m_classInjector.injectWorldEditClass(name, data, 0, data.length));
    }
    
    private void crateNMSClass(Function<ICreateClass, BaseClassCreator> factory) {
        crateClass(factory, 
                (name, data) -> m_classInjector.injectNMSClass(name, data, 0, data.length));
    }
    
    private void crateClass(Function<ICreateClass, BaseClassCreator> factory, IEmit emit) {
        BaseClassCreator bcc = factory.apply((cn, cw) -> createClasses(cn, cw, emit));
        
        log("Creating class " + bcc.getName());
        bcc.run();
    }
    
    private void createClasses(String className, ClassWriter classWriter, IEmit emit) throws IOException {
        byte[] data = classWriter.toByteArray();

        writeData(className , data);
        emit.emit(className, data);
    }

    private void writeData(String className, byte[] data) {
        try (DataOutputStream dout = new DataOutputStream(new FileOutputStream(new File("./classes/" + className + ".class")))) {
            dout.write(data);
        } catch (IOException ex) {            
        }
    }

    @FunctionalInterface
    private interface IGetClassReader {
        ClassReader get(String className) throws IOException;
    }
    
    @FunctionalInterface
    private interface IEmit {
        void emit(String className, byte[] data) throws IOException;
    }

    private class NmsClassInjectorBridge implements IClassInjectorBridge {

        public NmsClassInjectorBridge() {
        }

        @Override
        public void modifyClasses(String className, Function<ClassWriter, InjectorClassVisitor> classVisitor) throws IOException {
            modifyNMSClasses(m_classInjector.correctNmsName(className), classVisitor);
        }

        @Override
        public void modifyClasses(String className, BiFunction<ClassWriter, ICreateClass, InjectorClassVisitor> classVisitor) throws IOException {
            modifyNMSClasses(m_classInjector.correctNmsName(className), classVisitor);
        }

        @Override
        public void crateClass(Function<ICreateClass, BaseClassCreator> factory) {
            crateNMSClass(factory);
        }
    }
}
