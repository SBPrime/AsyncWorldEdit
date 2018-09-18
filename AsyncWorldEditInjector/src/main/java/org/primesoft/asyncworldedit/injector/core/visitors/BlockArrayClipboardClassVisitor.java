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

import com.sk89q.worldedit.extent.clipboard.Clipboard;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Stream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 *
 * @author SBPrime
 */
public class BlockArrayClipboardClassVisitor extends BaseClassVisitor {

    @FunctionalInterface
    private interface MethodFactory {

        void define(String name, String descriptor, String clsName, Method m);
    }

    private final static String IC_DESCRIPTOR = "com/sk89q/worldedit/extent/clipboard/InjectableClipboard";
    private String m_clsDescriptor;

    public BlockArrayClipboardClassVisitor(ClassVisitor classVisitor, ICreateClass createClass) {
        super(classVisitor, createClass);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        m_clsDescriptor = name;
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isCtor(name)) {
            return new MethodVisitor(api, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                private boolean m_isDone = false;
                
                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

                    if (opcode != Opcodes.INVOKESPECIAL || !isCtor(name) || m_isDone) {
                        return;
                    }

                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitTypeInsn(Opcodes.NEW, IC_DESCRIPTOR);
                    super.visitInsn(Opcodes.DUP);
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitMethodInsn(Opcodes.INVOKESPECIAL,
                            IC_DESCRIPTOR,
                            "<init>",
                            "(L" + m_clsDescriptor + ";)V",
                            false);
                    super.visitVarInsn(Opcodes.ALOAD, 1);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC,
                            Type.getInternalName(Helpers.class),
                            "createClipboard",
                            "(Lcom/sk89q/worldedit/extent/clipboard/Clipboard;Lcom/sk89q/worldedit/regions/Region;)Lcom/sk89q/worldedit/extent/clipboard/Clipboard;",
                            false);

                    super.visitFieldInsn(Opcodes.PUTFIELD, m_clsDescriptor, "m_injected", Type.getDescriptor(Clipboard.class));
                    m_isDone = true;
                }
            };
        }

        if (isPublic(access)) {
            return super.visitMethod(access, RANDOM_PREFIX + name, descriptor, signature, exceptions);
        } else {
            return super.visitMethod(access, RANDOM_PREFIX + name, descriptor, signature, exceptions);
        }
    }

    @Override
    public void visitEnd() {
        addFields();

        processMethods((String name, String descriptor, String clsName, Method m) -> defineClipboardMethod(name, descriptor, clsName, m));
        super.visitEnd();

        try {
            createInjectableClipboard();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create InjectableClipboard.", ex);
        }
    }

    private void addFields() {
        visitField(Opcodes.ACC_PRIVATE, "m_injected",
                Type.getDescriptor(Clipboard.class), null, null)
                .visitEnd();
    }

    private void createInjectableClipboard() throws IOException {
        String className = IC_DESCRIPTOR.replace("/", ".");

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, IC_DESCRIPTOR,
                null, "java/lang/Object",
                new String[]{
                    Type.getInternalName(Clipboard.class)
                });

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "m_injected",
                "L" + m_clsDescriptor + ";", null, null)
                .visitEnd();

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(L" + m_clsDescriptor + ";)V", null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0); // push `this` to the operand stack
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, IC_DESCRIPTOR, "m_injected", "L" + m_clsDescriptor + ";");
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        processMethods((String name, String descriptor, String clsName, Method m) -> defineInjectableClipboardMethod(cw, name, descriptor, clsName, m));
        cw.visitEnd();

        createClass(className, cw);
    }

    private void defineInjectableClipboardMethod(ClassWriter cw, String name, String descriptor, String clsName, Method m) {
        Class<?>[] exceptions = m.getExceptionTypes();
        Class<?> resultType = m.getReturnType();
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                name, descriptor,
                null, exceptions == null || exceptions.length == 0 ? null
                        : Stream.of(exceptions)
                                .map(i -> Type.getInternalName(i))
                                .toArray(String[]::new));

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, IC_DESCRIPTOR, "m_injected", "L" + m_clsDescriptor + ";");

        for (int i = 0; i < getArgsCount(descriptor); i++) {
            mv.visitVarInsn(Opcodes.ALOAD, i + 1);
        }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                m_clsDescriptor,
                RANDOM_PREFIX + name, descriptor,
                false);

        visitReturn(resultType, mv);

        mv.visitCode();
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void defineClipboardMethod(String name, String descriptor, String clsName, Method m) {
        Class<?>[] exceptions = m.getExceptionTypes();
        Class<?> resultType = m.getReturnType();
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC,
                name, descriptor,
                null, exceptions == null || exceptions.length == 0 ? null
                        : Stream.of(exceptions)
                                .map(i -> Type.getInternalName(i))
                                .toArray(String[]::new));

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_clsDescriptor, "m_injected", Type.getDescriptor(Clipboard.class));

        for (int i = 0; i < getArgsCount(descriptor); i++) {
            mv.visitVarInsn(Opcodes.ALOAD, i + 1);
        }

        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                clsName,
                name, descriptor,
                true);

        visitReturn(resultType, mv);

        mv.visitCode();
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void visitReturn(Class<?> resultType, MethodVisitor mv) {
        if (void.class.equals(resultType)) {
            mv.visitInsn(Opcodes.RETURN);
        } else if (double.class.equals(resultType)) {
            mv.visitInsn(Opcodes.DRETURN);
        } else if (float.class.equals(resultType)) {
            mv.visitInsn(Opcodes.FRETURN);
        } else if (int.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (long.class.equals(resultType)) {
            mv.visitInsn(Opcodes.LRETURN);
        } else if (boolean.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (byte.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (char.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (short.class.equals(resultType)) {
            mv.visitInsn(Opcodes.IRETURN);
        } else if (resultType.isPrimitive()) {
            mv.visitInsn(Opcodes.IRETURN);
        } else {
            mv.visitInsn(Opcodes.ARETURN);
        }
    }

    private void processMethods(MethodFactory mf) throws SecurityException {
        Queue<Class<?>> clsInterfaces = new ArrayDeque<>();
        clsInterfaces.add(Clipboard.class);
        Set<String> definedMethods = new HashSet<>();
        while (!clsInterfaces.isEmpty()) {
            Class<?> clsInterface = clsInterfaces.poll();
            String clsName = Type.getInternalName(clsInterface);

            for (Method m : clsInterface.getDeclaredMethods()) {
                String descriptor = Type.getMethodDescriptor(m);
                String name = m.getName();
                if (definedMethods.contains(name + descriptor)) {
                    continue;
                }

                definedMethods.add(name + descriptor);
                mf.define(name, descriptor, clsName, m);
            }

            Class<?>[] newInterfaces = clsInterface.getInterfaces();
            if (newInterfaces != null) {
                for (Class<?> c : newInterfaces) {
                    clsInterfaces.add(c);
                }
            }
        }
    }

    @Override
    public void validate() throws RuntimeException {
    }
}
