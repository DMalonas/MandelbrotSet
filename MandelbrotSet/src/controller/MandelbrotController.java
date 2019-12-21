package controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import model.IMandelbrotModel;
import org.w3c.dom.NodeList;

public class MandelbrotController implements IMandelbrotController {
    
    private IMandelbrotModel model;

    public MandelbrotController(IMandelbrotModel model) {
            this.model = model;
    }
    
    /**
     * In this method the controller handles how the user chooses image file 
     *  and utilises the readCustomData() method for retrieving vital
     * information from the same image file. If the specific meta-data 
     * do not exist the method ends.
     * 
     * This method does not need to involve the model classes. It just has to
     * retrieve the current image from the model and load it from the hard disk.
     * Namely, the controller incorporates the main functionality.
     */
    public void controlLoadFromFile() {
        /* Code for file chooser from https://stackoverflow.com/questions/2918
        435/get-user-inputed-file-name-from-jfilechooser-save-dialog-box */
        JFileChooser chooser=new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.APPROVE_OPTION);
        
        /* Restrict file type https://stackoverflow.com/questions/15771949/how
        -do-i-make-jfilechooser-only-accept-txt */
        FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG"
                + " Image files .png", "png", "image");
        chooser.setFileFilter(filter);
        chooser.showOpenDialog(null);
        
        if (chooser.getSelectedFile() != null) {
            String path=chooser.getSelectedFile().getAbsolutePath();
            try {
                /* Code for opening file as bufferedimage from https://docs.
                oracle.com/javase/tutorial/2d/images/loadimage.html */
                
                if (readCustomData(Files.readAllBytes(Paths.get(path))))
                    model.setCurrentImage(ImageIO.read(new File(path)));
                else
                    JOptionPane.showMessageDialog(null, "The file cannot be "
                            + "opened because it does not comply to metadata"
                            + " format of the application", "File not compliant",
                            JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                Logger.getLogger(MandelbrotController.class.getName()).
                        log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * In this method the controller handles how the user chooses fileName and
     * path and utilises the writeCustomData() method for incorporating vital
     * information within the same image file. 
     * 
     * This method does not need to involve the model classes. It just has to
     * retrieve the current image from the model and save it to the hard disk
     */
  
    public void controlSaveToFile() {
        /* Code for file chooser from 
        https://stackoverflow.com/questions/2918435/get-user-inputed-file-name
        -from-jfilechooser-save-dialog-box */
        JFileChooser chooser=new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.APPROVE_OPTION);
        
        /* Restrict file type https://stackoverflow.com/questions/15771949/how
        -do-i-make-jfilechooser-only-accept-txt*/
        FileNameExtensionFilter filter = new FileNameExtensionFilter
        ("PNG Image files .png", "png", "image");
        chooser.setFileFilter(filter);
        chooser.showSaveDialog(null);
        
        if (chooser.getSelectedFile() != null) {
            String path=chooser.getSelectedFile().getAbsolutePath();
            /* Code for saving file from https://stackoverflow.com/questions/126
            74064/how-to-save-a-bufferedimage-as-a-file*/
            try {
                FileOutputStream fos = new FileOutputStream(path + ".png");
                fos.write(writeCustomData());
                fos.close();
            } catch (Exception ex) {
                Logger.getLogger(MandelbrotController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void controlReset() {
        model.reset();
    }
    
    public void controlUndo() {
        model.undo();
    }
    
    public void controlRedo() {
        model.redo();
    }
    
    public void changeColourMapping() {
        model.changeColourMapping();
    }
    
    public void controlGenerate(int minRealPixel, int maxRealPixel, 
            int minImaginaryPixel, int maxImaginaryPixel, 
            int maxIterations, boolean changedScale, double sentRatio) {
        model.generate(minRealPixel, maxRealPixel, minImaginaryPixel, 
                maxImaginaryPixel, maxIterations, changedScale, sentRatio);
    }
    
    /**
     * This method is necessary for saving meta-data within the image. 
     * Specifically, it saves the MaxIterations, the Ratio, the 
     * MinReal, the MaxReal, the MinImaginary, and the MaxImaginary.
     * so we can re-load the image later and retrieve necessary data
     * for further editing.
     * @return
     * @throws Exception 
     */
    // https://stackoverflow.com/questions/6495518/writing-image-metadata-in-java-preferably-png
    public byte[] writeCustomData() throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();

        ImageWriteParam writeParam = writer.getDefaultWriteParam();
        ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.
                createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);

        //adding metadata
        IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
        IIOMetadataNode text = new IIOMetadataNode("Text");
        
        IIOMetadataNode textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", "MaxIterations");
        textEntry.setAttribute("value", model.getCurrentMaxIterationsAsString());
        text.appendChild(textEntry);
        
        textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", "Ratio");
        textEntry.setAttribute("value", model.getCurrentRatioAsString());
        text.appendChild(textEntry);
        
        textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", "MinReal");
        textEntry.setAttribute("value", model.getCurrentMinRealAsString());
        text.appendChild(textEntry);
        
        textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", "MaxReal");
        textEntry.setAttribute("value", model.getCurrentMaxRealAsString());
        text.appendChild(textEntry);
        
        textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", "MinImaginary");
        textEntry.setAttribute("value", model.getCurrentMinImaginaryAsString());
        text.appendChild(textEntry);
        
        textEntry = new IIOMetadataNode("TextEntry");
        textEntry.setAttribute("keyword", "MaxImaginary");
        textEntry.setAttribute("value", model.getCurrentMaxImaginaryAsString());
        text.appendChild(textEntry);
        
        IIOMetadataNode root = new IIOMetadataNode(IIOMetadataFormatImpl.standardMetadataFormatName);
        root.appendChild(text);

        metadata.mergeTree(IIOMetadataFormatImpl.standardMetadataFormatName, root);
        
        //writing the data
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream stream = ImageIO.createImageOutputStream(baos);
        writer.setOutput(stream);
        writer.write(metadata, new IIOImage(model.getCurrentImage(), null, metadata), writeParam);
        stream.close();

        return baos.toByteArray();
    }
    
    
    /**
     * 
     * @param imageData
     * @return
     * @throws IOException 
     * This method is necessary for retrieving meta-data within the image,saved
     * by writeCustomData method.
     * https://stackoverflow.com/questions/41265608/png-metadata-read-and-write
     * Specifically, it retrieves the MaxIterations, the Ratio, the 
     * MinReal, the MaxReal, the MinImaginary, and the MaxImaginary.
     * so we can re-load the image and retrieve necessary data
     * for further editing.
     */
    public boolean readCustomData(byte[] imageData) throws IOException {
        ImageReader imageReader = ImageIO.getImageReadersByFormatName("png").next();

        imageReader.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(imageData)), true);

        // read metadata of first image
        IIOMetadata metadata = imageReader.getImageMetadata(0);
        
        IIOMetadataNode root = (IIOMetadataNode) metadata.getAsTree
        (IIOMetadataFormatImpl.standardMetadataFormatName);
        NodeList childNodes = root.getElementsByTagName("TextEntry");
        /*Metadata 175 - 180 Ratio, MinReal, MaxReal, MinImaginary, MaxImaginary*/
        String MaxIterations = null;
        String Ratio = null;
        String MinReal = null;
        String MaxReal = null;
        String MinImaginary = null;
        String MaxImaginary = null;
        
        String keyword = null;
        String value = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            System.out.println("*");
            IIOMetadataNode node = (IIOMetadataNode) childNodes.item(i);
            keyword = node.getAttributes().getNamedItem("keyword").getNodeValue();
            value = node.getAttributes().getNamedItem("value").getNodeValue();
            
            //System.out.println(keyword + " " + value);
            if(keyword.equals("MaxIterations")){
                MaxIterations = value;
            }
            else if (keyword.equals("Ratio")){
                Ratio = value;
            }
            else if (keyword.equals("MinReal")){
                MinReal = value;
            }
            else if (keyword.equals("MaxReal")){
                MaxReal = value;
            }
            else if (keyword.equals("MinImaginary")){
                MinImaginary = value;
            }
            else if (keyword.equals("MaxImaginary")){
                MaxImaginary = value;
            }
        }
        if (MaxIterations != null && Ratio != null && MinReal != 
                null && MaxReal != null && MinImaginary != null && MaxImaginary != null)
        {
            model.setAllParametersFromStrings(MaxIterations, Ratio,
                    MinReal, MaxReal, MinImaginary, MaxImaginary);
            return true;
        }
        else
            return false;
    }
    
}
