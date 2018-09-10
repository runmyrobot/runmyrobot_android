package com.runmyrobot.android_robot_for_phone.control

/**
 * Created by Brendon on 9/9/2018.
 */
interface CommunicationInterface{
    fun enable()
    fun disable()
    fun isConnected() : Boolean
    fun send(byteArray: ByteArray) : Boolean
}
