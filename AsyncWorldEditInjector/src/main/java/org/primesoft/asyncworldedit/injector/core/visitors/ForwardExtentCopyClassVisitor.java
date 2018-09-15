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

import java.util.stream.Stream;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.injected.function.operation.IForwardExtentCopy;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

/**
 *
 * @author SBPrime
 */
public class ForwardExtentCopyClassVisitor extends BaseClassVisitor {

    private final static String CLASS_DESCRIPTOR = "com/sk89q/worldedit/function/operation/ForwardExtentCopy";
    private final static String CLASS_EXTENT_BLOCK_COPY = "com/sk89q/worldedit/function/block/ExtentBlockCopy";

    private final static String FIELD_NAME = "m_copyBiome";

    private final static String METHOD_NAME = "resume";
    private final static String METHOD_DESCRIPTOR = "(Lcom/sk89q/worldedit/function/operation/RunContext;)Lcom/sk89q/worldedit/function/operation/Operation;";

    private final SimpleValidator m_vMethod = new SimpleValidator("Method resume not found");
    
    private static class ResumeMethodVisitor extends MethodVisitor {
        public ResumeMethodVisitor(int api, MethodVisitor mv) {
            super(api, mv);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (opcode != Opcodes.INVOKESPECIAL || !"<init>".equals(name)
                    || !"com/sk89q/worldedit/function/block/ExtentBlockCopy".equals(owner)) {                
                return;
            }

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_DESCRIPTOR, "source", "Lcom/sk89q/worldedit/extent/Extent;");
            
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_DESCRIPTOR, "from", "Lcom/sk89q/worldedit/Vector;");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_DESCRIPTOR, "destination", "Lcom/sk89q/worldedit/extent/Extent;");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_DESCRIPTOR, "to", "Lcom/sk89q/worldedit/Vector;");
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_DESCRIPTOR, "currentTransform", "Lcom/sk89q/worldedit/math/transform/Transform;");

            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                    Type.getInternalName(Helpers.class),
                    "addBiomeCopy",
                    Type.getMethodDescriptor(
                            Stream.of(Helpers.class.getDeclaredMethods())
                                    .filter(i -> "addBiomeCopy".equals(i.getName()))
                                    .findFirst().get()),
                    false);
        }
    }

    public ForwardExtentCopyClassVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName,
                Stream.concat(Stream.of(interfaces),
                        Stream.of(Type.getInternalName(IForwardExtentCopy.class)))
                        .toArray(String[]::new));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (!isStatic(access) && isPublic(access)
                && METHOD_NAME.equalsIgnoreCase(name) && METHOD_DESCRIPTOR.equalsIgnoreCase(descriptor)) {
            
            m_vMethod.set();
            return new ResumeMethodVisitor(api, mv);
        }

        return mv;
    }

    @Override
    public void visitEnd() {
        cv.visitField(Opcodes.ACC_PRIVATE, FIELD_NAME, Type.getDescriptor(boolean.class), null, false)
                .visitEnd();

        //public void setBiomeCopy(boolean);
        MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "setBiomeCopy", "(Z)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, CLASS_DESCRIPTOR, FIELD_NAME, Type.getDescriptor(boolean.class));
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        //public boolean isBiomeCopy();
        mv = cv.visitMethod(Opcodes.ACC_PUBLIC, "isBiomeCopy", "()Z", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, CLASS_DESCRIPTOR, FIELD_NAME, Type.getDescriptor(boolean.class));
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        super.visitEnd();
    }
    
    @Override
    public void validate() throws RuntimeException {
        m_vMethod.validate();
    }
}
