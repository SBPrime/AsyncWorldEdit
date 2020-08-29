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

import org.objectweb.asm.Type;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.primesoft.asyncworldedit.injector.utils.SimpleValidator;

/**
 *
 * @author SBPrime
 */
public class BaseFieldAccessorVisitor extends BaseClassVisitor {
    private final FieldEntry[] m_fields;
    private final Class<?> m_interface;

    protected BaseFieldAccessorVisitor(Class<?> accessInterface, FieldEntry[] fields,
            ClassVisitor classVisitor) {
        super(classVisitor);
        
        m_interface = accessInterface;
        m_fields = fields;
    }
    
    @Override
    public void validate() throws RuntimeException {
        for (FieldEntry e : m_fields) {
            e.m_validator.validate();
        }
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, injectInterface(interfaces, Type.getInternalName(m_interface)));
    }   
    
    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        for (FieldEntry e : m_fields) {
            Integer newAccess = e.visit(access, name, descriptor);
            if (newAccess != null) {
                return super.visitField(newAccess, name, descriptor, signature, value);
            }
        }
        
        return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public void visitEnd() {
        for (FieldEntry f : m_fields) {
            if (f.Getter != null) {
                visitGetter(f);
            }
            if (f.Setter != null) {
                visitSetter(f);
            }
        }
        
        super.visitEnd();
    }

    private void visitGetter(FieldEntry f) {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, f.Getter, "()" + f.Descriptor, null, null);
        mv.visitCode();
        emmitGet(mv, f);
        visitReturn(mv, f.Descriptor);
        
        mv.visitMaxs(1, 1);
        mv.visitEnd();

    }

    protected void emmitGet(final MethodVisitor mv, final FieldEntry f) {
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitFieldInsn(Opcodes.GETFIELD, m_cls, f.Name, f.Descriptor);
    }

    private void visitSetter(FieldEntry f) {
        MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC, f.Setter, "(" + f.Descriptor + ")V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        visitArgumemt(mv, f.Descriptor, 1);
        mv.visitFieldInsn(Opcodes.PUTFIELD, m_cls, f.Name, f.Descriptor);
        mv.visitInsn(Opcodes.RETURN);
        
        mv.visitMaxs(1, 1);
        mv.visitEnd();

    }

    public static class FinalFieldEntry extends FieldEntry {
        /**
         * Create new instance of the field entry
         * @param access The accessor pattern to match
         * @param name The field name to match
         * @param descriptor The field descriptor to match
         * @param getter The getter name in the interface, null if no getter
         * @param setter The setter name in the interface, null if not setter
         */
        public FinalFieldEntry(int access, String name, String descriptor,
                          String getter, String setter) {
            this(access, name, descriptor, i -> i, getter, setter);
        }

        /**
         * Create new instance of the field entry
         * @param access The accessor pattern to match
         * @param name The field name to match
         * @param descriptor The field descriptor to match
         * @param getter The getter name in the interface, null if no getter
         * @param setter The setter name in the interface, null if not setter
         * @param ac Function to change the accessor
         */
        public FinalFieldEntry(int access, String name, String descriptor,
                          AccessChanger ac, String getter, String setter) {
            super(access | Opcodes.ACC_FINAL, name, descriptor, i -> ac.process(i) & (~Opcodes.ACC_FINAL), getter, setter);
        }
    }

    public static class FieldEntry {
        public final int Access;
        public final String Name;
        public final String Descriptor;        
        public final String Getter;
        public final String Setter;
        
        private final SimpleValidator m_validator;
        private final AccessChanger m_accessChanger;

        /**
         * Create new instance of the field entry
         * @param access The accessor pattern to match
         * @param name The field name to match
         * @param descriptor The field descriptor to match
         * @param getter The getter name in the interface, null if no getter
         * @param setter The setter name in the interface, null if not setter
         */
        public FieldEntry(int access, String name, String descriptor,
                String getter, String setter) {
            this(access, name, descriptor, i -> i, getter, setter);
        }

        /**
         * Create new instance of the field entry
         * @param access The accessor pattern to match
         * @param name The field name to match
         * @param descriptor The field descriptor to match
         * @param getter The getter name in the interface, null if no getter
         * @param setter The setter name in the interface, null if not setter
         * @param ac Function to change the accessor
         */
        public FieldEntry(int access, String name, String descriptor,
                AccessChanger ac, String getter, String setter) {
            Access = access;
            Name = name;
            Descriptor = descriptor;
            Getter = getter;
            Setter = setter;
            
            m_validator = new SimpleValidator("Field '" + name +":" + descriptor +"' not found.");
            m_accessChanger = ac;            
        }

        private Integer visit(int access, String name, String descriptor) {
            if (Name.equals(name) && Descriptor.equals(descriptor) && Access == access) {
                m_validator.set();
                
                return m_accessChanger.process(access);
            }
            
            return null;
        }
    }
    
    @FunctionalInterface
    protected interface AccessChanger {
        int process(int current);
    }
}
