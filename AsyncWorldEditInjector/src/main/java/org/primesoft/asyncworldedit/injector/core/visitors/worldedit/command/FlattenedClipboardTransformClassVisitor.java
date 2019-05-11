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
package org.primesoft.asyncworldedit.injector.core.visitors.worldedit.command;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.stream.Stream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.ICreateClass;
import org.primesoft.asyncworldedit.injector.injected.command.FlattenedClipboardTransformFactory;
import org.primesoft.asyncworldedit.injector.injected.command.IFlattenedClipboardTransform;
import org.primesoft.asyncworldedit.injector.injected.command.IFlattenedClipboardTransformFactory;

/**
 *
 * @author SBPrime
 */
public class FlattenedClipboardTransformClassVisitor extends BaseClassVisitor {

    private final String DESCRIPTOR_FACTORY_CLASS = "com.sk89q.worldedit.command.FlattenedClipboardTransformFactoryImpl_";

    public FlattenedClipboardTransformClassVisitor(ClassVisitor classVisitor, ICreateClass createClass) {
        super(classVisitor, createClass);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, changeVisibility(access, Opcodes.ACC_PUBLIC), name, signature, superName,
                Stream.concat(Stream.of(interfaces),
                        Stream.of(Type.getInternalName(IFlattenedClipboardTransform.class)))
                        .toArray(String[]::new));
    }

    @Override
    public void visitEnd() {
        String className = DESCRIPTOR_FACTORY_CLASS + RANDOM_PREFIX;
        
        try {
            emitFactory(className);
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to create IFlattenedClipboardTransformFactory implementation.", ex);
        }
        
        super.visitEnd();
        
        try {
            FlattenedClipboardTransformFactory.initialize((IFlattenedClipboardTransformFactory) 
                    Class.forName(className).getConstructor().newInstance());
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException |
                InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
            throw new IllegalStateException("Unable to create IFlattenedClipboardTransformFactory instance.", ex);
        }
    }

    private void emitFactory(String className) throws IOException {        
        String classDescriptor = className.replace(".", "/");

        Method mTransform = IFlattenedClipboardTransformFactory.class
                .getDeclaredMethods()[0];
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, classDescriptor,
                null, "java/lang/Object",
                new String[]{
                    Type.getInternalName(IFlattenedClipboardTransformFactory.class)
                });

        emitEmptyCtor(cw);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, mTransform.getName(),
                Type.getMethodDescriptor(mTransform), null, null);

        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, 
                "com/sk89q/worldedit/command/FlattenedClipboardTransform", "transform",
                "(Lcom/sk89q/worldedit/extent/clipboard/Clipboard;Lcom/sk89q/worldedit/math/transform/Transform;)Lcom/sk89q/worldedit/command/FlattenedClipboardTransform;",
                false);

        mv.visitInsn(Opcodes.ARETURN);

        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();

        createClass(className, cw);
    }

    @Override
    public void validate() throws RuntimeException {
    }
}
