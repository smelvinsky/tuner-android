package com.smelvinsky.tuner;

/**
 * Created by smelvinsky on 10.12.17.
 */

final public class SoundProcessing
{
    /*
     * Computes the discrete Fourier transform (DFT) of the given complex vector, storing the result back into the vector.
     * The vector's length must be a power of 2. Uses the Cooley-Tukey decimation-in-time radix-2 algorithm.
     *
     * Created only for processing only Realm Numbers
     */

    static double[] fft(double[] data)
    {
        //Checks if the length of input vector is a power of 2
        int vectorLength = data.length; //N
        int leadingZeros = Integer.numberOfLeadingZeros(vectorLength);
        if ((vectorLength << leadingZeros + 1) != 0)
        {
            throw new IllegalArgumentException("Vectors length is not a power of 2");
        }

        //Trigonometric tables
        double[] cosTable = new double[vectorLength / 2];
        for (int i = 0; i < vectorLength / 2; i++)
        {
            cosTable[i] = Math.cos(2 * Math.PI * i / vectorLength);
        }

        // Bit-reversed addressing permutation
        int power = 31 - leadingZeros;
        double[] temp;
        temp = data.clone();
        for (int i = 0; i < vectorLength; i++)
        {
            temp[i] = data[Integer.reverse(i) >>> (32 - power)];
        }
        data = temp;

        // Cooley-Tukey decimation-in-time radix-2 FFT
        for (int size = 2; size <= vectorLength; size *= 2)
        {
            int halfSize = size / 2;
            int tableStep = vectorLength / size;
            for (int i = 0; i < vectorLength; i += size)
            {
                for (int j = i, k = 0; j < i + halfSize; j++, k += tableStep)
                {
                    double tmpRe = data[j + halfSize] * cosTable[k];
                    data[j + halfSize] = data[j] - tmpRe;
                    data[j] += tmpRe;
                }
            }
            if (size == vectorLength)  // Prevent overflow in 'size *= 2'
            {
                break;
            }
        }
        return data;
    }

    static double[] getMagnitude(double[] real, double[] imag)
    {
        double magnitude[] = new double[real.length];

        for (int i = 0; i < real.length; i++)
        {
            magnitude[i] = Math.sqrt((real[i] * real[i]) + (imag[i] + imag[i]));
        }

        return magnitude;
    }
}
