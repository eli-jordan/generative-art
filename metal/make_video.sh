#!/bin/bash

ffmpeg -framerate 25 \
	-i frame-%4d.tif \
	-pix_fmt yuv420p \
	output-raw.mp4

# ffmpeg -framerate 10 \
# 	-i screen-%4d.tif \
# 	-pix_fmt yuv420p \
# 	-filter "minterpolate='fps=25'" \
# 	output-fps-interp.mp4

# ffmpeg -framerate 10 \
# 	-i screen-%4d.tif \
# 	-pix_fmt yuv420p \
# 	-s 600x600 \
# 	-filter "minterpolate='fps=25'" \
# 	output-600x600-fps-interp.mp4

