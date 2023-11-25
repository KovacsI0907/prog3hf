package ImageProcessingAlgorithms;

import ParallelImageProcessing.ImageTile;

public class BilateralFilter extends TileProcessingAlgorithm{

    public final double spatialSigma;
    public final double intensitySigma;
    public final int kernelSize;

    public BilateralFilter(BilateralFilterParams params, ImageTile tile) {
        super(params, tile);

        this.spatialSigma = params.spatialSigma;
        this.intensitySigma = params.intensitySigma;
        this.kernelSize = params.kernelSize;
    }

    private double gaussianFunction(double x, double sigma){
        double multiplier = 1 / (sigma * Math.sqrt(2*Math.PI));
        double exponent = -0.5 * (x*x) / (sigma*sigma);
        return multiplier*Math.exp(exponent);
    }

    private double[] normalize(double[] input){
        double sum = 0;
        for(int i = 0;i<input.length;i++){
            sum += input[i];
        }

        for(int i = 0;i<input.length;i++){
            input[i] = input[i] / sum;
        }

        return input;
    }
    private double[] normalizeHalf(double[] input){
        double[] result = new double[input.length];
        double sum = 0;
        for(int i = 0;i<input.length;i++){
            sum += input[i]/2;
        }

        for(int i = 0;i<input.length;i++){
            result[i] = input[i] / sum;
        }

        return result;
    }

    private double[][] getSpatialKernel(int physicalKernelSize, double sigma){
        double[] preciseValues = new double[2*physicalKernelSize*1000];
        for(int i = 0;i<preciseValues.length;i++){
            preciseValues[i] = gaussianFunction(i*0.001,sigma);
        }
        preciseValues = normalizeHalf(preciseValues);

        int center = physicalKernelSize/2;
        double[][] lut = new double[physicalKernelSize][physicalKernelSize];
        for(int j = -center;j<=center;j++){
            for(int i = -center;i<=center;i++){
                double dist = Math.sqrt((i * i) + (j * j));
                int distInt = (int)(dist * 1000);
                lut[j + center][i + center] = preciseValues[distInt];
            }
        }

        return lut;
    }

    private double[] getIntensityKernel(double sigma){
        double[] kernel = new double[256];

        for(int i = 0;i<kernel.length;i++){
            kernel[i] = gaussianFunction(i, sigma);
        }

        return normalizeHalf(kernel);
    }

    private long getFilteredPixel(ImageTile tile, int x, int y, double[] intensityKernel, double[][] spatialKernel){
        double red = 0;
        double green = 0;
        double blue = 0;
        double redNorm = 0;
        double greenNorm = 0;
        double blueNorm = 0;

        int pixelRed = tile.getRedRO(x,y);
        int pixelGreen = tile.getGreenRO(x,y);
        int pixelBlue = tile.getBlueRO(x,y);

        int center = spatialKernel.length / 2;
        for(int j = -center;j<=center;j++){
            for(int i = -center;i<=center;i++){
                int currentRed = tile.getRedRO(x+i, y+j);
                int currentGreen = tile.getGreenRO(x+i, y+j);
                int currentBlue = tile.getBlueRO(x+i, y+j);

                double multRed = spatialKernel[j+center][i+center] * intensityKernel[(Math.abs(currentRed-pixelRed))];
                red += multRed*currentRed;
                redNorm += multRed;

                double multGreen = spatialKernel[j+center][i+center] * intensityKernel[(Math.abs(currentGreen-pixelGreen))];
                green += multGreen*currentGreen;
                greenNorm += multGreen;

                double multBlue = spatialKernel[j+center][i+center] * intensityKernel[(Math.abs(currentBlue-pixelBlue))];
                blue += multBlue*currentBlue;
                blueNorm += multBlue;
            }
        }


        //normalize
        long redRes = (long)(red/redNorm) & 0xFFL;
        long greenRes = (long)(green/greenNorm) & 0xFFL;
        long blueRes = (long)(blue/blueNorm) & 0xFFL;

        long result = tile.getPixelRelativeOriginal(x,y) & 0xFF000000L; // keep transparency
        result = result | (redRes << 16);
        result = result | (greenRes << 8);
        result = result | (blueRes << 0);

        return result;
    }

    @Override
    public ImageTile produceOutput() {
        long[][] outputData = new long[tile.height][tile.width];
        double[][] spatialKernel = getSpatialKernel(kernelSize, spatialSigma);
        double[] intensityKernel = getIntensityKernel(intensitySigma);

        for(int y = 0;y<tile.height;y++){
            for(int x = 0;x<tile.width;x++){
                outputData[y][x] = getFilteredPixel(tile, x, y, intensityKernel, spatialKernel);
            }
        }
        return new ImageTile(tile.width, tile.height, 0, tile.image, tile.tileIndex, outputData);
    }
}
