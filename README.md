[![Maintainability](https://api.codeclimate.com/v1/badges/b6b7eb90dfe3d77bb5f0/maintainability)](https://codeclimate.com/github/btelman96/runmyrobot_android/maintainability)

# LetsRobotAndroid-ControlSDK
android code to run on robots via LetsRobot api

# Development

## Required software

### Android Studio

- Version 3.2 or higher. May not be buildable on lower versions

## Branches

### master
 
The most stable code in the repo. Can be used for testing and is known to work

### devel

The latest code, mostly stable, but might have issues. Sometimes this code may not be buildable

## Device Limitations

- Setup to be capable of running on Android 4.1 (API 16) or higher. Some devices may run into issues, so feel free to report them

- Android Things not tested

- Raspberry PI not tested

# Setup

## Robot and Camera Ids

When the app is started, and permissions have been accepted (API > 23), a setup screen will pop up
with settings that can be changed. RobotID is the only id required to get it connected to the API.

### User Configurable Settings

- RobotID

- Enable Camera Toggle

- CameraID (Disabled if enable camera is false)

- Camera Password (Disabled if enable camera is false)

- Resolution (Disabled if enable camera is false)

- Enable Microphone Toggle

- Text to speech toggle

- Error reporting toggle (Disabled for now)

- Fake black screen to save power (Not really needed that much now that the screen can be turned off)

- Bluetooth device setup

- Scanning a QR Code that was exported with this app.

## Running the robot

### To make the robot operational and connected:

 1. Click build and run to deploy to phone (Play button)
 2. Open App if not opened already
 3. Accept permissions if they pop up
 4. Configure robot settings
 5. Hit POWER - Button will be disabled until fully connected
 6. Robot will now be connected to letsrobot website. POWER button will be green
 
### Supported Commands ###

#### Table mode ####
Only the owner can use this.
Turns on table top mode, not allowing 'f' and 'b' commands, but allows everything else. Not case sensitive

On: '.table on'

Off: '.table off'

#### Disable All Control ####
Only the owner can use this.
Disables all commands, even for the owner.

Disable: '.motors off'
Enable: '.motors on'

#### Battery Level ####
Only the owner can use this.
'.battery level'

Robot says the phone battery level
'Internal battery 80%'

#### TTS Language ####
'.locale en-US'
Sets the locale of the TTS (System TTS Only)

Language must be installed for it to speak using it. This only changes the accent, and does not translate.
 
### Ways to stop the robot:
 
 - A notification will appear that states that the app is active. Use the "Terminate App" Button to 
 kill the app from anywhere
 
 - Hit the POWER button again when it is green to disable
 
 - Swipe app away from recents

### App Permissions ###

#### Location ####

Needed for bluetooth to function. In the future, this will not prompt if not needed

#### Camera ####

Needed to stream the camera

#### Microphone ####

Needed to stream the microphone

#### Storage ####

Not used right now

#### Phone ####

Not used, and will not be prompted for it

### Troubleshooting issues

#### Flickering Camera and Microphone indicators

Reload the robot page on LetsRobot.tv

Also check that the robotId, cameraId, and cameraPass match with the site. 
If your camera password on the site is empty, your password is "hello"

#### Most indicators immediately go red

Check the phone's internet connection

#### Most indicators go red after being yellow for some time

- Check the phone's internet connection.
 
- Connection may be too slow or connected to a WiFi router with no internet.

- Also could potentially be a site issue, but most of the time it would be internet related

#### Robot turns off after some time if I turn the screen off ####
This only applies to Android 6.0 and above

- Go to the app settings and turn battery optimization off for this app

## Adding separate components

Make a class that extends and implements methods of Component

Then add to the Builder's externalComponents list

See CustomComponentExample.kt
See MainRobotController.kt for an example of CustomComponentExample being added

### Devices supported
 
 - SaberTooth Motor Controllers (Simplified Serial), 9600 BAUD
 - Arduino via raw commands (f, b, l, r, stop, etc), 9600 BAUD, USB or Bluetooth Classic
 - Any device that has the same protocol as SaberTooth
 - Lego Mindstorms NXT (JoystickDriver or equivalent. Most commonly used with older FIRST Tech Challenge robots that ran RobotC and Labview)
 
### Connection Options

 - Bluetooth Classic (less than 4.0 guaranteed), HC04 would work. Please pair the bluetooth device in settings first, then setup in this app
 - USB Serial (Not working on Android Things 1.0.3 rpi3) (https://github.com/felHR85/UsbSerial#devices-supported)

## Error reporting

Errors do not get reported at the moment

# Some known issues

- Battery optimization has to be disabled if OS version is 6.0 or above if you want to turn the screen off on the phone.

- Currently no code to use a USB webcam. Also currently not sure if the Raspberry Pi with camera functions as is

- BluetoothClassic currently not hooked up to handle input from the connected device. It can output to it just fine

# Supported or broken devices

Feel free to add your device to this list if you have tested it via a pull request

## Broken devices:

-
-

## Verified functional devices:

- Casio G'zOne CA-201L (4.1.2 JellyBean). Tested with 512kbps bitrate and bluetooth. Might not support USB OTG

- ZTE Speed (4.4 Kitkat), might not be fast enough on high bitrates, some weird bluetooth issues possible (https://github.com/btelman96/runmyrobot_android/issues/45)

- Motorola Moto Z (8.0.0 Oreo)

- Galaxy S4 (5.0.1 Lollipop)

- Pixel 2 XL (9 Pie)





