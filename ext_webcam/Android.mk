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
	$(LOCAL_PATH)\src\main\jni\capture.c \
	$(LOCAL_PATH)\src\main\jni\util.c \
	$(LOCAL_PATH)\src\main\jni\video_device.c \
	$(LOCAL_PATH)\src\main\jni\webcam.c \
	$(LOCAL_PATH)\src\main\jni\yuv.c \

LOCAL_C_INCLUDES += $(LOCAL_PATH)\src\main\jni
LOCAL_C_INCLUDES += $(LOCAL_PATH)\src\debug\jni
LOCAL_C_INCLUDES += $(LOCAL_PATH)\src\arm64_v8a\jni
LOCAL_C_INCLUDES += $(LOCAL_PATH)\src\arm64_v8aDebug\jni

include $(BUILD_SHARED_LIBRARY)
