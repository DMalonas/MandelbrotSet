package model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Observable;
import java.util.Random;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import view.ImagePanel;

public class MandelbrotModel extends Observable implements IMandelbrotModel {
    //current image - what we see
    private BufferedImage currentImage;
    private int[][] currentArray;

    private int currentMaxIterations;
    private int currentWidthInPixels;
    private int currentHeightInPixels;
    private double currentMinReal;
    private double currentMaxReal;
    private double currentMinImaginary;
    private double currentMaxImaginary;
    private double currentRatio;
    private int currentColourMappingValue;
    private MandelbrotCalculator mandelbrotCalculator;
    
    //Stack for undo
    private Stack<BufferedImage> pastImages;
    private Stack<int[][]> pastArrays;
    private Stack<Double> pastMinReal;
    private Stack<Double> pastMaxReal;
    private Stack<Double> pastMinImaginary;
    private Stack<Double> pastMaxImaginary;
    private Stack<Integer> pastMaxIterations;
    private Stack<Double> pastRatios;
    
    //Stack for redo
    private Stack<BufferedImage> undoneImages;
    private Stack<int[][]> undoneArrays;
    private Stack<Double> undoneMinReal;
    private Stack<Double> undoneMaxReal;
    private Stack<Double> undoneMinImaginary;
    private Stack<Double> undoneMaxImaginary;
    private Stack<Integer> undoneMaxIterations;
    private Stack<Double> undoneRatios;
    
    //Colour Mapping
    Random randomColourMapping;
    
    //This array stores the Buffered Images for creating the animation feeling
    BufferedImage[] multipleImages;
    //Images counter to facilitate the multiple images array processing 
    //- index showing where to put the next image.
    int multipleImagesCounter;
    
    protected static final double INITIAL_MIN_REAL = -2.1;
    protected static final double INITIAL_MAX_REAL = 2.1;
    protected static final double INITIAL_MIN_IMAGINARY = -2;
    protected static final double INITIAL_MAX_IMAGINARY = 2;
    protected static final int INITIAL_MAX_ITERATIONS = 50;
    protected static final int DEFAULT_WIDTH_IN_PIXELS = 800;
    protected static final int DEFAULT_HEIGHT_IN_PIXELS = 600;
    protected static final double DEFAULT_RADIUS_SQUARED = 4.0;
    protected static final double INITIAL_RATIO = 1.0;
    protected static final int MAX_COLOUR_MAPPING_VALUE = 256;
    protected static final int DEFAULT_COLOUR_MAPPING_VALUE = -1;
            
    /**
     * Constructor - Initialise the stacks, create a MandelbrotCalculator object
     * and load initial state.
     */
    public MandelbrotModel() {
        initializeStacks();
        mandelbrotCalculator = new MandelbrotCalculator();
        loadInitialState();
    }
    
    @Override
    public void reset() {
        loadInitialState();
        update();
    }
    
    public void loadInitialState() {
        currentMinReal = INITIAL_MIN_REAL;
        currentMaxReal = INITIAL_MAX_REAL;
        currentMinImaginary = INITIAL_MIN_IMAGINARY;
        currentMaxImaginary = INITIAL_MAX_IMAGINARY;
        currentMaxIterations = INITIAL_MAX_ITERATIONS;
        currentWidthInPixels = DEFAULT_WIDTH_IN_PIXELS;
        currentHeightInPixels = DEFAULT_HEIGHT_IN_PIXELS;
        currentRatio = INITIAL_RATIO;
        currentColourMappingValue = DEFAULT_COLOUR_MAPPING_VALUE;
        currentArray = mandelbrotCalculator.calcMandelbrotSet(currentWidthInPixels, 
                currentHeightInPixels, currentMinReal, currentMaxReal,
                currentMinImaginary, currentMaxImaginary, currentMaxIterations,
                DEFAULT_RADIUS_SQUARED);
        randomColourMapping = new Random();
        paintColor();
    }

    @Override
    public void undo() {
        if (!pastImages.empty()) {
            pushToUndoneStacks();
            popFromPastStacks();
            update();
        }
    }

    @Override
    public void redo() {
        if (!undoneImages.empty()) {
            pushToPastStacks();
            popFromUndoneStacks();
            update();
        }
    }

    @Override
    public void changeColourMapping() {
        currentColourMappingValue = randomColourMapping.nextInt(MAX_COLOUR_MAPPING_VALUE);
        paintColor(currentColourMappingValue);
        update();
    }

    @Override
    public void generate(int minRealPixel, int maxRealPixel, int minImaginaryPixel,
            int maxImaginaryPixel, int maxIterations, boolean changedScale, double sentRatio) {
        pushToPastStacks();
        currentMaxIterations = maxIterations;
        currentRatio = sentRatio;
        if (changedScale) {
            double prevMinReal = currentMinReal;
            double prevMaxReal = currentMaxReal;
            double prevMinImaginary = currentMinImaginary;
            double prevMaxImaginary = currentMaxImaginary;
            currentMinReal = prevMinReal + ((double)minRealPixel / 
                    (double)currentWidthInPixels) * (prevMaxReal - prevMinReal);
            currentMaxReal = prevMinReal + ((double)maxRealPixel /
                    (double)currentWidthInPixels) * (prevMaxReal - prevMinReal);
            currentMinImaginary = prevMinImaginary + ((double)minImaginaryPixel 
                    / (double)currentHeightInPixels) * (prevMaxImaginary - prevMinImaginary);
            currentMaxImaginary = prevMinImaginary + ((double)maxImaginaryPixel
                    / (double)currentHeightInPixels) * (prevMaxImaginary - prevMinImaginary);
            //zoomAnimated creates an array of Buffered Images to facilitate
            //View classes to create the animation feeling.
            zoomAnimated(minRealPixel, maxRealPixel, minImaginaryPixel, maxImaginaryPixel);
        }
        currentArray = new int[currentHeightInPixels][currentWidthInPixels];
        currentArray = mandelbrotCalculator.calcMandelbrotSet(currentWidthInPixels,
                currentHeightInPixels, currentMinReal, currentMaxReal, 
                currentMinImaginary, currentMaxImaginary, currentMaxIterations,
                DEFAULT_RADIUS_SQUARED);
        if (currentColourMappingValue == DEFAULT_COLOUR_MAPPING_VALUE)
            paintColor();
        else
            paintColor(currentColourMappingValue);
        update();
    }
    
    /**
     *  This method is utilised by the animated zoom function.
     * The parameters indicate the pixels of the rectangle the user has defined.
     * @param minRealPixel
     * @param maxRealPixel
     * @param minImaginaryPixel
     * @param maxImaginaryPixel 
     */
    public void zoomAnimated(int minRealPixel, int maxRealPixel, int minImaginaryPixel,
            int maxImaginaryPixel) {
        int tempHeightInPixels = maxImaginaryPixel - minImaginaryPixel;
        int tempWidthInPixels = maxRealPixel - minRealPixel;
        //defines the number of images we will send 
        //the total width minus the width selected by the user
        //every image I send is 80 pixels bigger than the previous one.
         // For small selected rectangles more repetitions , denominator is always 80
        int numberOfRepetitions = (currentWidthInPixels - tempWidthInPixels) / 
                (currentWidthInPixels / 10);
       
        //The next two variables,define the upper left corner of the expanding
        //rectangle of the animation feeling. For example, if the selected 
        //rectangle is on the left side, the expanding rectangle must expand
        //slowly on its left side and rapidly on its right side.
        int minRealPixelInterval = minRealPixel / numberOfRepetitions;
        int minImaginaryPixelInterval = minImaginaryPixel / numberOfRepetitions;
        //We initialize the array of images to be sent to the view class.
        //The last one will be calculated in the generate method.
        multipleImages = new BufferedImage[numberOfRepetitions - 1];
        multipleImagesCounter = 0;
        //tempArray is the smaller image that needs to be expanded.
        int[][] tempArray;
        //Gradually create the images with increasing size that need to be sent
        // to the view class.
        for (int i = 1; i < numberOfRepetitions; i++) {
            tempWidthInPixels += currentWidthInPixels / 10;
            tempHeightInPixels += currentHeightInPixels / 10;
            
            tempArray = new int[tempHeightInPixels][tempWidthInPixels];
            tempArray = mandelbrotCalculator.calcMandelbrotSet(tempWidthInPixels,
                    tempHeightInPixels, currentMinReal, currentMaxReal, 
                    currentMinImaginary, currentMaxImaginary, currentMaxIterations,
                    DEFAULT_RADIUS_SQUARED);
            if (currentColourMappingValue == DEFAULT_COLOUR_MAPPING_VALUE)
                paintColorAboveCurrent(tempArray, 
                        tempWidthInPixels, tempHeightInPixels, minRealPixel - i *
                                minRealPixelInterval, minImaginaryPixel - i * 
                                        minImaginaryPixelInterval);
            else
                paintColorAboveCurrent(tempArray, tempWidthInPixels,
                        tempHeightInPixels, minRealPixel - i * minRealPixelInterval, 
                        minImaginaryPixel - i * minImaginaryPixelInterval, 
                        currentColourMappingValue);
        }
    }
    
    public void setSize(int width, int height) {
        currentWidthInPixels = width;
        currentHeightInPixels = height;
    }
    
    private void update() {
        this.setChanged();
        this.notifyObservers();
    }
    
    
    public BufferedImage getCurrentImage() {
        return currentImage;
    }
    
    public void setCurrentImage(BufferedImage loadedImage) {
        currentImage = loadedImage;
        update();
    }
    
    public void paintBW() {
        currentImage = new BufferedImage(currentWidthInPixels, currentHeightInPixels, BufferedImage.TYPE_INT_RGB);
        Color colorTrue = new Color(0, 0, 0);
        Color colorFalse = new Color(255, 255, 255);
        for (int y = 0; y < currentWidthInPixels; y++)
        {
            for (int x = 0; x < currentHeightInPixels; x++)
            {
                if (currentArray[x][y] == currentMaxIterations)
                    currentImage.setRGB(y, x, colorTrue.getRGB());  // https://stackoverflow.com/questions/11951646/setrgb-in-java
                else
                {
                    currentImage.setRGB(y, x, colorFalse.getRGB());
                }
            }
        }
    }
    /**
     * 
     */
    public void paintColor() {
        currentImage = new BufferedImage(currentWidthInPixels, currentHeightInPixels, 
                BufferedImage.TYPE_INT_RGB);
        Color colorTrue = new Color(0, 0, 0);
        Color colorFalse;
        for (int y = 0; y < currentWidthInPixels; y++)
        {
            for (int x = 0; x < currentHeightInPixels; x++)
            {
                if (currentArray[x][y] == currentMaxIterations)
                    currentImage.setRGB(y, x, colorTrue.getRGB());
                else
                {   
                    /* A technique for a color feeling. Adjacent array values
                    -> adjacent colors */
                    colorFalse= new Color(255 - 5 * currentArray[x][y] % 25, 
                            255 - 5 * currentArray[x][y] % 256, 255 - 5 * 
                                    currentArray[x][y] % 256);   
                    currentImage.setRGB(y, x, colorFalse.getRGB());
                }
            }
        }
        
    }
    /**
     * 
     * @param randomColourMappingValue 
     */
    public void paintColor(int randomColourMappingValue) {
        currentImage = new BufferedImage(currentWidthInPixels, currentHeightInPixels, BufferedImage.TYPE_INT_RGB);
        Color colorTrue = new Color(0, 0, 0);
        Color colorFalse;
        for (int y = 0; y < currentWidthInPixels; y++)
        {
            for (int x = 0; x < currentHeightInPixels; x++)
            {
                if (currentArray[x][y] == currentMaxIterations)
                    currentImage.setRGB(y, x, colorTrue.getRGB());
                else
                {
                    colorFalse= new Color(255 - 5 * currentArray[x][y] % (1 + randomColourMappingValue), 255 - 5 * currentArray[x][y] %  (1 + (randomColourMappingValue * randomColourMappingValue) % 255), 5 * currentArray[x][y] % (1 + (randomColourMappingValue * randomColourMappingValue * randomColourMappingValue) % 255));   // A technique for a color feeling. Adjacent array values -> adjacent colors
                    currentImage.setRGB(y, x, colorFalse.getRGB());
                }
            }
        }
    }

    /**
     * This method is utilised by the animated zoom function.
     * It projects the smaller image defined by the tempArray parameter to 
     * the current image.
     * @param tempArray
     * @param tempWidthInPixels
     * @param tempHeightInPixels
     * @param minRealPixel
     * @param minImaginaryPixel 
     */
    public void paintColorAboveCurrent(int[][] tempArray, int tempWidthInPixels, int tempHeightInPixels, int minRealPixel, int minImaginaryPixel) {
        BufferedImage tempImage = new BufferedImage(currentWidthInPixels, currentHeightInPixels, BufferedImage.TYPE_INT_RGB);
        tempImage = deepCopy(currentImage);
        Color colorTrue = new Color(0, 0, 0);
        Color colorFalse;
        for (int y = 0; y < tempWidthInPixels; y++)
        {
            for (int x = 0; x < tempHeightInPixels; x++)
            {
                if (tempArray[x][y] == currentMaxIterations)
                    tempImage.setRGB(y + minRealPixel, x + minImaginaryPixel, colorTrue.getRGB());
                else
                {
                    colorFalse= new Color(255 - 5 * tempArray[x][y] % 25, 255 - 5 * tempArray[x][y] % 256, 255 - 5 * tempArray[x][y] % 256);   // A technique for a color feeling. Adjacent array values -> adjacent colors
                    tempImage.setRGB(y + minRealPixel, x + minImaginaryPixel, colorFalse.getRGB());
                }
            }
        }
        multipleImages[multipleImagesCounter++] = tempImage;
    }
    
    /**
     *This method is utilised by the two paintColorAboveCurrent methods for 
     * effectively cloning a BufferedImage.
     * https://stackoverflow.com/questions/3514158/how-do-you-clone-a-bufferedimage
     * @param bi
     * @return 
     */
    static BufferedImage deepCopy(BufferedImage bi) {
        ColorModel cm = bi.getColorModel();
        boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
        WritableRaster raster = bi.copyData(null);
        return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
}
    
    
    /**
     * This method is utilised by the animated zoom function.
     * For random colour.
     * @param tempArray
     * @param tempWidthInPixels
     * @param tempHeightInPixels
     * @param minRealPixel
     * @param minImaginaryPixel
     * @param randomColourMappingValue 
     */
    public void paintColorAboveCurrent(int[][] tempArray, int tempWidthInPixels, int tempHeightInPixels, int minRealPixel, int minImaginaryPixel, int randomColourMappingValue) {
        BufferedImage tempImage = new BufferedImage(currentWidthInPixels, currentHeightInPixels, BufferedImage.TYPE_INT_RGB);
        tempImage = deepCopy(currentImage);
        Color colorTrue = new Color(0, 0, 0);
        Color colorFalse;
        for (int y = 0; y < tempWidthInPixels; y++)
        {
            for (int x = 0; x < tempHeightInPixels; x++)
            {
                if (tempArray[x][y] == currentMaxIterations)
                    tempImage.setRGB(y + minRealPixel, x + minImaginaryPixel, colorTrue.getRGB());
                else
                {
                    colorFalse= new Color(255 - 5 * tempArray[x][y] % (1 + randomColourMappingValue), 255 - 5 * tempArray[x][y] %  (1 + (randomColourMappingValue * randomColourMappingValue) % 255), 5 * tempArray[x][y] % (1 + (randomColourMappingValue * randomColourMappingValue * randomColourMappingValue) % 255));   // A technique for a color feeling. Adjacent array values -> adjacent colors
                    tempImage.setRGB(y + minRealPixel, x + minImaginaryPixel, colorFalse.getRGB());
                }
            }
        }
        multipleImages[multipleImagesCounter++] = tempImage;
    }
    
    /**
     * next 5 methods not in the iNTERFACE because it is an internal 
     * functionality to the model it does not concern the View or the Controller
     **/
    public void initializeStacks() {
        
        pastImages = new Stack<BufferedImage>();
        pastArrays = new Stack<int[][]>();
        pastMinReal = new Stack<Double>();
        pastMaxReal = new Stack<Double>();
        pastMinImaginary = new Stack<Double>();
        pastMaxImaginary = new Stack<Double>();
        pastMaxIterations = new Stack<Integer>();
        pastRatios = new Stack<Double>();
        
        undoneImages = new Stack<BufferedImage>();
        undoneArrays = new Stack<int[][]>();
        undoneMinReal = new Stack<Double>();
        undoneMaxReal = new Stack<Double>();
        undoneMinImaginary = new Stack<Double>();
        undoneMaxImaginary = new Stack<Double>();
        undoneMaxIterations = new Stack<Integer>();
        undoneRatios = new Stack<Double>();
    }
    
    public void pushToUndoneStacks() {
        undoneImages.push(currentImage);
        undoneArrays.push(currentArray);
        undoneMinReal.push(currentMinReal);
        undoneMaxReal.push(currentMaxReal);
        undoneMinImaginary.push(currentMinImaginary);
        undoneMaxImaginary.push(currentMaxImaginary);
        undoneMaxIterations.push(currentMaxIterations);
        undoneRatios.push(currentRatio);
    }
    
    public void popFromPastStacks() {
        currentImage = pastImages.pop();
        currentArray = pastArrays.pop();
        currentMinReal = pastMinReal.pop();
        currentMaxReal = pastMaxReal.pop();
        currentMinImaginary = pastMinImaginary.pop();
        currentMaxImaginary = pastMaxImaginary.pop();
        currentMaxIterations = pastMaxIterations.pop();
        currentRatio = pastRatios.pop();
    }
    
    public void pushToPastStacks() {
        pastImages.push(currentImage);
        pastArrays.push(currentArray);
        pastMinReal.push(currentMinReal);
        pastMaxReal.push(currentMaxReal);
        pastMinImaginary.push(currentMinImaginary);
        pastMaxImaginary.push(currentMaxImaginary);
        pastMaxIterations.push(currentMaxIterations);
        pastRatios.push(currentRatio);
    }
    
    public void popFromUndoneStacks() {
        currentImage = undoneImages.pop();
        currentArray = undoneArrays.pop();
        currentMinReal = undoneMinReal.pop();
        currentMaxReal = undoneMaxReal.pop();
        currentMinImaginary = undoneMinImaginary.pop();
        currentMaxImaginary = undoneMaxImaginary.pop();
        currentMaxIterations = undoneMaxIterations.pop();
        currentRatio = undoneRatios.pop();
    }
    
    public int getCurrentMaxIterations() {
        return currentMaxIterations;
    }
    
    public double getCurrentRatio() {
        return currentRatio;
    }
    
    /**
     * This method is utilised by the animated zoom function.
     * @return 
     */
        public BufferedImage[] getMultipleImages() {
        return multipleImages;
    }
    
    /* Below methods are for the load/save functions and help with the conversion
       from/to strings. Values will be incorporated as
      string meta-data within the PNG BufferedImages.*/
    
    public String getCurrentMaxIterationsAsString() {
        return Integer.toString(currentMaxIterations);
    }
    
    public String getCurrentRatioAsString() {
        return String.valueOf(currentRatio);
    }
    
    public String getCurrentMinRealAsString() {
        return String.valueOf(currentMinReal);
    }
    
    public String getCurrentMaxRealAsString() {
        return String.valueOf(currentMaxReal);
    }
    
    public String getCurrentMinImaginaryAsString() {
        return String.valueOf(currentMinImaginary);
    }
    
    public String getCurrentMaxImaginaryAsString() {
        return String.valueOf(currentMaxImaginary);
    }
    
    public void setCurrentMaxIterationsFromString(String currentMaxIterationsAsString) {
        currentMaxIterations = Integer.parseInt(currentMaxIterationsAsString);
    }
    
    public void setCurrentRatioFromString(String currentRatioAsString) {
        currentRatio = Double.parseDouble(currentRatioAsString);
    }
    
    public void setCurrentMinRealFromString(String currentMinRealAsString) {
        currentMinReal = Double.parseDouble(currentMinRealAsString);
    }
    
    public void setCurrentMaxRealFromString(String currentMaxRealAsString) {
        currentMaxReal = Double.parseDouble(currentMaxRealAsString);
    }
    
    public void setCurrentMinImaginaryFromString(String currentMinImaginaryAsString) {
        currentMinImaginary = Double.parseDouble(currentMinImaginaryAsString);
    }
    
    public void setCurrentMaxImaginaryFromString(String currentMaxImaginaryAsString) {
        currentMaxImaginary = Double.parseDouble(currentMaxImaginaryAsString);
    }
    
    
    public void setAllParametersFromStrings(String currentMaxIterationsAsString, 
            String currentRatioAsString, String currentMinRealAsString,
            String currentMaxRealAsString, String currentMinImaginaryAsString, 
            String currentMaxImaginaryAsString) {
        setCurrentMaxIterationsFromString(currentMaxIterationsAsString);
        setCurrentRatioFromString(currentRatioAsString);
        setCurrentMinRealFromString(currentMinRealAsString);
        setCurrentMaxRealFromString(currentMaxRealAsString);
        setCurrentMinImaginaryFromString(currentMinImaginaryAsString);
        setCurrentMaxImaginaryFromString(currentMaxImaginaryAsString);
        currentArray = mandelbrotCalculator.calcMandelbrotSet(currentWidthInPixels, currentHeightInPixels, currentMinReal, currentMaxReal, currentMinImaginary, currentMaxImaginary, currentMaxIterations, DEFAULT_RADIUS_SQUARED);
    }
}
