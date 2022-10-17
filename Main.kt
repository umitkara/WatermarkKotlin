package watermark

import java.awt.Color
import java.awt.image.BufferedImage
import java.awt.Transparency
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO
import kotlin.system.exitProcess

fun main() {
    print("Input the image filename:")
    val filename = readln()
    // Check if the image file exists
    if (File(filename).exists()) {
        WatermarkImage.openImage(filename)
    } else {
        println("The file $filename doesn't exist.")
        exitProcess(1)
    }
    print("Input the watermark image filename:")
    val watermarkFilename = readln()
    // Check if the watermark image file exists
    if (File(watermarkFilename).exists()) {
        WatermarkImage.openWatermarkImage(watermarkFilename)
    } else {
        println("The file $watermarkFilename doesn't exist.")
        exitProcess(1)
    }
    print("Input the watermark transparency percentage (Integer 0-100):")
    val transparency = readln().toIntOrNull()
    if (transparency != null) {
        WatermarkImage.watermarkTransparency = transparency
    } else {
        println("The transparency percentage isn't an integer number.")
        exitProcess(1)
    }

    print("Choose the position method (single, grid):")
    when (readln()) {
        "single" -> {
            WatermarkImage.watermarkPositionMethod = "single"
            WatermarkImage.setWatermarkPosition()
        }
        "grid" -> {
            WatermarkImage.watermarkPositionMethod = "grid"
        }
        else -> {
            println("The position method input is invalid.")
            exitProcess(1)
        }
    }

    print("Input the output image filename (jpg or png extension):")
    val outputFilename = readln()
    if (outputFilename.endsWith(".jpg") || outputFilename.endsWith(".png")) {
        WatermarkImage.saveImage(outputFilename)
    } else {
        println("The output file extension isn't \"jpg\" or \"png\".")
        exitProcess(1)
    }
    println("The watermarked image $outputFilename has been created.")
}

/**
 * Function that gets the transparency indicator and returns the string representation.
 * @param type The transparency indicator.
 */
fun getTransparency(type: Int): String {
    return when (type) {
        Transparency.OPAQUE -> "OPAQUE"
        Transparency.BITMASK -> "BITMASK"
        Transparency.TRANSLUCENT -> "TRANSLUCENT"
        else -> "Unknown"
    }
}

/**
 * Watermark Image object.
 * This object gets an image and a watermark image file.
 * Creates a new watermarked image with the dedicated name and extension (jpg or png).
 */
object WatermarkImage {
    private var filename: String = ""
    private var watermarkFilename: String = ""
    private var imageFile: BufferedImage? = null
    private var watermarkFile: BufferedImage? = null
    var watermarkTransparency = 0
        set(value) {
            if (value in 0..100) {
                field = value
            } else {
                println("The transparency percentage is out of range.")
                exitProcess(1)
            }
        }
    private var watermarkImageTransparencyType = 0
    private var useAlpha = false
    private var transparencyColor = Color(0, 0, 0)
    private var outputImage: BufferedImage? = null
    var watermarkPositionMethod = ""
    private var watermarkPosition = (0 to 0)

    /**
     * Opens the image file which is going to be watermarked.
     * @param filename the image file name.
     */
    fun openImage(filename: String) {
        this.filename = filename
        val fileInputStream = FileInputStream(filename)
        imageFile = ImageIO.read(fileInputStream)
        // check if image has 3 color component
        if (imageFile!!.colorModel.numComponents != 3) {
            println("The number of image color components isn't 3.")
            exitProcess(2)
        }
        // check if image is either 24 or 32 bit
        if (imageFile!!.colorModel.pixelSize != 24 && imageFile!!.colorModel.pixelSize != 32) {
            println("The image isn't 24 or 32-bit.")
            exitProcess(3)
        }
    }

    /**
     * Opens the watermark image file.
     * @param filename the watermark image file name.
     */
    fun openWatermarkImage(filename: String) {
        this.watermarkFilename = filename
        watermarkFile = ImageIO.read(File(filename))
        // set the transparency type of watermark image
        this.checkWatermarkTransparency()
        // check if image has 3 color component
        if (watermarkFile!!.colorModel.numComponents != 3 && this.watermarkImageTransparencyType != Transparency.TRANSLUCENT) {
            println("The number of watermark color components isn't 3.")
            exitProcess(2)
        }
        // check if image is either 24 or 32 bit
        if (watermarkFile!!.colorModel.pixelSize != 24 && watermarkFile!!.colorModel.pixelSize != 32) {
            println("The watermark isn't 24 or 32-bit.")
            exitProcess(3)
        }
        // Deprecated
        // check if dimensions of watermark image is same as image
        // if (watermarkFile!!.width != imageFile!!.width || watermarkFile!!.height != imageFile!!.height) {
        //     println("The image and watermark dimensions are different.")
        //     exitProcess(4)
        // }

        // check if watermark image is larger than image
        if (watermarkFile!!.width > imageFile!!.width || watermarkFile!!.height > imageFile!!.height) {
            println("The watermark's dimensions are larger.")
            exitProcess(6)
        }
        this.setTransparencyColor()
    }

    /**
     * Checks if watermark image has transparency.
     * If it has, sets the useAlpha variable based on user input.
     * useAlpha variable is indicates that user wants to use the alpha channel of watermark image.
     */
    private fun checkWatermarkTransparency() {
        this.watermarkImageTransparencyType = watermarkFile!!.colorModel.transparency
        if (this.watermarkFile!!.colorModel.transparency == Transparency.TRANSLUCENT) {
            print("Do you want to use the watermark's Alpha channel?:")
            val useAlphaString = readln()
            if (useAlphaString == "yes") {
                this.useAlpha = true
            }
        }
    }

    /**
     * Check if the watermark image has transparency.
     * If it has not, asks the user if he wants to use a specific color as transparency.
     */
    private fun setTransparencyColor() {
        if (this.watermarkFile!!.colorModel.transparency != Transparency.TRANSLUCENT) {
            print("Do you want to set a transparency color?:")
            if (readln() == "yes") {
                print("Input a transparency color ([Red] [Green] [Blue]):")
                val colorString = readln()
                val colorArray = colorString.split(" ")
                // check if the color array has 3 elements
                if (colorArray.size == 3) {
                    // Try to convert the color array elements to integers
                    val red = colorArray[0].toIntOrNull()
                    val green = colorArray[1].toIntOrNull()
                    val blue = colorArray[2].toIntOrNull()
                    // set the transparency color if the color array elements are integers
                    if (red != null && red in 0..255 && green != null && green in 0..255 && blue != null && blue in 0..255) {
                        this.transparencyColor = Color(red, green, blue)
                    } else {
                        println("The transparency color input is invalid.")
                        exitProcess(5)
                    }
                } else {
                    println("The transparency color input is invalid.")
                    exitProcess(5)
                }
            }
        }
    }

    private fun processImage() {
        for (y in 0 until imageFile!!.height) {
            for (x in 0 until imageFile!!.width) {
                val i = Color(this.imageFile!!.getRGB(x, y))
                val w = Color(this.watermarkFile!!.getRGB(x, y), useAlpha)
                if (w.alpha == 0 || w == this.transparencyColor) {
                    // If the watermark image pixel is either transparent
                    // or is the same color as the transparency color,
                    // then the pixel of the image is used.
                    this.outputImage!!.setRGB(x, y, i.rgb)
                } else {
                    // If the watermark image pixel is not transparent,
                    // then two pixels blend together.
                    val clr = Color(
                        (i.red * (1 - this.watermarkTransparency / 100.0) + w.red * this.watermarkTransparency / 100.0).toInt(),
                        (i.green * (1 - this.watermarkTransparency / 100.0) + w.green * this.watermarkTransparency / 100.0).toInt(),
                        (i.blue * (1 - this.watermarkTransparency / 100.0) + w.blue * this.watermarkTransparency / 100.0).toInt()
                    )
                    this.outputImage!!.setRGB(x, y, clr.rgb)
                }
            }
        }
    }

    private fun processSingleImage() {
        val xRange = this.watermarkPosition.first until this.watermarkFile!!.width + this.watermarkPosition.first
        val yRange = this.watermarkPosition.second until this.watermarkFile!!.height + this.watermarkPosition.second
        for (y in 0 until imageFile!!.height) {
            for (x in 0 until imageFile!!.width) {
                val i = Color(this.imageFile!!.getRGB(x, y))
                if (x in xRange && y in yRange) {
                    val w = Color(this.watermarkFile!!.getRGB(x-this.watermarkPosition.first, y-this.watermarkPosition.second), useAlpha)
                    if (w.alpha == 0 || w == this.transparencyColor) {
                        // If the watermark image pixel is either transparent
                        // or is the same color as the transparency color,
                        // then the pixel of the image is used.
                        this.outputImage!!.setRGB(x, y, i.rgb)
                    } else {
                        // If the watermark image pixel is not transparent,
                        // then two pixels blend together.
                        val clr = Color(
                            (i.red * (1 - this.watermarkTransparency / 100.0) + w.red * this.watermarkTransparency / 100.0).toInt(),
                            (i.green * (1 - this.watermarkTransparency / 100.0) + w.green * this.watermarkTransparency / 100.0).toInt(),
                            (i.blue * (1 - this.watermarkTransparency / 100.0) + w.blue * this.watermarkTransparency / 100.0).toInt()
                        )
                        this.outputImage!!.setRGB(x, y, clr.rgb)
                    }
                } else {
                    this.outputImage!!.setRGB(x, y, i.rgb)
                }
            }
        }
    }

    private fun processGridImage() {
        for (y in 0 until imageFile!!.height) {
            for (x in 0 until imageFile!!.width) {
                val i = Color(this.imageFile!!.getRGB(x, y))
                val w = Color(this.watermarkFile!!.getRGB(x % this.watermarkFile!!.width, y % this.watermarkFile!!.height), useAlpha)
                if (w.alpha == 0 || w == this.transparencyColor) {
                    // If the watermark image pixel is either transparent
                    // or is the same color as the transparency color,
                    // then the pixel of the image is used.
                    this.outputImage!!.setRGB(x, y, i.rgb)
                } else {
                    // If the watermark image pixel is not transparent,
                    // then two pixels blend together.
                    val clr = Color(
                        (i.red * (1 - this.watermarkTransparency / 100.0) + w.red * this.watermarkTransparency / 100.0).toInt(),
                        (i.green * (1 - this.watermarkTransparency / 100.0) + w.green * this.watermarkTransparency / 100.0).toInt(),
                        (i.blue * (1 - this.watermarkTransparency / 100.0) + w.blue * this.watermarkTransparency / 100.0).toInt()
                    )
                    this.outputImage!!.setRGB(x, y, clr.rgb)
                }
            }
        }
    }

    fun setWatermarkPosition() {
        val diffX = this.imageFile!!.width - this.watermarkFile!!.width
        val diffY = this.imageFile!!.height - this.watermarkFile!!.height
        print("Input the watermark position ([x 0-$diffX] [y 0-$diffY]):")
        val positionString = readln()
        val position = positionString.split(" ")
        if (position.size == 2) {
            val x = position[0].toIntOrNull()
            val y = position[1].toIntOrNull()
            if (x == null && y == null) {
                println("The position input is invalid.")
                exitProcess(7)
            }
            if (x != null && x in 0..diffX && y != null && y in 0..diffY) {
                this.watermarkPosition = (x to y)
            } else {
                println("The position input is out of range.")
                exitProcess(7)
            }
        } else {
            println("The position input is invalid.")
            exitProcess(7)
        }
    }

    /**
     * Saves the watermarked image as a new image file with the given name and extension.
     * @param filename the output image file name.
     */
    fun saveImage(filename: String) {
        this.outputImage = BufferedImage(imageFile!!.width, imageFile!!.height, BufferedImage.TYPE_INT_RGB)
        //processImage()
        when (this.watermarkPositionMethod) {
            "single" -> processSingleImage()
            "grid" -> processGridImage()
            else -> processImage()
        }
        ImageIO.write(this.outputImage, filename.substring(filename.lastIndexOf(".") + 1), File(filename))
    }

    /**
     * Prints the image information.
     */
    fun printInfo() {
        println("Image file: $filename")
        println("Width: ${imageFile?.width}")
        println("Height: ${imageFile?.height}")
        println("Number of components: ${imageFile?.colorModel?.numComponents}")
        println("Number of color components: ${imageFile?.colorModel?.numColorComponents}")
        println("Bits per pixel: ${imageFile?.colorModel?.pixelSize}")
        println("Transparency: ${getTransparency(imageFile?.colorModel?.transparency ?: 1)}")
    }
}
