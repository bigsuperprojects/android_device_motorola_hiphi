#
# SPDX-FileCopyrightText: The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

DEVICE_PATH := device/motorola/hiphi

# Inherit from motorola sm8475-common
include device/motorola/sm8475-common/BoardConfigCommon.mk

# Bootloader
TARGET_BOOTLOADER_BOARD_NAME := hiphi

# HIDL
ODM_MANIFEST_SKUS += dne
ODM_MANIFEST_DNE_FILES := $(DEVICE_PATH)/manifest_dne.xml
ODM_MANIFEST_SKUS += dn
ODM_MANIFEST_DN_FILES := $(DEVICE_PATH)/manifest_dn.xml
ODM_MANIFEST_SKUS += n
ODM_MANIFEST_N_FILES := $(DEVICE_PATH)/manifest_n.xml $(COMMON_PATH)/manifest_cape_ss.xml

# Kernel
TARGET_KERNEL_CONFIG += \
	vendor/ext_config/moto-waipio-hiphi.config

# Kernel Modules
BOARD_VENDOR_KERNEL_MODULES_LOAD := $(strip $(shell cat $(DEVICE_PATH)/modules.load))
BOARD_VENDOR_KERNEL_MODULES_BLOCKLIST_FILE := $(DEVICE_PATH)/modules.blocklist
BOARD_VENDOR_RAMDISK_KERNEL_MODULES_LOAD := $(strip $(shell cat $(DEVICE_PATH)/modules.load.vendor_boot))
BOARD_VENDOR_RAMDISK_KERNEL_MODULES_BLOCKLIST_FILE := $(DEVICE_PATH)/modules.blocklist.vendor_boot
BOARD_VENDOR_RAMDISK_RECOVERY_KERNEL_MODULES_LOAD := $(strip $(shell cat $(DEVICE_PATH)/modules.load.recovery))
BOOT_KERNEL_MODULES := $(BOARD_VENDOR_RAMDISK_RECOVERY_KERNEL_MODULES_LOAD)

TARGET_KERNEL_EXT_MODULES += \
    motorola/drivers/mmi_annotate \
    motorola/drivers/mmi_info \
    motorola/drivers/power/bm_adsp_ulog \
    motorola/drivers/power/mmi_charger \
    motorola/drivers/power/qti_glink_charger \
    motorola/drivers/power/qpnp_adaptive_charge \
    motorola/drivers/power/cw2217b_fg_mmi \
    motorola/drivers/power/sgm4154x_charger_lite \
    motorola/drivers/misc/utag \
    motorola/drivers/mmi_relay \
    motorola/drivers/moto_f_mass_storage \
    motorola/drivers/misc/mmi_sys_temp \
    motorola/drivers/power/smart_pen_charger \
    motorola/drivers/regulator/slg5bm43670 \
    motorola/drivers/sensors \
    motorola/drivers/misc/hall \
    motorola/drivers/misc/sx937x \
    motorola/drivers/input/touchscreen/touchscreen_mmi \
    motorola/drivers/input/touchscreen/goodix_berlin_mmi \
    motorola/drivers/input/touchscreen/stmicro_mmi \
    motorola/drivers/input/misc/fpc_fps_mmi \
    motorola/drivers/input/misc/goodix_fod_mmi \
    motorola/drivers/nfc/st21nfc

# Partitions
BOARD_MOT_DP_GROUP_SIZE := 9659482112 # ( BOARD_SUPER_PARTITION_SIZE - 4MB )
BOARD_SUPER_PARTITION_SIZE := 9663676416

# Properties
TARGET_PRODUCT_PROP += $(DEVICE_PATH)/product.prop
TARGET_SYSTEM_PROP += $(DEVICE_PATH)/system.prop
TARGET_VENDOR_PROP += $(DEVICE_PATH)/vendor.prop

# Recovery
TARGET_RECOVERY_UI_MARGIN_HEIGHT := 90

# Security
BOOT_SECURITY_PATCH := 2025-02-01
VENDOR_SECURITY_PATCH := $(BOOT_SECURITY_PATCH)

# Verified Boot
BOARD_AVB_ROLLBACK_INDEX := 25
BOARD_AVB_VBMETA_SYSTEM_ROLLBACK_INDEX := $(BOARD_AVB_ROLLBACK_INDEX)

# inherit from the proprietary version
include vendor/motorola/hiphi/BoardConfigVendor.mk
