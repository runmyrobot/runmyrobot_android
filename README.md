# runmyrobot_android
android code to run on robots via LetsRobot api

# Development

## Required software

### Android Studio
- Version 3.1.4 or higher. May not be buildable on lower versions

# Setup

## Robot and Camera Ids

In your local.properties file, include these

```
robot.robotID = "robotIDInQuotes"
robot.cameraID="cameraIdInQuotes"
robot.cameraPass="cameraPassInQuotes" //(Will default to hello if not provided. If you are not sure what this is, do not include)
```

## Running the robot

To make the robot operational and connected:
 1. Click build and run to deploy to phone (Play button)
 2. Open App if not opened already
 3. Hit enable

## Adding separate components

Add classes that extend Component to app/src/main/java/com/runmyrobot/android_robot_for_phone/myrobot/RobotComponentList.kt
There are some examples there for Serial motor control

## Error reporting

To enable custom error reporting, please add this line to local.properties. It will not report errors by default

```
app.reportErrors="TRUE"
```

# Some known issues

- Camera streaming will only work if the app has not been backgrounded (turning screen off is fine)
Will crash if home button is hit

- Currently no code to use a USB webcam. Also currently not sure if the Raspberry Pi with camera functions as is


# Supported or broken devices

Feel free to add your device to this list if you have tested it via a pull request

## Broken devices:

-
-
-

## Verified functional devices:

- ZTE Speed (4.4 Kitkat)

- Motorola Moto Z (8.0.0 Oreo)

- Pixel 2 XL (9 Pie)





