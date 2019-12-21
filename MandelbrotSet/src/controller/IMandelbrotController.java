package controller;

public interface IMandelbrotController {
    
    void controlLoadFromFile();
    void controlSaveToFile();
    void controlReset();
    void controlUndo();
    void controlRedo();
    void changeColourMapping();
    void controlGenerate(int minRealPixel, int maxRealPixel, int minImaginaryPixel, int maxImaginaryPixel, int maxIterations, boolean changedScale, double sentRatio);
}
