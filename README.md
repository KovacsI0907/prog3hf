# Parallel Bulk Image Processor

This tool allows you to apply image processing algorithms to one or many PNG images efficiently.

<p align="center">
  <img src="https://github.com/user-attachments/assets/83c610a1-6b31-4aa5-8c9d-29192eeb81c9" width="256" alt="Original image">
  <img src="https://github.com/user-attachments/assets/d37e8801-9b7c-43fc-ad3e-f9f4db1d3f83" width="256" alt="After sobel operator">
</p>

## Features

__Parallel Processing:__ Choose the number of threads for processing images in parallel.

__Bulk Processing:__ Apply the same algorithm to hundreds of images at once.

__Efficient Memory Usage:__ Process images larger than available system RAM by streaming image data instead of loading it all at once. RAM usage for each processing job can be customized.

__Extensible Design:__ Implement new algorithms by extending the TileProcessingAlgorithm class.

>[!NOTE]  
>The project only supports algorithms that can process image tiles independently, such as convolution-based operations.


## How to Build

No build system is required. You can compile with standard javac commands or use an IDE (like IntelliJ) to handle the setup automatically.
## Usage

0. (Optional) Set RAM limit and thread count for the job.

<p align="center">
  <img src="https://github.com/user-attachments/assets/597e0943-2e83-42af-829d-117f7c3f6e10" width="256" alt="Options Menu">
  <img src="https://github.com/user-attachments/assets/2f3a4d3d-de96-4ec5-944f-101d0eaa96d7" width="256" alt="Set limits">
</p>

1. Select the images you want to process.

3. Choose an algorithm and start the processing.

<p align="center">
  <img src="https://github.com/user-attachments/assets/7f0d7a17-f48b-487c-8d9b-c7cc9f0d445c" width="256" alt="Select images">
  <img src="https://github.com/user-attachments/assets/19f46c10-ad12-44c9-b1de-6ea5ac49bafd" width="256" alt="Specify an algorithm">
</p>


3. Check the output folder for processed images.
>[!NOTE]
>The outputs' default location is `<current_working_dir>/output`.
>If that folder already exsist, the files it contains will not be touched, but the output files will be placed there nevertheless.

<p align="center">
  <img src="https://github.com/user-attachments/assets/d05c0901-36e0-42b0-9e6a-66d6cd6ed8f3" width="256">
</p>
