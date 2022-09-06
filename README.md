# Posture-Assistance
Noninvasive Posture Assistance Application for Computers (project submission for Metrohacks 2021)

This program utilizes the user's webcam to score the user's posture by comparing the current image to a base 'correct' posture that the user chooses.


The scoring is based off of several factors such as how similar the base case and current image are when applying edge extraction algorithm and a gaussian filter to prevent little fluxtuations of color from affecting the result. 

It also compares the concentration of these edges to the base image. 

The program attempts to performs an algorithm that attempts to map out the user's body, however this is still prone to failure.


# Dependencies
## OpenCV (webcam image extraction)


# Usage
The user will see 2 images onscreen, one is the unfiltered image from their webcam, the other is the filtered image that will be used for scoring. 


In order for the program to score accurately, the user must take a screenshot of the filtered edges image. It will be named 'baseEdgeImage'. 


Copy and paste it but rename it to 'baseFloodFillImage'. Then open up paint or some image editing software and color in yourself with pure red (rgb value : (255,0,0)). 


From there, a score should be visible detailing how 'accurate' your posture is compared to the base case.
