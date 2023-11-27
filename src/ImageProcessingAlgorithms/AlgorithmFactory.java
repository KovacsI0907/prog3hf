package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

public final class AlgorithmFactory {

    public static class AlgorithmIDs {
        public static final String BILATERAL_FILTER = "BILATERAL_FILTER";
        public static final String MEDIAN_FILTER = "MEDIAN_FILTER";
        public static final String SOBEL_OPERATOR = "SOBEL_OPERATOR";
    }

    public static TileProcessingAlgorithm getAlgorithm(String algoId, AlgorithmParameters params, ImageTile tile){
        return switch (algoId){
            case AlgorithmIDs.BILATERAL_FILTER -> new BilateralFilter((BilateralFilterParams) params, tile);
            case AlgorithmIDs.MEDIAN_FILTER -> new MedianFilter((MedianFilterParams) params, tile);
            case AlgorithmIDs.SOBEL_OPERATOR -> new SobelOperator((SobelParams) params, tile);
            default -> throw new IllegalStateException("Unexpected value: " + algoId);
        };
    }
}
