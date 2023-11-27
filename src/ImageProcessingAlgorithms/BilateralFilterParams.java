package ImageProcessingAlgorithms;

public class BilateralFilterParams extends AlgorithmParameters{
    public final double intensitySigma;
    public final double spatialSigma;
    public final int kernelSize;
    public BilateralFilterParams(double intensitySigma, double spatialSigma, int kernelSize) {
        super(kernelSize/2);

        if(kernelSize < 0 || kernelSize % 2 == 0){
            throw new IllegalArgumentException("Kernel size can't be negative or even");
        }

        if(intensitySigma <= 0 || spatialSigma <= 0){
            throw new IllegalArgumentException("Standard deviation can only be positive");
        }
        this.intensitySigma = intensitySigma;
        this.spatialSigma = spatialSigma;
        this.kernelSize = kernelSize;
    }

    @Override
    public String toString() {
        return "Bilateral Filter parameters:\n" +
                "Intensity deviation=" + intensitySigma +
                "\nSpatial deviation=" + spatialSigma +
                "\nKernel size=" + kernelSize;
    }
}
