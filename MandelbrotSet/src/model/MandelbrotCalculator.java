package model;

/**
 * The MandelbrotCalculator class contains methods which establish the Mandelbrot set.
 * The calcMandelbrotSet method below iterates over X,Y positions and establishes for specified parameter values a 2-D array containing
 * for each [y][x] pixel an iteration value that may be mapped to colour and used to visualise the Mandelbrot set.
 * The calcMandelbrotSet method iteratively calls for each X,Y coordinate, the calcMandel method (also included here) which establishes the iteration value for a particular X,Y coordinate.
 *
 * Example usage -- To obtain a 800x800 2-D array of Mandelbrot set values for the initial parameter values
 *
 * MandelbrotCalculator mandelCalc = new MandelbrotCalculator();
 * int[][] madelbrotData = mandelCalc.calcMandelbrotSet(800, 800, MandelbrotCalculator.INITIAL_MIN_REAL, MandelbrotCalculator.INITIAL_MAX_REAL, MandelbrotCalculator.INITIAL_MIN_IMAGINARY, MandelbrotCalculator.INITIAL_MAX_IMAGINARY, MandelbrotCalculator.INITIAL_MAX_ITERATIONS, MandelbrotCalculator.DEFAULT_RADIUS_SQUARED);
 *
 * @author jonl
 *
 */
public class MandelbrotCalculator {

    // Initial parameter values
    protected static final double INITIAL_MIN_REAL = -2.0;
    protected static final double INITIAL_MAX_REAL = 0.7;
    protected static final double INITIAL_MIN_IMAGINARY = -1.25;
    protected static final double INITIAL_MAX_IMAGINARY = 1.25;
    protected static final int INITIAL_MAX_ITERATIONS = 50;

    // Default parameter values
    protected static final double DEFAULT_RADIUS_SQUARED = 4.0;

    /**
     * Method which calculates the number of iterations over which Z_n+1 = Z_n^2 + C can be applied for Z starting at the origin and a specific constant C (given by its Real and cImaginary components).
     * If the square of the absolute value of Z is still inside the defined squaredRadius after maxIterations then we stop iterating and return maxIterations, implicitly assuming that Z will never escape the radius for the given setting of the constant C.
     * In this case, the value of C is treated as being part of the Mandelbrot set, i.e. the set of starting constants C for which the value of Z remains bounded within the complex plane under iteration.
     *
     * @param cReal the real component (akin to X component) of the constant C.
     * @param cImaginary the imaginary component (akin to Y component) of the constant C.
     * @param maxIterations the maximum number of iterations over which to iterate the equation until assuming Z will remain bounded.
     * @param radiusSquared the squared of the radius to use when determining whether Z escaped the circle in the complex plain or remained bounded. The value used is commonly 4.0.
     * @return the number iterations for the value of Z to grow outside of the bounding radius, or maxIterations if it never escaped.
     */
    private int calcMandel(double cReal, double cImaginary, int maxIterations, double radiusSquared){
        // To work out Z_n+1 = Z_n^2 + C and establish whether C is in the Mandelbrot set or not
        // we need to
        //    square the current value of Z
        //    add C
        // and then check whether the square of the length of Z is outside the given radiusSquared
        // Notes for squaring the complex number Z below
        // Z = zr + i*zi
        // C = cr + i*ci
        // Z^2 = zr^2 + 2zr*i*zi + i^2*zi^2 = (zr^2 - zi^2) + i*2zr*zi
        int iterations = 0;
        double zr = 0;
        double zi = 0;
        boolean outside = false;
        while (iterations < maxIterations && !outside) {
            double zr2 = zr * zr;
            double zi2 = zi * zi;
            double nzr = zr2 - zi2 + cReal;
            double nzi = 2 * zr * zi + cImaginary;
            zr = nzr;
            zi = nzi;
            if ((zr2 + zi2) > radiusSquared)
                outside = true;
            iterations++;
        }
        return iterations;
    }



    /**
     * Method to calculate the Mandelbrot set for the given parameter settings.
     * @param xResolution the number of pixels on the x-axis in your GUI display.
     * @param yResolution the number of pixels on the y-axis in your GUI display.
     * @param minReal the lower real bound for the complex constant C (equivalent to lower bound X value in Mandelbrot set)
     * @param maxReal the upper real bound for the complex constant C (equivalent to upper bound X value in Mandelbrot set)
     * @param minImaginary the lower imaginary bound for the complex constant C (equivalent to lower bound Y value in Mandelbrot set)
     * @param maxIterations the maximum number of iterations to iterate the complex formula
     * @param radiusSquared the squared of the radius to use when determining whether Z escaped the circle in the complex plain or remained bounded.
     * @return the 2-D integer array mandelbrotData[yResolution][xResolution] containing the for each [y][x] pixel the number of iterations needed until Z escaped the bounding radius, or maxIterations otherwise.
     */
    public int[][] calcMandelbrotSet(int xResolution, int yResolution, double minReal, double maxReal, double minImaginary, double maxImaginary, int maxIterations, double radiusSquared){
        int[][] mandelbrotData = new int[yResolution][xResolution];
        double realRange = maxReal - minReal;

        double realStep = (maxReal - minReal)/xResolution;
        double imaginaryStep = (maxImaginary - minImaginary)/yResolution;


        for (int y = 0; y < yResolution; y++) {
            double cImaginary = minImaginary + y * imaginaryStep;
            for (int x = 0; x < xResolution; x++) {
                double cReal = minReal + x * realStep;
                mandelbrotData[y][x] = calcMandel(cReal, cImaginary, maxIterations, radiusSquared);
                cReal = cReal + realStep;
            }
        }
        return mandelbrotData;
    }
}

