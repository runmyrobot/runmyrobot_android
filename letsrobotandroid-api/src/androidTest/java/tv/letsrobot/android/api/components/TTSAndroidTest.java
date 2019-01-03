package tv.letsrobot.android.api.components;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import tv.letsrobot.android.api.enums.ComponentStatus;

/**
 * Test TTS on its own, and make sure it can connect to the API
 */
@RunWith(AndroidJUnit4.class)
public class TTSAndroidTest {
    @Test
    public void Init(){
        ChatSocketComponent chatSocketComponent
                = new ChatSocketComponent(InstrumentationRegistry.getTargetContext(), ""); //TODO ROBOTID
        chatSocketComponent.enable();
        Assert.assertEquals(ComponentStatus.DISABLED, chatSocketComponent.getStatus());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(2, TimeUnit.MINUTES); //Wait for 2 Minutes
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(chatSocketComponent.getConnected()); //Make sure socket is actually connected
        chatSocketComponent.disable(); //Disable TTS
        latch = new CountDownLatch(1);
        try {
            //wait a little bit to make sure it had time to disconnect
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertFalse(chatSocketComponent.getConnected()); //Make sure disable works
    }
}
