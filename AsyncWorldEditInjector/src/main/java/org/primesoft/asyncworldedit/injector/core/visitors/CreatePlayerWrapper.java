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
package org.primesoft.asyncworldedit.injector.core.visitors;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.injected.entity.WrappedPlayerAction;
import org.primesoft.asyncworldedit.injector.injected.entity.WrappedPlayerData;

/**
 *
 * @author SBPrime
 */
public class CreatePlayerWrapper extends BaseCreateWrapper {

    private static final String DESCRIPTOR_WRAPPER_DATA = Type.getDescriptor(WrappedPlayerData.class);
    private static final String DESCRIPTOR_WRAPPER_ACTION = Type.getDescriptor(WrappedPlayerAction.class);
    
    public final static String IC_DESCRIPTOR = "org/primesoft/asyncworldedit/worldedit/entity/PlayerWrapper";
    
    private final List<MethodEntry> m_knownMethods = new LinkedList<>();

    public CreatePlayerWrapper(ICreateClass createClass) {
        super(createClass, Player.class, IC_DESCRIPTOR);
    }

    @Override
    protected void processFields(ClassWriter cw) {
        super.processFields(cw);

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "m_data", DESCRIPTOR_WRAPPER_DATA, null, null)
                .visitEnd();
    }

    @Override
    protected void ctorCode(MethodVisitor mv) {
        super.ctorCode(mv);

        mv.visitVarInsn(Opcodes.ALOAD, 0);

        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(WrappedPlayerData.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getInternalName(WrappedPlayerData.class),
                "<init>",
                "()V",
                false);

        mv.visitFieldInsn(Opcodes.PUTFIELD, m_targetName, "m_data",
                DESCRIPTOR_WRAPPER_DATA);
    }

    @Override
    protected void methodBody(MethodVisitor mv, String name, String descriptor, Method m) {
        if ("getWorld".equals(name)) {
            methodGetWorld(mv, name, descriptor, m);
        } else {
            safeExecute(mv, name, descriptor, m);
        }                
    }

    @Override
    protected void processEnd(ClassWriter cw) {
        for (MethodEntry me : m_knownMethods) {
            wrapperClass(me);
        }
        
        super.processEnd(cw);
    }        

    private void methodGetWorld(MethodVisitor mv, String name, String descriptor, Method m) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_targetName, "m_data", DESCRIPTOR_WRAPPER_DATA);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_targetName, "m_injected", Type.getDescriptor(Player.class));
        
        callParrent(mv, name, descriptor, m);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(WrappedPlayerData.class),
                "getWorld",
                "("
                        + Type.getDescriptor(Player.class)
                        + Type.getDescriptor(World.class)
                        + ")" + Type.getDescriptor(World.class),
                false);
        
        mv.visitInsn(Opcodes.ARETURN);
    }

    private void safeExecute(MethodVisitor mv, String name, String descriptor, Method m) {
        UUID uuid = UUID.randomUUID();
        String id = String.format("__%016x%016x__", uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
        MethodEntry me = new MethodEntry(id, mv, name, descriptor, m);
        m_knownMethods.add(me);                
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_targetName, "m_data", DESCRIPTOR_WRAPPER_DATA);
        
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_targetName, "m_injected", Type.getDescriptor(Player.class));
        
        mv.visitTypeInsn(Opcodes.NEW, me.className());
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, me.className(),
                "<init>",
                "()V",
                false);
        
        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            visitArgumemt(mv, params[i], i + 1);
        }
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                me.className(),
                name, me.DescriptorSimple,
                false);
        
        Class<?> resultType = m.getReturnType();
        boolean hasResult = !void.class.equals(resultType);
        
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(WrappedPlayerData.class),
                hasResult ? "executeAction" : "executeFunction",
                "("
                        + Type.getDescriptor(Player.class)
                        + Type.getDescriptor(WrappedPlayerAction.class)
                        + ")" + (hasResult ? Type.getDescriptor(Object.class) : "V"),
                false);
        if (hasResult) {            
            checkCast(mv, Type.getDescriptor(resultType));
        }
        
        visitReturn(mv, m.getReturnType());
    }

    private void wrapperClass(MethodEntry me) {
        String classDescriptor = me.className();
        String className = classDescriptor.replace("/", ".");

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, classDescriptor,
                null, Type.getInternalName(WrappedPlayerAction.class),
                new String[0]);
        
        
        emitEmptyCtor(cw, WrappedPlayerAction.class);
        Method m = me.Method;
        Class<?>[] params = m.getParameterTypes();
        Class<?> resultType = m.getReturnType();
        
        for (int i = 0;i < params.length; i++) {            
            cw.visitField(Opcodes.ACC_PRIVATE, "m_field_" + i, Type.getDescriptor(params[i]), null, null);
        }
        
        wrapperClassInit(cw, me, params, classDescriptor);                 
        wrapperClassExecute(cw, classDescriptor, me, params, resultType);
        cw.visitEnd();
        try {
            createClass(className, cw);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create " + m_targetName + ".", ex);
        }
    }

    private void wrapperClassInit(ClassWriter cw, MethodEntry me, Class<?>[] params, String classDescriptor) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                me.Name, me.DescriptorSimple,
                null, new String[] { "java/lang/Exception" }
        );
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            visitArgumemt(mv, params[i], i + 1);
            mv.visitFieldInsn(Opcodes.PUTFIELD, classDescriptor, "m_field_" + i, Type.getDescriptor(params[i]));
        }
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitEnd();
    }

    private void wrapperClassExecute(ClassWriter cw, String classDescriptor, MethodEntry me, Class<?>[] params, Class<?> resultType) {
        boolean hasResult = !void.class.equals(resultType);
        
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, 
                hasResult ? "function" : "action",
                "(Lcom/sk89q/worldedit/entity/Player;)" + (hasResult ? "Ljava/lang/Object;" : "V"),
                null, new String[] { "java/lang/Exception" }
        );
        mv.visitVarInsn(Opcodes.ALOAD, 1);        
        for (int i = 0; i < params.length; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, classDescriptor, "m_field_" + i, Type.getDescriptor(params[i]));
        }
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                m_clsName,
                me.Name, me.Descriptor,
                true);
        if (!hasResult) {
            mv.visitInsn(Opcodes.RETURN);
        } else {
            encapsulatePrimitives(mv, Type.getDescriptor(resultType));
            mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Object");
            mv.visitInsn(Opcodes.ARETURN);
        }
                
        mv.visitEnd();
    }
    
    private static class MethodEntry {
        public final String Id;
        
        public final MethodVisitor Mv;
        
        public final String Name;
        
        public final String Descriptor;
        
        public final Method Method;
        
        private String DescriptorSimple;
        
        MethodEntry(String id, MethodVisitor mv, String name, String descriptor, Method method) {
            Id = id;
            Mv = mv;
            Name = name;
            Descriptor = descriptor;
            
            int idx = Descriptor.indexOf(")");
            DescriptorSimple = Descriptor.substring(0, idx + 1) + "V";
            Method = method;
        }
        
        private String className() {
            return IC_DESCRIPTOR + "_WrappedMethod_" + Name + "_" + Id;
        }
    }
}
