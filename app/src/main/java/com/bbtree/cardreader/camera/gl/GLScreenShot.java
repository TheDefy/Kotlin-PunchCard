package com.bbtree.cardreader.camera.gl;


/**
 * Function:
 * Created by BBTree Team
 * Author: EngrZhou
 * Create Date: 2015/09/21
 * Create Time: 下午9:52
 */
public class GLScreenShot {
    private static GLScreenShot ourInstance = new GLScreenShot();

    private GLScreenShot() {
    }

    public static GLScreenShot getInstance() {
        return ourInstance;
    }

}
