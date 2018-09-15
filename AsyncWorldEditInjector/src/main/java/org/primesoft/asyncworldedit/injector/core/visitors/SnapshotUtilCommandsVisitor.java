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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import static org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor.RANDOM_PREFIX;
import org.primesoft.asyncworldedit.injector.utils.AnnotationEntry;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;
import org.primesoft.asyncworldedit.injector.utils.MethodEntry;

/**
 *
 * @author SBPrime
 */
public final class SnapshotUtilCommandsVisitor extends BaseClassVisitor {

    private final static String DESCRIPTOR_CLASS_INNER = "com/sk89q/worldedit/command/SnapshotUtilCommands_InnerForMethod";
    private final static String DESCRIPTOR_CLASS = "com/sk89q/worldedit/command/SnapshotUtilCommands";
    private final static Map<String, Map<String, SimpleValidator>> METHODS;

    static {
        METHODS = new HashMap<>();
        METHODS.put("restore",
                Stream.of("(Lcom/sk89q/worldedit/entity/Player;Lcom/sk89q/worldedit/LocalSession;Lcom/sk89q/worldedit/EditSession;Lcom/sk89q/minecraft/util/commands/CommandContext;)V")
                        .collect(Collectors.toMap(i -> i, i -> buildValidator("restore", i)))
        );
    }

    private static SimpleValidator buildValidator(String name, String descriptor) {
        return new SimpleValidator(String.format("Missing method '%1$s%2$s'", name, descriptor));
    }

    private final List<MethodEntry> m_methodsToWrap = new ArrayList<>();

    public SnapshotUtilCommandsVisitor(ClassVisitor classVisitor, ICreateClass createClass) {
        super(classVisitor, createClass);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!isPublic(access)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }

        Map<String, SimpleValidator> descriptors = METHODS.get(name);
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
        return new MethodVisitor(api, cv.visitMethod(changeVisibility(access, Opcodes.ACC_PUBLIC),
                RANDOM_PREFIX + name, descriptor, signature, exceptions)) {
            @Override
            public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
                AnnotationEntry an = new AnnotationEntry(descriptor, visible);
                me.annotations.add(an);

                return new AnnotationScannerVisitor(an, api,
                        super.visitAnnotation("Lorg/primesoft/asyncworldedit/injector/utils/FakeAttrib;", false)) {
                    @Override
                    protected void doVisit(String name, Object value) {
                    }

                    @Override
                    protected AnnotationVisitor doVisitAnnotation(String name, String descriptor) {
                        return null;
                    }

                    @Override
                    protected AnnotationVisitor doVisitArray(String name, Collection<Object> values) {
                        return new AnnotationVisitor(api, super.doVisitArray("fooArray", values)) {
                            @Override
                            public void visit(String name, Object value) {
                                values.add(value);
                            }   
                        };
                    }

                    @Override
                    protected void doVisitEnum(String name, String descriptor, String value) {
                    }
                };
            }
        };
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

    private String getMethodClassName(MethodEntry me) {
        return DESCRIPTOR_CLASS_INNER + RANDOM_PREFIX + me.name;
    }

    private void emitInnerClass(MethodEntry me) throws IOException {
        if (me.exceptions != null && me.exceptions.length > 1) {
            throw new IllegalArgumentException("Expected one or non exceptions.");
        }

        String className = getMethodClassName(me);
        String exception = me.exceptions[0];

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE, className,
                null, "java/lang/Object",
                new String[]{
                    "org/primesoft/asyncworldedit/injector/utils/MultiArgWorldEditOperationAction"
                });
        emitEmptyCtor(cw);

        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", "(Ljava/lang/Object;[Ljava/lang/Object;)V",
                null, new String[]{"com/sk89q/worldedit/WorldEditException"});

        mv.visitCode();
        String[] args = getArgs(me.descriptor);
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitTypeInsn(Opcodes.CHECKCAST, DESCRIPTOR_CLASS);
        for (int i = 0; i < args.length; i++) {
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitLdcInsn(i);
            mv.visitInsn(Opcodes.AALOAD);
            mv.visitTypeInsn(Opcodes.CHECKCAST, args[i].substring(1));
        }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                DESCRIPTOR_CLASS, RANDOM_PREFIX + me.name, me.descriptor, false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();
        createClass(className.replace("/", "."), cw);
    }

    private void emitMethod(MethodEntry me) {
        MethodVisitor mv = cv.visitMethod(me.access, me.name, me.descriptor, me.signature, me.exceptions);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitLdcInsn(me.name);

        String methodClassName = getMethodClassName(me);
        mv.visitTypeInsn(Opcodes.NEW, methodClassName);
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, methodClassName, "<init>", "()V", false);

        int args = getArgsCount(me.descriptor);
        mv.visitLdcInsn(args);
        mv.visitTypeInsn(Opcodes.ANEWARRAY, "java/lang/Object");
        for (int i = 0; i < args; i++) {
            mv.visitInsn(Opcodes.DUP);
            mv.visitLdcInsn(i);
            mv.visitVarInsn(Opcodes.ALOAD, i + 1);
            mv.visitInsn(Opcodes.AASTORE);
        }

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/primesoft/asyncworldedit/injector/core/visitors/Helpers",
                "executeMultiArgMethod",
                "(Ljava/lang/Object;Ljava/lang/String;Lorg/primesoft/asyncworldedit/injector/utils/MultiArgWorldEditOperationAction;[Ljava/lang/Object;)V", false);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);

        me.annotations.forEach(ae -> ae.visit(mv));

        mv.visitEnd();
    }

    @Override
    public void validate() throws RuntimeException {
        METHODS.values().stream()
                .flatMap(i -> i.values().stream())
                .forEach(SimpleValidator::validate);
    }

}
