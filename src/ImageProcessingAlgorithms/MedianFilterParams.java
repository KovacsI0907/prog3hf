package ImageProcessingAlgorithms;

public class MedianFilterParams extends AlgorithmParameters{
    public final int kernelSize;
    public MedianFilterParams(int kernelSize) {
        super(kernelSize / 2);

        if(kernelSize < 0 || kernelSize % 2 == 0){
            throw new IllegalArgumentException("Kernel size can't be negative or even");
        }

        this.kernelSize = kernelSize;
    }

    @Override
    public String toString() {
        return "Median Filter parameters:\n" +
                "Kernel Size=" + kernelSize;
    }
}
