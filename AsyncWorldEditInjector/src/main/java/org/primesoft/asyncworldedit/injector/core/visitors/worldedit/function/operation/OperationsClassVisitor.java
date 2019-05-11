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

package org.primesoft.asyncworldedit.injector.core.visitors.worldedit.function.operation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.primesoft.asyncworldedit.injector.core.visitors.BaseClassVisitor;
import org.primesoft.asyncworldedit.injector.core.visitors.ICreateClass;
import org.primesoft.asyncworldedit.injector.utils.MethodEntry;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

/**
 *
 * @author SBPrime
 */
public final class OperationsClassVisitor extends BaseClassVisitor {

    private final static String DESCRIPTOR_CLASS = "com/sk89q/worldedit/function/operation/Operations";
    private final static String DESCRIPTOR_CLASS_INNER = "com/sk89q/worldedit/function/operation/Operations_InnerForMethod";
    private final static String DESCRIPTOR_COMPLETEx = "(Lcom/sk89q/worldedit/function/operation/Operation;)V";

    private final static String METHOD_COMPLETEx = "complete.*";

    private final List<MethodEntry> m_methodsToWrap = new ArrayList<>();
    
    private final SimpleValidator m_vMethod = new SimpleValidator("No methods found");

    public OperationsClassVisitor(ClassVisitor classVisitor, ICreateClass createClass) {
        super(classVisitor, createClass);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isStatic(access) && name.matches(METHOD_COMPLETEx)) {
            m_methodsToWrap.add(new MethodEntry(access, name, descriptor, signature, exceptions));
            m_vMethod.set();
            
            return cv.visitMethod(changeVisibility(access, Opcodes.ACC_PUBLIC),
                    RANDOM_PREFIX + name, descriptor, signature, exceptions);
        }

        return super.visitMethod(access, name, descriptor, signature, exceptions);
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

    private void emitInnerClass(MethodEntry me) throws IOException {
        if (me.exceptions != null && me.exceptions.length > 1) {
            throw new IllegalArgumentException("Expected one or non exceptions.");
        }
        
        String className = getMethodClassName(me);
        String exception = me.exceptions == null || me.exceptions.length == 0 ? null : me.exceptions[0];
        String signature = null;

        if (exception != null) {
            signature = "Lorg/primesoft/asyncworldedit/injector/utils/ExceptionOperationAction<L" + exception + ";>;";
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_8, Opcodes.ACC_STATIC | Opcodes.ACC_PRIVATE, className,
                signature, "java/lang/Object",
                new String[]{
                    "org/primesoft/asyncworldedit/injector/utils/"
                    + (exception == null ? "OperationAction" : "ExceptionOperationAction")
                });
        emitEmptyCtor(cw);
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "execute", "(Lcom/sk89q/worldedit/function/operation/Operation;)V", null,
                exception == null ? null : new String[]{
                    exception
                });

        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, DESCRIPTOR_CLASS, RANDOM_PREFIX + me.name, me.descriptor, false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        cw.visitEnd();
        createClass(className.replace("/", "."), cw);
    }

    private String getMethodClassName(MethodEntry me) {
        return DESCRIPTOR_CLASS_INNER + RANDOM_PREFIX + me.name;
    }

    private void emitMethod(MethodEntry me) {
        MethodVisitor mv = cv.visitMethod(me.access, me.name, me.descriptor, me.signature, me.exceptions);

        //mv.visitCode();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        String methodClassName = getMethodClassName(me);
        String exception = me.exceptions == null || me.exceptions.length == 0 ? null : me.exceptions[0];
        
        mv.visitTypeInsn(Opcodes.NEW, methodClassName);        
        
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, methodClassName, "<init>", "()V", false);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "org/primesoft/asyncworldedit/injector/core/visitors/Helpers", 
                exception != null ? "executeMethodEx" : "executeMethod", 
                "(Lcom/sk89q/worldedit/function/operation/Operation;Lorg/primesoft/asyncworldedit/injector/utils/"
                + (exception == null ? "OperationAction" : "ExceptionOperationAction") + ";)V", false);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(2, 1);
        
        mv.visitEnd();
    }
    
    @Override
    public void validate() throws RuntimeException {
        m_vMethod.validate();
    }
}