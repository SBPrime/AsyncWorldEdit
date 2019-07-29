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
package org.primesoft.asyncworldedit.injector.core.visitors.worldedit.util.collection;

import java.util.Map;
import org.objectweb.asm.ClassVisitor;
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
public class LocatedBlockListVisitor extends BaseClassVisitor {
    private final SimpleValidator m_getMethod = new SimpleValidator("get not found");
    private String m_getSignature;
    private String[] m_getExceptions;
    private String m_cls;
    
    public LocatedBlockListVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        
        m_cls = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if ("get".equals(name) && 
            "(Lcom/sk89q/worldedit/math/BlockVector3;)Lcom/sk89q/worldedit/world/block/BaseBlock;".equals(descriptor)) {
            
            m_getSignature = signature;
            m_getExceptions = exceptions;
            m_getMethod.set();
            
            return null;
        }
        
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        overrideGet();
        
        super.visitEnd();
    }
       
    
    
    @Override
    public void validate() throws RuntimeException {
        m_getMethod.validate();
    }

    private void overrideGet() {
        MethodVisitor mv = super.visitMethod(
                Opcodes.ACC_PUBLIC, "get", "(Lcom/sk89q/worldedit/math/BlockVector3;)Lcom/sk89q/worldedit/world/block/BaseBlock;", 
                m_getSignature, m_getExceptions);
        mv.visitCode();
        
        // LocatedBlock tmp = this.map.get(p1)
        mv.visitIntInsn(Opcodes.ALOAD, 0);        
        String descriptorMap = Type.getDescriptor(Map.class);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_cls, "map", descriptorMap);
        mv.visitIntInsn(Opcodes.ALOAD, 1);
        mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, Type.getInternalName(Map.class), "get", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        mv.visitTypeInsn(Opcodes.CHECKCAST, "com/sk89q/worldedit/util/LocatedBlock");
        mv.visitInsn(Opcodes.DUP);

        Label lEnd = new Label();
        Label lNull = new Label();
        
        // if (tmp == null) then goto Null
        mv.visitJumpInsn(Opcodes.IFNULL, lNull);

        //
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/sk89q/worldedit/util/LocatedBlock", "getBlock", 
                "()Lcom/sk89q/worldedit/world/block/BaseBlock;", false);
        
        mv.visitJumpInsn(Opcodes.GOTO, lEnd);
        
        // Null:
        mv.visitLabel(lNull);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.ACONST_NULL);
        
        // End:
        mv.visitLabel(lEnd);
        mv.visitInsn(Opcodes.ARETURN);
        
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }
    
}
