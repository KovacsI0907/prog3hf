package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

/**
 * Sobel Operator éldetekciós algoritmus implementációja
 * Időkomplexitása: O(n)
 */
public class SobelOperator extends TileProcessingAlgorithm{

    public final int clampDownAt;

    public SobelOperator(SobelParams params, ImageTile tile) {
        super(params, tile);
        if(params.clampDownTreshold < 0 || params.clampDownTreshold > 255){
            throw new IllegalArgumentException();
        }
        this.clampDownAt = params.clampDownTreshold;
    }

    @Override
    public ImageTile produceOutput() {
        long[][] outputData = new long[tile.height][tile.width];
        double[] temp = new double[tile.width*tile.height];

        for(int y = 0;y<tile.height;y++){
            for(int x = 0;x<tile.width;x++){
                temp[y*tile.width+x] = convolve(tile, x,y);
            }
        }

        int[] normalized = normalize255(temp);

        for(int y = 0;y<tile.height;y++){
            for(int x = 0;x<tile.width;x++){
                long pixel = 0;
                int value = (normalized[y * tile.width + x] & 0xFF);
                value = (value<clampDownAt)?0:value;
                pixel = pixel | 0xFFL << 24;
                pixel = pixel | value << 16;
                pixel = pixel | value << 8;
                pixel = pixel | value;
                outputData[y][x] = pixel;
            }
        }

        return new ImageTile(tile.width, tile.height, 0, tile.image, tile.tileIndex, outputData);
    }

    private static double convolve(ImageTile tile, int x, int y){
        int redSumX = 0;
        int greenSumX = 0;
        int blueSumX = 0;
        int redSumY = 0;
        int greenSumY = 0;
        int blueSumY = 0;

        for(int j = -1;j<=1;j++){
            for(int i = -1;i<=1;i++){
                redSumX += sobelX[j+1][i+1] * tile.getRedRO(x + i,y+j);
                greenSumX += sobelX[j+1][i+1] * tile.getGreenRO(x + i,y+j);
                blueSumX += sobelX[j+1][i+1] * tile.getBlueRO(x + i,y+j);

                redSumY += sobelY[j+1][i+1] * tile.getRedRO(x + i,y+j);
                greenSumY += sobelY[j+1][i+1] * tile.getGreenRO(x + i,y+j);
                blueSumY += sobelY[j+1][i+1] * tile.getBlueRO(x + i,y+j);
            }
        }

        double red = Math.sqrt(redSumX*redSumX + redSumY*redSumY);
        double green = Math.sqrt(greenSumX*greenSumX + greenSumY*greenSumY);
        double blue = Math.sqrt(blueSumX*blueSumX + blueSumY*blueSumY);

        return red + green + blue;
    }

    private static int[] normalize255(double[] input){
        double max = Integer.MIN_VALUE;
        double min = Integer.MAX_VALUE;
        int[] output = new int[input.length];

        for(int i = 0;i<input.length;i++) {
            if(min > input[i]){
                min = input[i];
            }

            if(max < input[i]){
                max = input[i];
            }
        }

        double range = max -min;

        for(int i = 0;i<input.length;i++) {
            int val = (int)(((input[i] - min)/range)*255);
            output[i] = val;
        }

        return output;
    }

    // Sobel_x matrix
    static final int[][] sobelX = {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}
    };

    // Sobel_y matrix
    static final int[][] sobelY = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
    };

}
