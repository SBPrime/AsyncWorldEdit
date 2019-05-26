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
package org.primesoft.asyncworldedit.injector.core.spigot.v1_13_Rx.visitors;

import java.util.Map;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

/**
 *
 * @author SBPrime
 */
public class ChunkProviderServerVisitor extends BaseClassVisitor {

    private final SimpleValidator m_vFieldChunks = new SimpleValidator("Fields chunks not found");
    private final SimpleValidator m_vMethodGetChunkAt = new SimpleValidator("Method getChunkAt not found");

    private String m_cls;

    private String m_chunksDescriptor;

    private String m_getChunkAtDescriptor;
    private String m_getChunkAtSignature;
    private String[] m_getChunkAtExceptions;

    public ChunkProviderServerVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void validate() throws RuntimeException {
        m_vFieldChunks.validate();
        m_vMethodGetChunkAt.validate();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        m_cls = name;

        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        if (F_CHUNKS.equals(name)) {
            m_chunksDescriptor = descriptor;
            m_vFieldChunks.set();
        }

        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isCtor(name)) {
            return new CtorMethodVisitor(api,
                    super.visitMethod(access, name, descriptor, signature, exceptions));
        }

        if ("getChunkAt".equals(name)) {
            m_vMethodGetChunkAt.set();
            m_getChunkAtDescriptor = descriptor;
            m_getChunkAtSignature = signature;
            m_getChunkAtExceptions = exceptions;

            return new CtorMethodVisitor(api,
                    super.visitMethod(access, RANDOM_PREFIX + name, descriptor, signature, exceptions));
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    private final Thread m_tmp = Thread.currentThread();

    private Object m_foo;

    public Object ___tmp(int x, int z, boolean f1, boolean f2) {
        return null;
    }

    public Object tmp(int x, int z, boolean f1, boolean f2) {
        if (Thread.currentThread() == m_tmp) {
            return ___tmp(x, z, f1, f2);
        }

        Object result = ((Map<Long, Object>) m_foo).get((long) x & 4294967295L | ((long) z & 4294967295L) << 32);
        if (result == null) {
            throw new IllegalStateException("[AWE] Chunk not found for " + x + "," + z + ". Loading from async thread is not supported.");
        }

        return result;
    }

    @Override
    public void visitEnd() {
        super.visitField(Opcodes.ACC_PRIVATE, "m_serverThread", Type.getDescriptor(Thread.class), null, null).visitEnd();

        visitGetChunkAtMethod();

        super.visitEnd();
    }

    private void visitGetChunkAtMethod() {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, "getChunkAt", m_getChunkAtDescriptor, m_getChunkAtSignature, m_getChunkAtExceptions);
        mv.visitCode();

        String result = Type.getType(getResult(m_getChunkAtDescriptor)).getInternalName();
        
        Label lNotMainThread = new Label();
        Label lResultNull = new Label();

        // if (Thread.currentThread() == m_serverThread) {
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Thread.class), "currentThread", "()Ljava/lang/Thread;", false);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_cls, "m_serverThread", Type.getDescriptor(Thread.class));
        mv.visitJumpInsn(Opcodes.IF_ACMPNE, lNotMainThread);

        //  return getChunkAt(...);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        String[] args = getArgs(m_getChunkAtDescriptor);
        for (int i = 0; i < args.length; i++) {
            visitArgumemt(mv, args[i], i + 1);
        }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, m_cls, RANDOM_PREFIX + "getChunkAt", m_getChunkAtDescriptor, false);
        mv.visitInsn(Opcodes.ARETURN);

        // }
        mv.visitLabel(lNotMainThread);

        // Object tmp = ((Map)chunks).get((Long)(((long)i) & 0xFFFFFFFFl | (((long)i) & 0xFFFFFFFFl) << 32));
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_cls, F_CHUNKS, m_chunksDescriptor);
        mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(Map.class));

        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitInsn(Opcodes.I2L);
        mv.visitLdcInsn(0xFFFFFFFFl);
        mv.visitInsn(Opcodes.LAND);

        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitInsn(Opcodes.I2L);
        mv.visitLdcInsn(0xFFFFFFFFl);
        mv.visitInsn(Opcodes.LAND);
        mv.visitIntInsn(Opcodes.BIPUSH, 32);

        mv.visitInsn(Opcodes.LSHL);
        mv.visitInsn(Opcodes.LOR);

        mv.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Long.class), "valueOf", "(J)Ljava/lang/Long;", false);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, result);
        mv.visitVarInsn(Opcodes.ASTORE, args.length + 1);

        // if (tmp != null) {
        mv.visitVarInsn(Opcodes.ALOAD, args.length + 1);
        mv.visitJumpInsn(Opcodes.IFNULL, lResultNull);
        
        // return (Chunk)tmp;
        mv.visitVarInsn(Opcodes.ALOAD, args.length + 1);        
        mv.visitInsn(Opcodes.ARETURN);
        
        // }        
        mv.visitLabel(lResultNull);
        
        // throw new IllegalStateException("[AWE] Chunk not found for [" + i + "," + j + "]. Loading from async thread is not supported.");
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(IllegalStateException.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(StringBuilder.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(StringBuilder.class), METHOD_CTOR, "()V", false);
        mv.visitLdcInsn("[AWE] Chunk not found for [");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(Opcodes.ILOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(I)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(",");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitVarInsn(Opcodes.ILOAD, 2);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(I)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn("]. Loading from async thread is not supported.");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Type.getInternalName(StringBuilder.class), "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(IllegalStateException.class), METHOD_CTOR, "(Ljava/lang/String;)V", false);
        mv.visitInsn(Opcodes.ATHROW);

        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    private static final String F_CHUNKS = "chunks";

    private class CtorMethodVisitor extends MethodVisitor {

        public CtorMethodVisitor(int api, MethodVisitor methodVisitor) {
            super(api, methodVisitor);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);

            if (!isCtor(name) || !Type.getInternalName(Object.class).equals(owner)) {
                return;
            }

            super.visitVarInsn(Opcodes.ALOAD, 0);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(Thread.class), "currentThread", "()Ljava/lang/Thread;", false);
            super.visitFieldInsn(Opcodes.PUTFIELD, m_cls, "m_serverThread", Type.getDescriptor(Thread.class));
        }
    }
}
