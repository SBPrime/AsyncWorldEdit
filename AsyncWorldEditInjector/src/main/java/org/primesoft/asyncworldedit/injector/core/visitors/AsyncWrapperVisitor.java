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

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.injected.IAsyncWrapper;

/**
 *
 * @author SBPrime
 */
public class AsyncWrapperVisitor extends BaseClassVisitor {

    private final static String CLS_IASYNC_WRAPPER = Type.getInternalName(IAsyncWrapper.class);

    private final static String D_PLAYER_ENTRY = "Lorg/primesoft/asyncworldedit/api/playerManager/IPlayerEntry;";
    private final static String D_ASYNC_DATA = "Lorg/primesoft/asyncworldedit/injector/injected/IAsyncData;";

    private boolean isClass;

    public AsyncWrapperVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        isClass = (access & Opcodes.ACC_INTERFACE) != Opcodes.ACC_INTERFACE;
        
        super.visit(version, access, name, signature, superName, injectInterface(interfaces, CLS_IASYNC_WRAPPER));
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (isStatic(access)) {
            return mv;
        }

        final String result = Type.getReturnType(descriptor).getClassName().replace(".", "/");

        return new MethodVisitor(api, mv) {
            @Override
            public void visitInsn(int opcode) {
                if (opcode == Opcodes.ARETURN) {
                    super.visitVarInsn(Opcodes.ALOAD, 0);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC,
                                Type.getInternalName(Helpers.class),
                                "wrapResult",
                                "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                                false);
                    mv.visitTypeInsn(Opcodes.CHECKCAST, result);
                }
                super.visitInsn(opcode);
            }

        };
    }

    @Override
    public void visitEnd() {
        if (isClass) {
            super.visitField(Opcodes.ACC_PRIVATE, "m_asyncData", D_ASYNC_DATA, null, null);
            super.visitField(Opcodes.ACC_PRIVATE, "m_isAsync", "Z", null, null);
            super.visitField(Opcodes.ACC_PRIVATE, "m_jobId", "I", null, null);
            super.visitField(Opcodes.ACC_PRIVATE, "m_player", D_PLAYER_ENTRY, null, null);

            visitMethod("getAsyncData", "m_asyncData", D_ASYNC_DATA);
            visitMethod("isAsync", "m_isAsync", "Z");
            visitMethod("getJobId", "m_jobId", "I");
            visitMethod("getPlayer", "m_player", D_PLAYER_ENTRY);

            visitInitializeMethod();
        }

        super.visitEnd();
    }

    private void visitInitializeMethod() {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "initializeAsyncWrapper", "(IZLorg/primesoft/asyncworldedit/api/playerManager/IPlayerEntry;)V", null, null);
        mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        visitArgumemt(mv, "I", 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, "m_jobId", "I");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        visitArgumemt(mv, "Z", 2);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, "m_isAsync", "Z");

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        visitArgumemt(mv, D_PLAYER_ENTRY, 3);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, "m_player", D_PLAYER_ENTRY);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, "m_asyncData", D_ASYNC_DATA);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    private void visitMethod(final String methodName, final String fieldName, final String descriptor) {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, methodName, "()" + descriptor, null, null);

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_cls, fieldName, descriptor);
        super.visitReturn(mv, descriptor);

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    @Override
    public void validate() throws RuntimeException {
    }
}
