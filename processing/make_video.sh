#!/bin/bash

INPUT_DIR="$1"
OUTPUT_DIR="$2"

usage() {
	echo "Usage: ./make_video.sh <input dir> <output dir>"
}

if [[ -z "$INPUT_DIR" ]]; then
	echo "Input directory is required. $usage"
	exit 1
fi

if [[ -z "$OUTPUT_DIR" ]]; then
	echo "Output dir is required. $usage"
	exit 1
fi

mkdir -p "$OUTPUT_DIR"
cp $INPUT_DIR/screen-* $OUTPUT_DIR


ffmpeg -framerate 25 \
	-i "${OUTPUT_DIR}/screen-%4d.tif" \
	-pix_fmt yuv420p \
	"${OUTPUT_DIR}/output.mp4"

# ffmpeg -framerate 15 \
# 	-i "${OUTPUT_DIR}/screen-%4d.tif" \
# 	-pix_fmt yuv420p \
# 	-filter "minterpolate='fps=25'" \
# 	"${OUTPUT_DIR}/output-fps-interp.mp4"

if [[ -e "${OUTPUT_DIR}/output.mp4" ]]; then
	rm $INPUT_DIR/screen-*
fi



# ffmpeg -framerate 10 \
# 	-i screen-%4d.tif \
# 	-pix_fmt yuv420p \
# 	-s 600x600 \
# 	-filter "minterpolate='fps=25'" \
# 	output-600x600-fps-interp.mp4

