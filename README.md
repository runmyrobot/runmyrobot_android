# runmyrobot_android
android code to run on robots via LetsRobot api

# Development

## Required software

### Android Studio

- Version 3.1.4 or higher. May not be buildable on lower versions

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

Add classes that extend Component to app/src/main/java/com/runmyrobot/android_robot_for_phone/myrobot/RobotComponentList.kt
There are some examples there for Serial motor control and bluetooth control. The app currently defaults
to bluetooth control

## Error reporting

Errors will be reported to Bugsnag (In the future it may automatically create issues on github)
This is completely optional and will be disabled unless enabled through settings

# Some known issues

- Camera streaming will only work if the app has not been backgrounded (turning screen off is fine if no lock screen)

- Currently no code to use a USB webcam. Also currently not sure if the Raspberry Pi with camera functions as is

# Supported or broken devices

Feel free to add your device to this list if you have tested it via a pull request

## Broken devices:

-
-

## Verified functional devices:

- ZTE Speed (4.4 Kitkat)

- Motorola Moto Z (8.0.0 Oreo)

- Pixel 2 XL (9 Pie)





