package org.primesoft.asyncworldedit.utils;

/**
 * Stub for the API to compile
 * @author SBPrime
 * @param <T> The function result type
 * @param <TException> The function exception
 */

public interface FuncEx<T, TException extends Exception> {
    T execute() throws TException;
}
