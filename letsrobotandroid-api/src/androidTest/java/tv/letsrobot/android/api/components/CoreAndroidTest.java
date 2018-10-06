package tv.letsrobot.android.api.components;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;
import tv.letsrobot.android.api.Core;

/**
 * Helper test to test core functionality without an activity. Cannot test camera using this
 */
@RunWith(AndroidJUnit4.class)
public class CoreAndroidTest {
    Core core = null;
    @Test
    public void Init() throws Core.InitializationException {
        Core.Builder builder = new Core.Builder(InstrumentationRegistry.getTargetContext());
        builder.setRobotId(""); //TODO ROBOT ID
        builder.setCameraId(""); //TODO CAMERA ID
        builder.setUseTTS(true);
        core = builder.build();
        Assert.assertTrue(core.enable());
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(core.disable());
    }
}
