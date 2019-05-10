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
package org.primesoft.asyncworldedit.injector.core.visitors;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.world.World;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.primesoft.asyncworldedit.injector.injected.entity.WrappedPlayerData;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

/**
 *
 * @author SBPrime
 */
public class WrapGetWorldVisitor extends BaseClassVisitor {

    private static final String GETWORLD_NAME = "getWorld";

    private static final String GETWORLD_DESCRIPTOR = "()Lcom/sk89q/worldedit/world/World;";

    private static final String DESCRIPTOR_WRAPPER_DATA = Type.getDescriptor(WrappedPlayerData.class);

    private final SimpleValidator m_vGetWorld = new SimpleValidator("Get world not proxied");
    private String m_className;

    public WrapGetWorldVisitor(ClassVisitor classVisitor) {
        super(classVisitor);
    }

    @Override
    public void validate() throws RuntimeException {
        m_vGetWorld.validate();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);

        m_className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (GETWORLD_NAME.equals(name) && GETWORLD_DESCRIPTOR.equals(descriptor)) {
            m_vGetWorld.set();
            return super.visitMethod(access, RANDOM_PREFIX + name, descriptor, signature, exceptions);
        }
        
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        super.visitField(Opcodes.ACC_PRIVATE, "m_data", DESCRIPTOR_WRAPPER_DATA, null, null)
                .visitEnd();

        final MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC,
                GETWORLD_NAME, GETWORLD_DESCRIPTOR, null, new String[0]);
        
        final Label lDataNotNull = new Label();

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_className, "m_data", DESCRIPTOR_WRAPPER_DATA);
        mv.visitJumpInsn(Opcodes.IFNONNULL, lDataNotNull);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitTypeInsn(Opcodes.NEW, Type.getInternalName(WrappedPlayerData.class));
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                Type.getInternalName(WrappedPlayerData.class),
                "<init>",
                "()V",
                false);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_className, "m_data",
                DESCRIPTOR_WRAPPER_DATA);
        
        mv.visitLabel(lDataNotNull);
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_className, "m_data", DESCRIPTOR_WRAPPER_DATA);
        mv.visitVarInsn(Opcodes.ALOAD, 0);

        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                m_className,
                RANDOM_PREFIX + GETWORLD_NAME, GETWORLD_DESCRIPTOR,
                false);
        
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                Type.getInternalName(WrappedPlayerData.class),
                "getWorld",
                "("
                + Type.getDescriptor(Player.class)
                + Type.getDescriptor(World.class)
                + ")" + Type.getDescriptor(World.class),
                false);

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitCode();

        mv.visitMaxs(1, 1);
        mv.visitEnd();

        super.visitEnd();
    }
    
    
    WrappedPlayerData m_data;
    
    void test() {
        if (m_data == null) {
            m_data = new WrappedPlayerData();
        }
        
        m_data.getWorld(null, null);
    }
}
