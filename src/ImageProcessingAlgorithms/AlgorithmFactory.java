package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

/**
 * Algoritmus objektumokat gyárt a megadott ID és paraméterek alapján.
 * A GUI-val való kommunikáció miatt van rá szükség
 */
public final class AlgorithmFactory {

    public static class AlgorithmIDs {
        public static final String BILATERAL_FILTER = "BILATERAL_FILTER";
        public static final String MEDIAN_FILTER = "MEDIAN_FILTER";
        public static final String SOBEL_OPERATOR = "SOBEL_OPERATOR";
    }

    /**
     * Készít egy konkrét algoritmus objektumot. ami a megadott paraméterekkel rendelkezik
     * @param algoId
     * @param params
     * @param tile
     * @return
     */
    public static TileProcessingAlgorithm getAlgorithm(String algoId, AlgorithmParameters params, ImageTile tile){
        return switch (algoId){
            case AlgorithmIDs.BILATERAL_FILTER -> new BilateralFilter((BilateralFilterParams) params, tile);
            case AlgorithmIDs.MEDIAN_FILTER -> new MedianFilter((MedianFilterParams) params, tile);
            case AlgorithmIDs.SOBEL_OPERATOR -> new SobelOperator((SobelParams) params, tile);
            default -> throw new IllegalStateException("Unexpected value: " + algoId);
        };
    }
}
