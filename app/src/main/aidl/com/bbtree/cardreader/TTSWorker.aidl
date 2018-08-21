// TTSWoker.aidl
package com.bbtree.cardreader;

// Declare any non-default types here with import statements

interface TTSWorker {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void readText(int speaker,String text);
}
