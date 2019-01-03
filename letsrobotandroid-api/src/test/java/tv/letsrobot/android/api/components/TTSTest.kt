package tv.letsrobot.android.api.components

import org.junit.Assert
import org.junit.Test

/**
 * Created by Brendon on 9/7/2018.
 */
class TTSTest{
    @Test
    fun testChat(){
        Assert.assertEquals("Test message", ChatSocketComponent.getMessageFromRaw("[ROBOT NAME] Test message"))
        Assert.assertEquals("Test message", ChatSocketComponent.getMessageFromRaw("[ROBOTNAME] Test message"))
        Assert.assertEquals(null, ChatSocketComponent.getMessageFromRaw("[ROBOT NAME] "))
        Assert.assertEquals(null, ChatSocketComponent.getMessageFromRaw("[ROBOT NAME]  "))
    }

    @Test
    fun testSilence(){
        val msg = ChatSocketComponent.getMessageFromRaw("[ROBOT NAME] .message")
        Assert.assertEquals(false, ChatSocketComponent.isSpeakableText(msg))
        val msg2 = ChatSocketComponent.getMessageFromRaw("[ROBOT NAME] Test message")
        Assert.assertEquals(true, ChatSocketComponent.isSpeakableText(msg2))
    }

    @Test
    fun testBrokenResponse(){
        Assert.assertEquals(null, ChatSocketComponent.getMessageFromRaw(null))
        Assert.assertEquals(null, ChatSocketComponent.getMessageFromRaw("[[[dawaowdhawuhfda"))
    }
}
