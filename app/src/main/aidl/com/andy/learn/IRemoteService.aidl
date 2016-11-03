// IRemoteService.aidl
package com.andy.learn;

// Declare any non-default types here with import statements

interface IRemoteService {

    long multiply(long left, long right);
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}

