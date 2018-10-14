LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := webcam
LOCAL_CFLAGS := -std=c99
LOCAL_LDFLAGS := -Wl,--build-id
APP_STL := c++_shared
LOCAL_LDLIBS := \
	-llog \
	-ljnigraphics \

LOCAL_SRC_FILES := \
	D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\main\jni\capture.c \
	D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\main\jni\util.c \
	D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\main\jni\video_device.c \
	D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\main\jni\webcam.c \
	D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\main\jni\yuv.c \

LOCAL_C_INCLUDES += D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\main\jni
LOCAL_C_INCLUDES += D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\debug\jni
LOCAL_C_INCLUDES += D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\arm64_v8a\jni
LOCAL_C_INCLUDES += D:\Git\Libraries\AndroidUvcDemo\ext_webcam\src\arm64_v8aDebug\jni

include $(BUILD_SHARED_LIBRARY)
