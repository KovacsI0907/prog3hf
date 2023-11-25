package ImageProcessingAlgorithms;

public class SobelParams extends AlgorithmParameters{
    public final int clampDownTreshold;


    public SobelParams(int clampDownTreshold) {
        super(1);

        if(clampDownTreshold < 0 || clampDownTreshold > 255){
            throw new IllegalArgumentException();
        }

        this.clampDownTreshold = clampDownTreshold;
    }
}
