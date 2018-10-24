LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE := webcam
LOCAL_CFLAGS := -std=c99
LOCAL_LDFLAGS := -Wl,--build-id
APP_STL := c++_shared
LOCAL_LDLIBS := \
	-llog \
	-ljnigraphics \

LOCAL_SRC_FILES := $(SRCS:$(LOCAL_PATH)/%=%)

LOCAL_C_INCLUDES := $(LOCAL_PATH)/src

include $(BUILD_SHARED_LIBRARY)
