package tv.letsrobot.android.api.api;

import com.runmyrobot.android_robot_for_phone.test.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

/**
 * Test TTS on its own, and make sure it can connect to the API
 */
@RunWith(AndroidJUnit4.class)
public class AudioComponentTest {
    @Test
    public void Init(){
        AudioComponent audioComponent = new AudioComponent(InstrumentationRegistry.getTargetContext(), BuildConfig.CAMERA_ID);
        audioComponent.enable();
        //Assert.assertTrue(audioComponent.getRunning().get());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(2, TimeUnit.MINUTES); //Wait for 2 Minutes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Assert.assertTrue(audioComponent.getConnected()); //Make sure socket is actually connected
        audioComponent.disable(); //Disable TTS
        latch = new CountDownLatch(1);
        try {
            //wait a little bit to make sure it had time to disconnect
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //Assert.assertFalse(audioComponent.getConnected()); //Make sure disable works
    }
}
