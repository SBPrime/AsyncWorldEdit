/*
 * AsyncWorldEdit a performance improvement plugin for Minecraft WorldEdit plugin.
 * Copyright (c) 2019, SBPrime <https://github.com/SBPrime/>
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
package org.primesoft.asyncworldedit.injector.core.visitors.worldedit.util.eventbus;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.injected.util.eventbus.IDispatchableEventBus;
import org.primesoft.asyncworldedit.injector.injected.util.eventbus.IEventBus;

/**
 *
 * @author SBPrime
 */
public class EventBusVisitor extends BaseClassVisitor {

    private final static String PREFIX = "nonwrapped_";
    private final static String FIELD_WRAPPER = "m_wrapper";

    private String m_descriptorClass;

    private Map<String, Set<String>> m_methods = new HashMap<>();

    public EventBusVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void validate() throws RuntimeException {
        for (Method m : IEventBus.class.getDeclaredMethods()) {
            final Set<String> descriptors = m_methods.get(m.getName());
            final String md = Type.getMethodDescriptor(m);
            if (descriptors == null || !descriptors.remove(md)) {
                throw new IllegalStateException("Method '" + m.getName() + md + "' not found in " + m_descriptorClass);
            }
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        m_descriptorClass = name;

        super.visit(version,
                access,
                name, signature, superName,
                Stream.concat(Stream.of(interfaces), Stream.of(Type.getInternalName(IDispatchableEventBus.class))).toArray(String[]::new));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isCtor(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        if (!isPublic(access) && !"dispatch".equals(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        m_methods.computeIfAbsent(name, i -> new HashSet<>()).add(descriptor);

        return super.visitMethod(changeVisibility(access, Opcodes.ACC_PUBLIC),
                PREFIX + name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        cv.visitField(Opcodes.ACC_PRIVATE, FIELD_WRAPPER, Type.getDescriptor(IEventBus.class), null, false).visitEnd();

        visitSetOverrideMethod();
        for (Method m : IEventBus.class.getDeclaredMethods()) {
            visitDelegateMethod(m);
        }

        super.visitEnd();
    }

    private void visitSetOverrideMethod() {
        final Method setOverride = getMethod(IDispatchableEventBus.class, "setOverride").findFirst().get();
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC,
                setOverride.getName(), Type.getMethodDescriptor(setOverride), null, null);

        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_descriptorClass, FIELD_WRAPPER, Type.getDescriptor(IEventBus.class));

        mv.visitInsn(Opcodes.RETURN);

        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    private void visitDelegateMethod(Method m) {
        final String descriptor = Type.getMethodDescriptor(m);
        final String name = m.getName();
        final MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, name, descriptor, null, null);
        final Class<?>[] params = m.getParameterTypes();

        Label isNUll = new Label();

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, FIELD_WRAPPER, Type.getDescriptor(IEventBus.class));
        mv.visitJumpInsn(Opcodes.IFNULL, isNUll);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_descriptorClass, FIELD_WRAPPER, Type.getDescriptor(IEventBus.class));
        for (int i = 0; i < params.length; i++) {
            visitArgumemt(mv, params[i], i + 1);
        }

        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(IEventBus.class), name, descriptor, true);
        visitReturn(mv, m.getReturnType());

        mv.visitLabel(isNUll);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        for (int i = 0; i < params.length; i++) {
            visitArgumemt(mv, params[i], i + 1);
        }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, m_descriptorClass, PREFIX + name, descriptor, false);

        visitReturn(mv, m.getReturnType());

        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }
}
