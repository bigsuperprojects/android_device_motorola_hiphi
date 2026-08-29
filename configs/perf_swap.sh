#!/vendor/bin/sh
# Thermal profile/cpu throttling switching tool

MODE=$(getprop persist.vendor.performance_profile)
TARGET_CONF="/data/vendor/etc/thermal-engine.conf"

if [ -z "$MODE" ] || [ "$MODE" -lt 0 ] || [ "$MODE" -gt 2 ]; then
    setprop persist.vendor.performance_profile 0
    MODE=0
fi

if [ "$MODE" = "1" ]; then
    cp /vendor/etc/thermal-engine-cool.conf $TARGET_CONF
    echo 1478400 > /sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq
    echo 1651200 > /sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq
    echo 1612800 > /sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq
    echo 5 > /sys/class/kgsl/kgsl-3d0/max_pwrlevel

elif [ "$MODE" = "2" ]; then
    cp /vendor/etc/thermal-engine-perf.conf $TARGET_CONF
    echo 1785600 > /sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq
    echo 2419200 > /sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq
    echo 2841600 > /sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq
    echo 1267200 > /sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq
    echo 1440000 > /sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq
    echo 0 > /sys/class/kgsl/kgsl-3d0/max_pwrlevel

else
    cp /vendor/etc/thermal-engine-stock.conf $TARGET_CONF
    echo 307200 > /sys/devices/system/cpu/cpufreq/policy0/scaling_min_freq
    echo 633600 > /sys/devices/system/cpu/cpufreq/policy4/scaling_min_freq
    echo 1785600 > /sys/devices/system/cpu/cpufreq/policy0/scaling_max_freq
    echo 2419200 > /sys/devices/system/cpu/cpufreq/policy4/scaling_max_freq
    echo 2841600 > /sys/devices/system/cpu/cpufreq/policy7/scaling_max_freq
    echo "schedutil" > /sys/devices/system/cpu/cpufreq/policy0/scaling_governor
    echo "schedutil" > /sys/devices/system/cpu/cpufreq/policy4/scaling_governor
    echo "schedutil" > /sys/devices/system/cpu/cpufreq/policy7/scaling_governor
    echo "msm-adreno-tz" > /sys/class/kgsl/kgsl-3d0/devfreq/governor
fi
