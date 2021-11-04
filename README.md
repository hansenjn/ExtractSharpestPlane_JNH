# ExtractSharpestPlane_JNH
## General description
ExtractSharpestPlane_JNH is an ImageJ/FIJI plugin to extract the sharpest plane by highest SD from image stacks. The plugin can make use of the BioFormats integration in FIJI. Thus, it is highly recommended to use it with FIJI.

The plugin has been originally developed to be included in high-throughput analysis of images acquired with a Zeiss Cell Discoverer (CD7). Here, the plugin can be used to automatically extract images from .czi files and extract the sharpest plane from each image stack. This works better than performing the same with the integration in the CD7 software, as with this plugin the sharpest plane is detected for each image individually and does not need to be specified for all images together. Thus, the plugin delivers the sharpest possible images for each image stack.

## How to cite?
More information will be available here soon...

## Copyright
(c) 2021, Jan N. Hansen

Contact: jan.hansen (at) uni-bonn.de

## Licenses
The plugin and the source code are published under the [GNU General Public License v3.0](https://github.com/hansenjn/ExtractSharpestPlane_JNH/blob/master/LICENSE).

## Download
Download the latest plugin version [here](https://github.com/hansenjn/ExtractSharpestPlane_JNH/releases/).

## How to use it
### Installing the plugin
Download the latest version [here](https://github.com/hansenjn/ExtractSharpestPlane_JNH/releases/). Install it into FIJI or ImageJ by drag and drop into the status bar of the ImageJ/FIJI window. Confirm the upcoming dialog and restart the software. Now, the plugin is installed. It can be launched by going to the menu entry _Plugins > JNH > Custom > extract sharpest plane (vX.X.X)_.

### Launching the plugin
To launch the plugin, go to the plugins menu entry _Plugins > JNH > Custom > extract sharpest plane (vX.X.X)_. A dialog pops up, where you can select the plugin settings.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140383641-f0f83970-efae-4be9-bede-9002aa448637.png">
</p>

**Process:** You can select which images you want to process. Three options can be applied.
  - Multiple images: a dialog will open after pressing ok (see below). Here you can generate a list of images to be processed. Add files to the list (select files individually) and press start processing. Alternatively, files can be added to the list based on their file names (select files by pattern or regex entry).
  - Active image in FIJI: the currently front-most image opened in FIJI will be processed
  - All images open in FIJI: all images that are open in FIJI will be processed


Note: The software can process only saved images – every image to be processed needs to be saved on the hard disk before it can be processed. This is required because the plugin needs information on a path where it shall store the output files. Only saved images can provide that path in ImageJ/FIJI. If you process the image in ImageJ/FIJI and subject an unsaved image to the plugin, an error message will be thrown and the plugin stops.


<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140375269-f5b18b17-37e2-420c-a57a-38cef3d5edfe.png">
</p>

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140384450-2712facf-397d-4aab-8f93-cf56b03844e3.png">
   <img src="https://user-images.githubusercontent.com/27991883/140390291-0e82a21f-4106-48f3-b2d5-1d5547baed65.png">
   <img src="https://user-images.githubusercontent.com/27991883/140390294-92ea9969-ed04-4156-a95a-3309e53f8c01.png">
</p>

**Series to be processed:** If you are running this plugin on FIJI, you can make use of the BioFormats plugin to open microscopy files. These might contain multiple series in one file. In the field you can specify specific series you want to have processed only (separate them with a comma) or you type *ALL* to process all contained series (= images). Alternatively, you may also enter *SERIES* to process only images in the file that are entitled with a name beginning with "Series".

**Channels to be used for determining the sharpest plane:** Select the channels that you want to include in the determination of the sharpest plane. The plugin determines the standard deviation of all pixel intensities in a slice image belonging to the channels selected here. By checking the box in front of *use channel X for calculation*, you select a channel to be included in determining the standard deviation. The slice image featuring the highest SD is considered sharpest and returned as an image (see also below for how to make a projection around the sharpest slice). It is recommended that you activate here the channels where you expect to see the features you want to study.

**Color settings:** These settings allow to modify the LUTs (= "look-up-tables", the color schemes in which the channel images are displayed) for individual channels. For example, you can specify that a specific channel (e.g., channel 3) will appear in blue in the output image by setting the "Color for channel 3:" to Blue.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140384212-16e7945d-5512-4d3f-a4b4-d726bca7efc3.png">
</p>

**Projection around the sharpest plane:**
Here you can select that also a specific number of planes above and below the sharpest plane shall be included in the output image. The output image will then represent a maximum intensity projection of the sharpest plane and the planes above and below the sharpest plane. 

For example, if the sharpest plane for an image stack of 7 slice images would be slice image 3 and the user had selected the following setting (that 2 planes before and 1 plane after the sharpest plane should be included - see image below), the image that is output represents a maximum intensity projection of slice images 1 to 4.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140385528-8a57e579-47be-4826-a22e-a5ca16db21b4.png">
</p>

**Output file names:** 
The plugin will save the output image where the input image was stored. The output image will be named by a combination of the input image name and the suffix "\_Sh" and, if the input file contained multiple images, the series number (e.g., "\_s2" for the third series (Note: the numbering starts with "s0" for the first series)). 

Of note, the plugin overrides the equally named output images. Thus if you run the plugin on a file repetitively, the previous run may be overriden. This can be prevented by adding the processing date to the suffix through activating the following checkbox:
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140386996-107dd54e-33db-4fea-9071-6d4186807908.png">
</p>

Furthemore, if it is useful for you, you can also add the series name that is saved in the metadata of the file to the suffix (if the image is loaded via the BioFormats plugin):
<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140387004-7958fa9a-0afb-4419-881e-653e8b29d9e9.png">
</p>

### Running the plugin
When you have set the settings and start the plugin, the Multi-Task-Manager dialog appears (sometimes it takes a few seconds until it appears, especially when opening files via the BioFormats integration in FIJI). This dialog allows you to follow the progress of processing the input image(s). If you loaded a microscopy file that contains multiple series (e.g., as shown here the file "Example_Inputfile.czi"), each series is shown as an individual task. When the processing is done, the bar text will show "finished!" (and, on Windows computers, the bar will turn green).

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140389233-522e46f2-293f-4e3d-99be-5d706e97be3e.png">
</p>

For each processed image/series, two output file wills have been generated at the path, where the respective input image was saved:
1. A tif image, containing the output image.
2. A txt file, which stores the settings that were selected for processing by the ExtractSharpestPlane_JNH plugin and the measurement results obtained when determining the sharpest plane.

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140389453-ac18ad89-0509-4171-9dd9-b62e7868a885.png">
</p>

The txt file looks, e.g., like this and contains all serttings

<p align="center">
   <img src="https://user-images.githubusercontent.com/27991883/140389760-6d3521df-18b0-482e-b2bd-764ba0486c2d.png">
</p>
