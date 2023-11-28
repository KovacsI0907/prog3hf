package ImageProcessingAlgorithms;

/**
 * Adott algoritmushoz tartozó paramétereket tartalmaz. Célja, hogy ellenőrízze azok helyességét
 */
public abstract class AlgorithmParameters {
    public final int paddingSizeNecessary;

    /**
     * Ellenőrzi az adatok helyességét, ezt a leszármazottaknak is meg kell tenni a konstruktorban
     * @param paddingSizeNecessary
     */
    protected AlgorithmParameters(int paddingSizeNecessary) {
        if(paddingSizeNecessary < 0){
            throw new IllegalArgumentException("Required padding size can't be negative");
        }
        this.paddingSizeNecessary = paddingSizeNecessary;
    }
}
