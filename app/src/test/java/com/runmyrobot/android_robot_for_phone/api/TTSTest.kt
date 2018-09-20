package com.runmyrobot.android_robot_for_phone.api

import org.junit.Assert
import org.junit.Test

/**
 * Created by Brendon on 9/7/2018.
 */
class TTSTest{
    @Test
    fun testChat(){
        Assert.assertEquals("Test message", TextToSpeechComponent.getMessageFromRaw("[ROBOT NAME] Test message"))
        Assert.assertEquals("Test message", TextToSpeechComponent.getMessageFromRaw("[ROBOTNAME] Test message"))
        Assert.assertEquals(null, TextToSpeechComponent.getMessageFromRaw("[ROBOT NAME] "))
        Assert.assertEquals(null, TextToSpeechComponent.getMessageFromRaw("[ROBOT NAME]  "))
    }

    @Test
    fun testSilence(){
        Assert.assertEquals(null, TextToSpeechComponent.getMessageFromRaw("[ROBOT NAME] .message"))
    }

    @Test
    fun testBrokenResponse(){
        Assert.assertEquals(null, TextToSpeechComponent.getMessageFromRaw(null))
        Assert.assertEquals(null, TextToSpeechComponent.getMessageFromRaw("[[[dawaowdhawuhfda"))
    }
}
