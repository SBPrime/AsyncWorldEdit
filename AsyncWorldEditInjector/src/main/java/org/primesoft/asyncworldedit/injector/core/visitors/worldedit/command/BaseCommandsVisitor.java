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
package org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.ICreateClass;
import org.primesoft.asyncworldedit.injector.core.visitors.MethodAnnotationRecorderVisitor;
import org.primesoft.asyncworldedit.injector.utils.MethodEntry;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

/**
 *
 * @author prime
 */
public abstract class BaseCommandsVisitor extends BaseClassVisitor {

    private final List<MethodEntry> m_methodsToWrap = new ArrayList<>();

    private String m_descriptorClass;
    private String m_descriptorClassInner;

    private final Map<String, Map<String, SimpleValidator>> m_methods;

    private static SimpleValidator buildValidator(String name, String descriptor) {
        return new SimpleValidator("Missing method '" + name + descriptor + "'");
    }

    protected BaseCommandsVisitor(ClassVisitor classVisitor, ICreateClass createClass,
            Map<String, String[]> methods) {
        super(classVisitor, createClass);

        m_methods = methods.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, i
                        -> Stream.of(i.getValue()).collect(Collectors.toMap(j -> j, j -> buildValidator(i.getKey(), j)))));
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        m_descriptorClass = name;
        m_descriptorClassInner = name + "_" + RANDOM_PREFIX + "_InnerForMethod_";

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitEnd() {
        try {
            for (MethodEntry me : m_methodsToWrap) {
                emitInnerClass(me);
                emitMethod(me);
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create inner class.", ex);
        }

        super.visitEnd();
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (checkFlag(access, Opcodes.ACC_PRIVATE) || checkFlag(access, Opcodes.ACC_PROTECTED)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        Map<String, SimpleValidator> descriptors = m_methods.get(name);
        if (descriptors == null) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        SimpleValidator validator = descriptors.get(descriptor);
        if (validator == null) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        validator.set();
        final MethodEntry me = new MethodEntry(access, name, descriptor, signature, exceptions);
        m_methodsToWrap.add(me);
        return new MethodAnnotationRecorderVisitor(api,
                cv.visitMethod(changeVisibility(access, Opcodes.ACC_PUBLIC),
                        RANDOM_PREFIX + name, descriptor, signature, exceptions), me);
    }

    @Override
    public void validate() throws RuntimeException {
        m_methods.values().stream()
                .flatMap(i -> i.values().stream())
                .filter(Objects::nonNull)
                .forEach(SimpleValidator::validate);
    }

    private void emitMethod(MethodEntry me) {
        MethodVisitor mv = cv.visitMethod(me.access, me.name, me.descriptor, me.signature, me.exceptions);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(me.name);

        String methodClassName = getMethodClassName(me);
        mv.visitTypeInsn(Opcodes.NEW, methodClassName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, methodClassName, "<init>", "()V", false);

        final String[] args = getArgs(me.descriptor);
        
        mv.visitLdcInsn(args.length);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        for (int i = 0; i < args.length; i++) {
            final String argumentType = args[i];
            
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(i);
            visitArgumemt(mv, argumentType, i + 1);
            encapsulatePrimitives(mv, argumentType);
            mv.visitInsn(Opcodes.AASTORE);
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, CLASS_HELPERS_DESCRIPTOR,
                "executeMultiArgMethod",
                "(Ljava/lang/Object;Ljava/lang/String;Lorg/primesoft/asyncworldedit/injector/utils/MultiArgWorldEditOperationAction;[Ljava/lang/Object;)V", false);

        if (me.descriptor.endsWith(")I")) {
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitInsn(Opcodes.IRETURN);
        } else if (me.descriptor.endsWith(")V")) {
            mv.visitInsn(Opcodes.RETURN);
        } else {
            throw new IllegalStateException("Method result not supported for: " + me.name + me.descriptor);
        }

        mv.visitMaxs(2, 1);

        me.annotations.forEach(ae -> ae.visit(mv));

        mv.visitEnd();
    }

    private String getMethodClassName(MethodEntry me) {
        return m_descriptorClassInner + me.name;
    }

    private void emitInnerClass(MethodEntry me) throws IOException {
        String className = getMethodClassName(me);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE, className,
                null, "java/lang/Object",
                new String[]{
                    "org/primesoft/asyncworldedit/injector/utils/MultiArgWorldEditOperationAction"
                });
        emitEmptyCtor(cw);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", "(Ljava/lang/Object;[Ljava/lang/Object;)V",
                null, new String[]{"java/lang/Exception"});

        mv.visitCode();
        String[] args = getArgs(me.descriptor);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, m_descriptorClass);
        for (int i = 0; i < args.length; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitLdcInsn(i);
            mv.visitInsn(Opcodes.AALOAD);

            checkCast(mv, args[i]);
        }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                m_descriptorClass, RANDOM_PREFIX + me.name, me.descriptor, false);
        
        if (!me.descriptor.endsWith(")V")) {
            mv.visitInsn(Opcodes.POP);
        }
        
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();
        createClass(className.replace("/", "."), cw);
    }
}
