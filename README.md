# Posture-Assistance
Noninvasive Posture Assistance Application for Computers
- a project submittion for Metrohacks 2021.


This program uses the user's webcam to score the user's posture by comparing the current image to a previous base case that the user chooses as an optimal posture.

The scoring is based off of several factors such as how similar are the base case and current image when converted to a format that displays only edges and applies a gaussian
filter to prevent little fluxtuations of color from affecting the result; and it also compares the concentration of these edges to the base image. The program also performs an
algorithm that attempts to map out the user's body, however this is still buggy.

There maybe a few libraries needed to run this, namely, opencv. Not entirely sure though.


# Usage
The user will see on screen 2 images, one is the unfiltered image from their webcam, the other is the filtered image that will be used for scoring
In order for the program to score accurately, the user must press the button to take a screenshot of the filtered edges image. It will be named 'baseEdgeImage'. Keep it like that and in the same location. Copy and paste it but rename it to 'baseFloodFillImage'. Then open up paint or some image editing software and color in yourself with pure red (rgb value : (255,0,0))
