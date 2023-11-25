package ImageProcessingAlgorithms;

public abstract class AlgorithmParameters {
    public final int paddingSizeNecessary;

    protected AlgorithmParameters(int paddingSizeNecessary) {
        if(paddingSizeNecessary < 0){
            throw new IllegalArgumentException("Required padding size can't be negative");
        }
        this.paddingSizeNecessary = paddingSizeNecessary;
    }
}
