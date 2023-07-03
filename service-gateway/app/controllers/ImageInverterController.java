package controllers;

import com.encentral.inverter.api.IImageInverter;
import com.encentral.inverter.impl.DefaultImageInverter;
import io.swagger.annotations.*;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

//@Api(value = "Image Invert")
public class ImageInverterController extends Controller {
    String uploadFilePath = "uploads/";

    public ImageInverterController() {
        try {
            Files.createDirectories(Paths.get(uploadFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "Upload an image to invert")
    @ApiResponses(
            value = {
                    @ApiResponse( code=200, message = "File upload successful", response = String.class),
                    @ApiResponse(code = 400, message = "File Upload failed. File not received", response = String.class),
                    @ApiResponse(code = 400, message = "An Image file is expected", response = String.class),
                    @ApiResponse(code = 500, message = "Unable to save file", response = String.class)
            }
    )
    @ApiImplicitParam(
            name = "image",     value = "image file to upload",
            dataType = "file",  paramType = "form",
            required = true
    )
    public Result invertImage() {
        Http.MultipartFormData<File> formData = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart<File> filePart = formData.getFile("image");

        if (filePart == null)
            return badRequest("File Upload failed. File not received");

        File file = filePart.getFile();

        if (!isImageFile(file)) {
            return badRequest("An Image file is expected");
        }
        String fileName = UUID.randomUUID().toString() + "_" + filePart.getFilename();
        Path destinationPath = Paths.get(uploadFilePath, fileName);

        try {
            Files.move(file.toPath(), destinationPath);
        } catch (IOException e) {
            return internalServerError("Unable to save file");
        }

        String invertedFilename = "inverted_" + fileName;
        IImageInverter inverter = new DefaultImageInverter();
        inverter.invertImage(destinationPath.toString(), invertedFilename);
        return ok("File upload successful, fetch the converted Image with the path: " + invertedFilename);

    }

    @ApiOperation(value = "Get the converted image")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "", response = File.class),
                    @ApiResponse(code = 400, message = "Image not found", response = String.class)
            }
    )
    public Result getImage(@ApiParam(value = "Image File name", required = true) String imageFilename) {
        File file = new File(uploadFilePath, imageFilename);

        if (!file.exists())
            return notFound("Image not found: pls upload an image to convert via the invert-image endpoint");

        return ok(file);
    }

    public static boolean isImageFile(File file) {
        try {
            // Attempt to read the file as an image
            ImageIO.read(file);

            // If no exception is thrown, the file is an image
            return true;
        } catch (IOException e) {
            // If an exception is thrown, the file is not an image or is corrupted
            return false;
        }
    }
}
