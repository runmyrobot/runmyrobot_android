[![Maintainability](https://api.codeclimate.com/v1/badges/b6b7eb90dfe3d77bb5f0/maintainability)](https://codeclimate.com/github/btelman96/runmyrobot_android/maintainability)

# runmyrobot_android
android code to run on robots via LetsRobot api

# Development

## Required software

### Android Studio

- Version 3.2 or higher. May not be buildable on lower versions

## Device Limitations

- Setup to be capable of running on Android 4.4 (API 19) or higher

- App will build to a device with 4.1.2 if minSDKVersion is lowered to 16, but functionality has not been fully tested.

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

- Error reporting toggle (Logs to a error reporting website if the app crashes)

- Bluetooth device setup

## Running the robot

To make the robot operational and connected:
 1. Click build and run to deploy to phone (Play button)
 2. Open App if not opened already
 3. Accept permissions if they pop up
 4. Configure robot settings
 5. Hit Enable
 6. Robot will now be connected to letsrobot website

## Adding separate components

Make a class that extends and implements methods of Component

Then add to the Builder's externalComponents list

See CustomComponentExample.kt
See MainRobotController.kt for an example of CustomComponentExample being added

### Devices supported
 
 - SaberTooth Motor Controllers (Simplified Serial), 9600 BAUD
 - Arduino via raw commands (f, b, l, r, stop, etc), 9600 BAUD
 - Any device that has the same protocol as SaberTooth
 
### Connection Options

 - Bluetooth Classic (less than 4.0 guaranteed), HC04 would work
 - USB Serial (Not working on Android Things 1.0.3 rpi3) (https://github.com/felHR85/UsbSerial#devices-supported)

## Error reporting

Errors will be reported to Bugsnag (In the future it may automatically create issues on github)
This is completely optional and will be disabled unless enabled through settings

# Some known issues

- Camera streaming will only work if the app has not been backgrounded (turning screen off is fine if no lock screen?)

- Currently no code to use a USB webcam. Also currently not sure if the Raspberry Pi with camera functions as is

# Supported or broken devices

Feel free to add your device to this list if you have tested it via a pull request

## Broken devices:

-
-

## Verified functional devices:

- ZTE Speed (4.4 Kitkat), some weird bluetooth issues possible (https://github.com/btelman96/runmyrobot_android/issues/45)

- Motorola Moto Z (8.0.0 Oreo)

- Pixel 2 XL (9 Pie)





