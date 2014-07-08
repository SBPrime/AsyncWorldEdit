/*
 * The MIT License
 *
 * Copyright 2014 SBPrime.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.primesoft.asyncworldedit.worldedit.utils.command;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.CommandMapping;
import com.sk89q.worldedit.util.command.Description;
import com.sk89q.worldedit.util.command.Dispatcher;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.primesoft.asyncworldedit.utils.Reflection;
import org.primesoft.asyncworldedit.worldedit.entity.PlayerWrapper;

/**
 *
 * @author SBPrime
 */
public class DispatcherWrapper implements Dispatcher {
    private final Dispatcher m_parent;
    
    public final Dispatcher getParent(){
        return m_parent;
    }
    
    public DispatcherWrapper(Dispatcher parent){
        m_parent = parent;
    }
    
    @Override
    public void registerCommand(CommandCallable callable, String... alias) {
        m_parent.registerCommand(callable, alias);
    }

    @Override
    public Set<CommandMapping> getCommands() {
        return m_parent.getCommands();
    }

    @Override
    public Collection<String> getPrimaryAliases() {
        return m_parent.getPrimaryAliases();
    }

    @Override
    public Collection<String> getAliases() {
        return m_parent.getAliases();
    }

    @Override
    public CommandMapping get(String alias) {
        return m_parent.get(alias);
    }

    @Override
    public boolean contains(String alias) {
        return m_parent.contains(alias);
    }

    @Override
    public boolean call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException {        
        Map<Object, Object> valuesMap = Reflection.get(locals, Map.class, "locals", "Unable to get locals, player not injected.");
        Map.Entry<Object, Object>[] values = valuesMap.entrySet().toArray(new Map.Entry[0]);
        if (values != null) {
            for (Map.Entry<Object, Object> entry : values) {
                Object key = entry.getKey();
                Object v = entry.getValue();
                
                if (v instanceof Player){
                    valuesMap.remove(key);
                    valuesMap.put(key, new PlayerWrapper((Player)v));
                }
            }
        }
        
        return m_parent.call(arguments, locals, parentCommands);
    }

    @Override
    public Description getDescription() {
        return m_parent.getDescription();
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return m_parent.testPermission(locals);
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        return m_parent.getSuggestions(arguments, locals);
    }
    
}
