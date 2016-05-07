package org.primesoft.asyncworldedit.utils;

/**
 * Stub for the API to compile
 * @author SBPrime
 * @param <TResult>
 * @param <TParam>
 * @param <TException>
 */
public interface FuncParamEx<TResult, TParam, TException extends Exception> {

    /**
     *
     * @param param
     * @return
     * @throws TException
     */
    TResult execute(TParam param) throws TException;
}
