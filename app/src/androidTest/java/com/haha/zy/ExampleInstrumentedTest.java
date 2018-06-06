package com.haha.zy;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.haha.zy", appContext.getPackageName());


        String fileName = "A-Lin - 给我一个理由";
        if (fileName.contains("-")) {
            //String regex = "\\s*-\\s*";
            int splitIndex = fileName.lastIndexOf("-");
            String artistName = fileName.substring(0, splitIndex).trim();
            String audioName = fileName.substring(splitIndex, fileName.length()).trim();

            System.out.print(artistName + " :: " + audioName);
        }
    }

}
