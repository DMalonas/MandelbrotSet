package model;

import java.awt.image.BufferedImage;

public interface IMandelbrotModel {
    
    public abstract void reset();

    public abstract void undo();

    public abstract void redo();

    public abstract void changeColourMapping();

    public abstract void generate(int minRealPixel, int maxRealPixel, int minImaginaryPixel, int maxImaginaryPixel, int maxIterations, boolean changedScale, double sentRatio);
    
    public abstract void setSize(int width, int height);
   
    public abstract BufferedImage getCurrentImage();
    
    public abstract void setCurrentImage(BufferedImage loadedImage);
    
    public abstract int getCurrentMaxIterations();
    
    public abstract double getCurrentRatio();
    
    // Below methods are for the load/save functions. Values will be incorporated as string metadata within the PNG BufferedImages
    
    public abstract String getCurrentMaxIterationsAsString();
    
    public abstract String getCurrentRatioAsString();
    
    public abstract String getCurrentMinRealAsString();
    
    public abstract String getCurrentMaxRealAsString();
    
    public abstract String getCurrentMinImaginaryAsString();
    
    public abstract String getCurrentMaxImaginaryAsString();
    
    public abstract void setAllParametersFromStrings(String currentMaxIterationsAsString, String currentRatioAsString, String currentMinRealAsString, String currentMaxRealAsString, String currentMinImaginaryAsString, String currentMaxImaginaryAsString);
    
    public abstract BufferedImage[] getMultipleImages();
}
