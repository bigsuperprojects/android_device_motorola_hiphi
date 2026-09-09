#!/bin/bash
set -e
OUT_DIR="out/target/product/hiphi/"
PRODUCT_NAME="hiphi"

if [ -z "$OUT_DIR" ]; then
    echo "Error: ANDROID_PRODUCT_OUT is not set. Please run lunch first."
    exit 1
fi

cd "$OUT_DIR"

# Detect if a full target zip exists, otherwise fallback to local naming convention
LINEAGE_ZIP=$(ls lineage-23.0-*-nightly-${PRODUCT_NAME}-signed.zip 2>/dev/null | head -n 1)

if [ -z "$LINEAGE_ZIP" ]; then
    BASE_NAME="lineage_23.0-$(date +%Y%m%d)-UNOFFICIAL-${PRODUCT_NAME}-eng"
else
    BASE_NAME="${LINEAGE_ZIP%.zip}"
fi

OUTPUT_ZIP="${BASE_NAME}-linux-boot-chain.zip"
OUTPUT_MD5="${OUTPUT_ZIP}.md5sum"

IMAGES=(
    "boot.img"
    "dtbo.img"
    "vendor_boot.img"
    "vendor_dlkm.img"
)

echo "Verifying images in $OUT_DIR..."
for img in "${IMAGES[@]}"; do
    if [ ! -f "$img" ]; then
        echo "Error: Required image '$img' missing. Ensure your device tree correctly defines its build targets."
        exit 1
    fi
done

echo "Creating custom boot chain: $OUTPUT_ZIP"
rm -f "$OUTPUT_ZIP"
zip -j "$OUTPUT_ZIP" "${IMAGES[@]}"

echo "Generating MD5 hash..."
md5sum "$OUTPUT_ZIP" > "$OUTPUT_MD5"

echo "Package complete:"
echo "  $OUT_DIR/$OUTPUT_ZIP"
echo "  $OUT_DIR/$OUTPUT_MD5"
