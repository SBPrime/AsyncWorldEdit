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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.injected.IWrapper;

/**
 *
 * @author SBPrime
 */
public abstract class BaseCreateWrapper extends BaseClassCreator {

    private final String m_clsDescriptor;
    protected final String m_targetName;
    private final Class<?> m_clsToWrapp;
    private final String m_clsName;

    @Override
    public String getName() {
        return m_targetName;
    }

    public BaseCreateWrapper(ICreateClass createClass,
            Class<?> clsToWrapp, String wrapperName) {
        super(createClass);

        m_clsName = Type.getInternalName(clsToWrapp);
        m_clsDescriptor = Type.getDescriptor(clsToWrapp);
        m_targetName = wrapperName;
        m_clsToWrapp = clsToWrapp;
    }

    @Override
    public void run() {
        String className = m_targetName.replace("/", ".");

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, m_targetName,
                null, "java/lang/Object",
                new String[]{
                    Type.getInternalName(m_clsToWrapp),
                    Type.getInternalName(IWrapper.class)
                });

        processFields(cw);

        createCtor(cw);

        processGetWrapper(cw);
        processEquals(cw);

        processMethods((String name, String descriptor, String clsName, Method m) -> defineMethod(cw, name, descriptor, clsName, m),
                m_clsToWrapp);

        cw.visitEnd();
        try {
            createClass(className, cw);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create " + m_targetName + ".", ex);
        }
    }

    protected void processFields(ClassWriter cw) {
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "m_injected",
                m_clsDescriptor, null, null)
                .visitEnd();
    }

    private void createCtor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(" + m_clsDescriptor + ")V", null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0); // push `this` to the operand stack
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);

        ctorCode(mv);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void defineMethod(ClassWriter cw, String name, String descriptor, String clsName, Method m) {
        Class<?>[] exceptions = m.getExceptionTypes();
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC,
                name, descriptor,
                null, exceptions == null || exceptions.length == 0 ? null
                        : Stream.of(exceptions)
                                .map(i -> Type.getInternalName(i))
                                .toArray(String[]::new));

        methodBody(mv, name, descriptor, m);

        mv.visitCode();
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    protected void methodBody(MethodVisitor mv, String name, String descriptor, Method m) {
        callParrent(mv, name, descriptor, m);
        visitReturn(mv, m.getReturnType());
    }

    protected final void callParrent(MethodVisitor mv, String name, String descriptor, Method m) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_targetName, "m_injected", m_clsDescriptor);

        Class<?>[] params = m.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            visitArgumemt(mv, params[i], i + 1);
        }

        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE,
                m_clsName,
                name, descriptor,
                true);
    }

    private void processGetWrapper(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getWrappedInstance", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_targetName, "m_injected", m_clsDescriptor);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Object.class));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void processEquals(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "equals", "(Ljava/lang/Object;)Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/primesoft/asyncworldedit/injector/core/visitors/Helpers",
                "wrapperEquals",
                "(Ljava/lang/Object;Ljava/lang/Object;)Z", false);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    protected void ctorCode(MethodVisitor mv) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_targetName, "m_injected", m_clsDescriptor);
    }
}
