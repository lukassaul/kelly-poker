/*
*   Class   Stat
*
*   USAGE:  Statistical functions
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:    June 2002 as part of Fmath
*   AMENDED: 12 May 2003 Statistics separated out from Fmath as a new class
*   UPDATE:  18 June 2005, 5 January 2006, 25 April 2006, 12, 21 November 2006
*            4 December 2006 (renaming of cfd and pdf methods - older version also retained)
*            27 December 2006
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   Stat.html
*
*   Copyright (c) April 2004, June 2005, January 2006, December 2006
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/

package flanagan.analysis;

import java.util.*;
import flanagan.math.Fmath;
import flanagan.integration.Integration;
import flanagan.integration.IntegralFunction;
import flanagan.plot.PlotGraph;
import flanagan.complex.*;
import flanagan.roots.*;


// Class to evaluate the linear correlation coefficient probablity function
// Needed in calls to Integration.gaussQuad
class CorrCoeff implements IntegralFunction{

         public double a;

         public double function(double x){
                  double y = Math.pow((1.0D - x*x),a);
                  return y;
         }
}

//Statical methods class Stat
public class Stat{


        // A small number close to the smallest representable floating point number
        public static final double FPMIN = 1e-300;

        // PRIVATE MEMBERS FOR USE IN GAMMA FUNCTION METHODS AND HISTOGRAM CONSTRUCTION METHODS

        // GAMMA FUNCTIONS
        //  Lanczos Gamma Function approximation - N (number of coefficients -1)
        private static int lgfN = 6;
        //  Lanczos Gamma Function approximation - Coefficients
        private static double[] lgfCoeff = {1.000000000190015, 76.18009172947146, -86.50532032941677, 24.01409824083091, -1.231739572450155, 0.1208650973866179E-2, -0.5395239384953E-5};
        //  Lanczos Gamma Function approximation - small gamma
        private static double lgfGamma = 5.0;
        //  Maximum number of iterations allowed in Incomplete Gamma Function calculations
        private static int igfiter = 1000;
        //  Tolerance used in terminating series in Incomplete Gamma Function calculations
        private static double igfeps = 1e-8;

        // HISTOGRAM CONSTRUCTION
        //  Tolerance used in including an upper point in last histogram bin when it is outside due to riunding erors
        private static double histTol = 1.0001D;

        // METHODS

        // Arithmetic mean of a 1D array of doubles, aa
        public static double mean(double[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                return sum/((double)n);
        }

        // Weighted arithmetic mean of a 1D array of doubles, aa
        public static double mean(double[] aa, double[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                double sumx=0.0D;
                double sumw=0.0D;
                double weight = 0.0D;
                for(int i=0; i<n; i++){
                    weight = 1.0D/(ww[i]*ww[i]);
                    sumx+=aa[i]*weight;
                    sumw+=weight;
                }
                return sumx/sumw;
        }

        // Weighted arithmetic mean of a 1D array of floats, aa
        public static double mean(float[] aa, float[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                float sumx=0.0F;
                float sumw=0.0F;
                float weight = 0.0F;
                for(int i=0; i<n; i++){
                    weight = 1.0F/(ww[i]*ww[i]);
                    sumx+=aa[i]*weight;
                    sumw+=weight;
                }
                return sumx/sumw;
        }

        // Geometric mean of a 1D array of doubles, aa
        public static double geometricMean(double[] aa){
                int n = aa.length;
                double product=1.0D;
                for(int i=0; i<n; i++)product *= Math.pow(aa[i], 1.0D/((double)n));
                return product;
        }

        // Geometric mean of a 1D array of floats, aa
        public static float geometricMean(float[] aa){
                int n = aa.length;
                float product=1.0F;
                for(int i=0; i<n; i++)product *= (float)Math.pow(aa[i], 1.0F/((float)n));
                return product;
        }

        // Weighted geometric mean of a 1D array of doubles, aa
        public static double geometricMean(double[] aa, double[] ww){
                int n = aa.length;
                double sumW = 0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                    weight[i]=1.0D/(ww[i]*ww[i]);
                    sumW += ww[i];
                }
                double product=1.0D;
                for(int i=0; i<n; i++){
                    product *= Math.pow(aa[i], weight[i]/sumW);
                }
                return product;
        }

        // Weighted geometric mean of a 1D array of floats, aa
        public static float geometricMean(float[] aa, float[] ww){
                int n = aa.length;
                float sumW = 0.0F;
                float[] weight = new float[n];
                for(int i=0; i<n; i++){
                    weight[i]=1.0F/(ww[i]*ww[i]);
                    sumW += ww[i];
                }
                float product=1.0F;
                for(int i=0; i<n; i++){
                    product *= (float)Math.pow(aa[i], weight[i]/sumW);
                }
                return product;
        }

        // Harmonic mean of a 1D array of doubles, aa
        public static double harmonicMean(double[] aa){
                int n = aa.length;
                double sum = 0.0D;
                for(int i=0; i<n; i++)sum += 1.0D/aa[i];
                return (double)n/sum;
        }

        // Harmonic mean of a 1D array of floats, aa
        public static float harmonicMean(float[] aa){
                int n = aa.length;
                float sum = 0.0F;
                for(int i=0; i<n; i++)sum += 1.0F/aa[i];
                return (float)n/sum;
        }

        // Weighted harmonic mean of a 1D array of doubles, aa
        public static double harmonicMean(double[] aa, double[] ww){
                int n = aa.length;
                double sum = 0.0D;
                double sumW = 0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                    sumW += ww[i];
                    weight[i]=1.0D/(ww[i]*ww[i]);
                }
                for(int i=0; i<n; i++)sum += ww[i]/aa[i];
                return sumW/sum;
        }

        // Weighted harmonic mean of a 1D array of floats, aa
        public static float harmonicMean(float[] aa, float[] ww){
                int n = aa.length;
                float sum = 0.0F;
                float sumW = 0.0F;
                float[] weight = new float[n];
                for(int i=0; i<n; i++){
                    sumW += ww[i];
                    weight[i]=1.0F/(ww[i]*ww[i]);
                }
                for(int i=0; i<n; i++)sum += ww[i]/aa[i];
                return sumW/sum;
        }

        // Generalised mean of a 1D array of doubles, aa
        public static double generalisedMean(double[] aa, double m){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum += Math.pow(aa[i],m);
                }
                return Math.pow(sum/((double)n), 1.0D/m);
        }

        // Generalised mean of a 1D array of floats, aa
        public static float generalisedMean(float[] aa, float m){
                int n = aa.length;
                float sum=0.0F;
                for(int i=0; i<n; i++){
                        sum += Math.pow(aa[i],m);
                }
                return (float)Math.pow(sum/((float)n), 1.0F/m);
        }

        // Interquartile mean of a 1D array of doubles, aa
        public static double interQuartileMean(double[] aa){
                int n = aa.length;
                if(n<4)throw new IllegalArgumentException("At least 4 array elements needed");
                double[] bb = Fmath.selectionSort(aa);
                double sum = 0.0D;
                for(int i=n/4; i<3*n/4; i++)sum += bb[i];
                return 2.0*sum/(double)(n);
        }

        // Interquartile mean of a 1D array of floats, aa
        public static float interQuartileMean(float[] aa){
                int n = aa.length;
                if(n<4)throw new IllegalArgumentException("At least 4 array elements needed");
                float[] bb = Fmath.selectionSort(aa);
                float sum = 0.0F;
                for(int i=n/4; i<3*n/4; i++)sum += bb[i];
                return 2.0F*sum/(float)(n);
        }

       // Root mean square (rms) of a 1D array of doubles, aa
        public static double rms(double[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i]*aa[i];
                }
                return Math.sqrt(sum/((double)n));
        }

        // Root mean square (rms) of a 1D array of floats, aa
        public static float rms(float[] aa){
                int n = aa.length;
                float sum = 0.0F;
                for(int i=0; i<n; i++){
                        sum+=aa[i]*aa[i];
                }
                sum /= (float)n;

                return (float)Math.sqrt(sum);
        }

        // Arithmetic mean of a 1D array of floats, aa
        public static float mean(float[] aa){
                int n = aa.length;
                float sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                return sum/((float)n);
        }

        // Arithmetic mean of a 1D array of int, aa
        public static double mean(int[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                return sum/((double)n);
        }

             // Arithmetic mean of a 1D array of long, aa
        public static double mean(long[] aa){
                int n = aa.length;
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                return sum/((double)n);
        }

        // Arithmetic mean of a 1D array of Complex, aa
        public static Complex mean(Complex[] aa){
                int n = aa.length;
                Complex sum = new Complex(0.0D, 0.0D);
                for(int i=0; i<n; i++){
                        sum = sum.plus(aa[i]);
                }
                return sum.over((double)n);
        }

        // Median of a 1D array of doubles, aa
        public static double median(double[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                double med = 0.0D;
                double[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = bb[nOverTwo];
                }
                else{
                    med = (bb[nOverTwo-1]+bb[nOverTwo])/2.0D;
                }

                return med;
        }

        // Median of a 1D array of floats, aa
        public static float median(float[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                float med = 0.0F;
                float[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = bb[nOverTwo];
                }
                else{
                    med = (bb[nOverTwo-1]+bb[nOverTwo])/2.0F;
                }

                return med;
        }

        // Median of a 1D array of int, aa
        public static double median(int[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                double med = 0.0D;
                int[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = (double)bb[nOverTwo];
                }
                else{
                    med = (double)(bb[nOverTwo-1]+bb[nOverTwo])/2.0D;
                }

                return med;
        }

        // Median of a 1D array of long, aa
        public static double median(long[] aa){
                int n = aa.length;
                int nOverTwo = n/2;
                double med = 0.0D;
                long[] bb = Fmath.selectionSort(aa);
                if(Fmath.isOdd(n)){
                    med = (double)bb[nOverTwo];
                }
                else{
                    med = (double)(bb[nOverTwo-1]+bb[nOverTwo])/2.0D;
                }

                return med;
        }

        // Standard deviation of a 1D array of doubles, aa
        public static double standardDeviation(double[] aa){
                return Math.sqrt(variance(aa));
        }

        // Standard deviation of a 1D array of floats, aa
        public static float standardDeviation(float[] aa){
                return (float)Math.sqrt(variance(aa));
        }

        // Standard deviation of a 1D array of int, aa
        public static double standardDeviation(int[] aa){
                return Math.sqrt(variance(aa));
        }

        // Standard deviation of a 1D array of long, aa
        public static double standardDeviation(long[] aa){
                return Math.sqrt(variance(aa));
        }

        // Weighted standard deviation of a 1D array of doubles, aa
        public static double standardDeviation(double[] aa, double[] ww){
                if(aa.length!=ww.length)throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
                return Math.sqrt(variance(aa, ww));
        }

        // Weighted standard deviation of a 1D array of floats, aa
        public static float standardDeviation(float[] aa, float[] ww){
                if(aa.length!=ww.length)throw new IllegalArgumentException("length of variable array, " + aa.length + " and length of weight array, " + ww.length + " are different");
                return (float)Math.sqrt(variance(aa, ww));
        }



        // volatility   log  (doubles)
        public static double volatilityLogChange(double[] array){
            int n = array.length-1;
            double[] change = new double[n];
            for(int i=0; i<n; i++)change[i] = Math.log(array[i+1]/array[i]);
            return Stat.standardDeviation(change);
        }

        // volatility   log  (floats)
        public static float volatilityLogChange(float[] array){
            int n = array.length-1;
            float[] change = new float[n];
            for(int i=0; i<n; i++)change[i] = (float)Math.log(array[i+1]/array[i]);
            return Stat.standardDeviation(change);
        }

        // volatility   percentage (double)
        public static double volatilityPerCentChange(double[] array){
            int n = array.length-1;
            double[] change = new double[n];
            for(int i=0; i<n; i++)change[i] = (array[i+1] - array[i])*100.0D/array[i];
            return Stat.standardDeviation(change);
        }

        // volatility   percentage (float)
        public static double volatilityPerCentChange(float[] array){
            int n = array.length-1;
            float[] change = new float[n];
            for(int i=0; i<n; i++)change[i] = (array[i+1] - array[i])*100.0F/array[i];
            return Stat.standardDeviation(change);
        }

        // Coefficient of variation of an array of doubles
        public static double coefficientOfVariation(double[] array){
            return 100.0D*Stat.standardDeviation(array)/Math.abs(Stat.mean(array));
        }

        // Coefficient of variation of an array of float
        public static float coefficientOfVariation(float[] array){
            return 100.0F*Stat.standardDeviation(array)/Math.abs(Stat.mean(array));
        }

        // Subtract mean of an array from data array elements
        public static double[] subtractMean(double[] array){
            int n = array.length;
            double mean = Stat.mean(array);
            double[] arrayMinusMean = new double[n];
            for(int i=0; i<n; i++)arrayMinusMean[i] = array[i] - mean;

            return arrayMinusMean;
        }

        // Subtract mean of an array from data array elements
        public static float[] subtractMean(float[] array){
            int n = array.length;
            float mean = Stat.mean(array);
            float[] arrayMinusMean = new float[n];
            for(int i=0; i<n; i++)arrayMinusMean[i] = array[i] - mean;

            return arrayMinusMean;
        }

        // Variance of a 1D array of doubles, aa
        public static double variance(double[] aa){
                int n = aa.length;
                double sum=0.0D, mean=0.0D;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                mean=sum/((double)n);
                sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square(aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

        // Variance of a 1D array of floats, aa
        public static float variance(float[] aa){
                int n = aa.length;
                float sum=0.0F, mean=0.0F;
                for(int i=0; i<n; i++){
                        sum+=aa[i];
                }
                mean=sum/((float)n);
                sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square(aa[i]-mean);
                }
                return sum/((float)(n-1));
        }

       // Variance of a 1D array of int, aa
        public static double variance(int[] aa){
                int n = aa.length;
                double sum=0.0D, mean=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                mean=sum/((double)n);
                sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square((double)aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

       // Variance of a 1D array of ilong, aa
        public static double variance(long[] aa){
                int n = aa.length;
                double sum=0.0D, mean=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(double)aa[i];
                }
                mean=sum/((double)n);
                sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=Fmath.square((double)aa[i]-mean);
                }
                return sum/((double)(n-1));
        }

        // Weighted variance of a 1D array of doubles, aa
        public static double variance(double[] aa, double[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                double sumx=0.0D, sumw=0.0D, mean=0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                        sumx+=aa[i];
                        weight[i]=1.0D/(ww[i]*ww[i]);
                        sumw+=weight[i];
                }
                mean=sumx/sumw;
                sumx=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=weight[i]*Fmath.square(aa[i]-mean);
                }
                return sumx*(double)(n)/((double)(n-1)*sumw);
        }

        // Weighted variance of a 1D array of floats, aa
        public static float variance(float[] aa, float[] ww){
                int n = aa.length;
                if(n!=ww.length)throw new IllegalArgumentException("length of variable array, " + n + " and length of weight array, " + ww.length + " are different");
                float sumx=0.0F, sumw=0.0F, mean=0.0F;
                float[] weight = new float[n];
                for(int i=0; i<n; i++){
                        sumx+=aa[i];
                        weight[i]=1.0F/(ww[i]*ww[i]);
                        sumw+=weight[i];
                }
                mean=sumx/sumw;
                sumx=0.0F;
                for(int i=0; i<n; i++){
                        sumx+=weight[i]*Fmath.square(aa[i]-mean);
                }
                return sumx*(float)(n)/((float)(n-1)*sumw);
        }

        // Covariance of two 1D arrays of doubles, xx and yy
        public static double covariance(double[] xx, double[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                double sumx=0.0D, meanx=0.0D;
                double sumy=0.0D, meany=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=xx[i];
                        sumy+=yy[i];
                }
                meanx=sumx/((double)n);
                meany=sumy/((double)n);
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=(xx[i]-meanx)*(yy[i]-meany);
                }
                return sum/((double)(n-1));
        }

        // Covariance of two 1D arrays of floats, xx and yy
        public static float covariance(float[] xx, float[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                float sumx=0.0F, meanx=0.0F;
                float sumy=0.0F, meany=0.0F;
                for(int i=0; i<n; i++){
                        sumx+=xx[i];
                        sumy+=yy[i];
                }
                meanx=sumx/((float)n);
                meany=sumy/((float)n);
                float sum=0.0F;
                for(int i=0; i<n; i++){
                        sum+=(xx[i]-meanx)*(yy[i]-meany);
                }
                return sum/((float)(n-1));
        }

        // Covariance of two 1D arrays of ints, xx and yy
        public static double covariance(int[] xx, int[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                double sumx=0.0D, meanx=0.0D;
                double sumy=0.0D, meany=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=(double)xx[i];
                        sumy+=(double)yy[i];
                }
                meanx=sumx/((double)n);
                meany=sumy/((double)n);
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=((double)xx[i]-meanx)*((double)yy[i]-meany);
                }
                return sum/((double)(n-1));
        }

        // Covariance of two 1D arrays of ints, xx and yy
        public static double covariance(long[] xx, long[] yy){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");

                double sumx=0.0D, meanx=0.0D;
                double sumy=0.0D, meany=0.0D;
                for(int i=0; i<n; i++){
                        sumx+=(double)xx[i];
                        sumy+=(double)yy[i];
                }
                meanx=sumx/((double)n);
                meany=sumy/((double)n);
                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=((double)xx[i]-meanx)*((double)yy[i]-meany);
                }
                return sum/((double)(n-1));
        }

        // Weighted covariance of two 1D arrays of doubles, xx and yy with weights ww
        public static double covariance(double[] xx, double[] yy, double[] ww){
                int n = xx.length;
                if(n!=yy.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of y array, " + yy.length + " are different");
                if(n!=ww.length)throw new IllegalArgumentException("length of x variable array, " + n + " and length of weight array, " + yy.length + " are different");
                double sumx=0.0D, sumy=0.0D, sumw=0.0D, meanx=0.0D, meany=0.0D;
                double[] weight = new double[n];
                for(int i=0; i<n; i++){
                        sumx+=xx[i];
                        sumy+=yy[i];
                        weight[i]=1.0D/(ww[i]*ww[i]);
                        sumw+=weight[i];
                }
                meanx=sumx/sumw;
                meany=sumy/sumw;

                double sum=0.0D;
                for(int i=0; i<n; i++){
                        sum+=weight[i]*(xx[i]-meanx)*(yy[i]-meany);
                }
                return sum*(double)(n)/((double)(n-1)*sumw);
        }


        // GAMMA DISTRIBUTION AND FUNCTIONS
        // Gamma distribution - three parameter
        // cumulative distribution function
        public static double gammaCDF(double mu, double beta, double gamma, double upperLimit){
            if(upperLimit<mu)throw new IllegalArgumentException("The upper limit, " + upperLimit + "must be equal to or greater than the location parameter, " + mu);
            if(beta<=0.0D)throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
            if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
            double xx = (upperLimit - mu)/beta;
            return regularisedGammaFunction(gamma, xx);
        }

        // Gamma distribution - standard
        // cumulative distribution function
        public static double gammaCDF(double gamma, double upperLimit){
            if(upperLimit<0.0D)throw new IllegalArgumentException("The upper limit, " + upperLimit + "must be equal to or greater than zero");
            if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
            return regularisedGammaFunction(gamma, upperLimit);
        }

        // Gamma distribution - three parameter
        // probablity density function
        public static double gammaPDF(double mu, double beta, double gamma, double x){
            if(x<mu)throw new IllegalArgumentException("The variable, x, " + x + "must be equal to or greater than the location parameter, " + mu);
            if(beta<=0.0D)throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
            if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
            double xx = (x - mu)/beta;
            return Math.pow(xx, gamma-1)*Math.exp(-xx)/(beta*gammaFunction(gamma));
        }

        // Gamma distribution - standard
        // probablity density function
        public static double gammaPDF(double gamma, double x){
            if(x<0.0D)throw new IllegalArgumentException("The variable, x, " + x + "must be equal to or greater than zero");
            if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
            return Math.pow(x, gamma-1)*Math.exp(-x)/gammaFunction(gamma);
        }

        // Gamma distribution - three parameter
        // mean
        public static double gammaMean(double mu, double beta, double gamma){
            if(beta<=0.0D)throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
            if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
            return gamma*beta - mu;
        }

        // Gamma distribution - three parameter
        // mode
        public static double gammaMode(double mu, double beta, double gamma){
            if(beta<=0.0D)throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
            if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
            double mode = Double.NaN;
            if(gamma>=1.0D)mode = (gamma-1.0D)*beta - mu;
            return mode;
        }

        // Gamma distribution - three parameter
        // standard deviation
        public static double gammaStandDev(double mu, double beta, double gamma){
            if(beta<=0.0D)throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
            if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
            return Math.sqrt(gamma)*beta;
        }


    // Returns an array of Gamma random deviates - clock seed
    public static double[] gammaRand(double mu, double beta, double gamma, int n){
        if(beta<=0.0D)throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
        if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
        Random rr = new Random();
        return gammaRandGen(rr, mu, beta, gamma, n);
    }

    // Returns an array of Gamma random deviates - user supplied seed
    public static double[] gammaRand(double mu, double beta, double gamma, int n, long seed){
        if(beta<=0.0D)throw new IllegalArgumentException("The scale parameter, " + beta + "must be greater than zero");
        if(gamma<=0.0D)throw new IllegalArgumentException("The shape parameter, " + gamma + "must be greater than zero");
        Random rr = new Random(seed);
        return gammaRandGen(rr, mu, beta, gamma, n);
    }

    // private method for generating an array of Gamma random deviates
    private static double[] gammaRandGen(Random rr, double mu, double beta, double gamma, int n){
        double[] ran = new double[n];

        // Create instance of the class holding the Gaussian cfd function
        GammaFunct gart = new GammaFunct();

        // set function variables
        gart.mu = mu;
        gart.beta = beta;
        gart.gamma = gamma;

        // Set initial range for search
        double range = Math.sqrt(gamma)*beta;

        // required tolerance
        double tolerance = 1e-10;

        for(int i=0; i<n; i++){

            // lower bound
            double lowerBound = mu;
            // upper bound
            double upperBound = mu + 5.0D*range;
            if(upperBound<=lowerBound)upperBound += 5.0D*range;

            //  set functioncfd  variables
            gart.cfd = rr.nextDouble();

            // Create instance of RealRoot
            RealRoot realR = new RealRoot();

            // Set extension limits
            realR.noLowerBoundExtension();

            // Set tolerance
            realR.setTolerance(tolerance);

            // call root searching method, bisectNewtonRaphson
            ran[i] = realR.falsePosition(gart, lowerBound, upperBound);
        }

        return ran;

    }

        // Gamma function
        // Lanczos approximation (6 terms)
        public static double gammaFunction(double x){

                double xcopy = x;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];
                double fg = 0.0D;

                if(x>=0.0){
                        if(x>=1.0D && x-(int)x==0.0D){
                                fg = Stat.factorial(x)/x;
                        }
                        else{
                                first = Math.pow(first, x + 0.5)*Math.exp(-first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = first*Math.sqrt(2.0*Math.PI)*second/x;
                        }
                }
                else{
                         fg = -Math.PI/(x*Stat.gamma(-x)*Math.sin(Math.PI*x));
                }
                return fg;
        }

        // Gamma function
        // Lanczos approximation (6 terms)
        // retained for backward compatibity
        public static double gamma(double x){

                double xcopy = x;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];
                double fg = 0.0D;

                if(x>=0.0){
                        if(x>=1.0D && x-(int)x==0.0D){
                                fg = Stat.factorial(x)/x;
                        }
                        else{
                                first = Math.pow(first, x + 0.5)*Math.exp(-first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = first*Math.sqrt(2.0*Math.PI)*second/x;
                        }
                }
                else{
                         fg = -Math.PI/(x*Stat.gamma(-x)*Math.sin(Math.PI*x));
                }
                return fg;
        }

        // Return the Lanczos constant gamma
        public static double getLanczosGamma(){
                return Stat.lgfGamma;
        }

        // Return the Lanczos constant N (number of coeeficients + 1)
        public static int getLanczosN(){
                return Stat.lgfN;
        }

        // Return the Lanczos coeeficients
        public static double[] getLanczosCoeff(){
                int n = Stat.getLanczosN()+1;
                double[] coef = new double[n];
                for(int i=0; i<n; i++){
                        coef[i] = Stat.lgfCoeff[i];
                }
                return coef;
        }

        // Return the nearest smallest representable floating point number to zero with mantissa rounded to 1.0
        public static double getFpmin(){
                return Stat.FPMIN;
        }

        // log to base e of the Gamma function
        // Lanczos approximation (6 terms)
        public static double logGammaFunction(double x){
                double xcopy = x;
                double fg = 0.0D;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];

                if(x>=0.0){
                        if(x>=1.0 && x-(int)x==0.0){
                                fg = Stat.logFactorial(x)-Math.log(x);
                        }
                        else{
                                first -= (x + 0.5)*Math.log(first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = Math.log(Math.sqrt(2.0*Math.PI)*second/x) - first;
                        }
                }
                else{
                        fg = Math.PI/(Stat.gamma(1.0D-x)*Math.sin(Math.PI*x));

                        if(fg!=1.0/0.0 && fg!=-1.0/0.0){
                                if(fg<0){
                                         throw new IllegalArgumentException("\nThe gamma function is negative");
                                }
                                else{
                                        fg = Math.log(fg);
                                }
                        }
                }
                return fg;
        }

        // log to base e of the Gamma function
        // Lanczos approximation (6 terms)
        // Retained for backward compatibility
        public static double logGamma(double x){
                double xcopy = x;
                double fg = 0.0D;
                double first = x + lgfGamma + 0.5;
                double second = lgfCoeff[0];

                if(x>=0.0){
                        if(x>=1.0 && x-(int)x==0.0){
                                fg = Stat.logFactorial(x)-Math.log(x);
                        }
                        else{
                                first -= (x + 0.5)*Math.log(first);
                                for(int i=1; i<=lgfN; i++)second += lgfCoeff[i]/++xcopy;
                                fg = Math.log(Math.sqrt(2.0*Math.PI)*second/x) - first;
                        }
                }
                else{
                        fg = Math.PI/(Stat.gamma(1.0D-x)*Math.sin(Math.PI*x));

                        if(fg!=1.0/0.0 && fg!=-1.0/0.0){
                                if(fg<0){
                                         throw new IllegalArgumentException("\nThe gamma function is negative");
                                }
                                else{
                                        fg = Math.log(fg);
                                }
                        }
                }
                return fg;
        }



        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        public static double regularisedGammaFunction(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                double igf = 0.0D;

                if(x < a+1.0D){
                        // Series representation
                        igf = incompleteGammaSer(a, x);
                }
                else{
                        // Continued fraction representation
                        igf = incompleteGammaFract(a, x);
                }
                return igf;
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        public static double regularizedGammaFunction(double a, double x){
            return regularisedGammaFunction(a, x);
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Retained for backward compatibility
        public static double regIncompleteGamma(double a, double x){
            return regularisedGammaFunction(a, x);
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Retained for backward compatibility
        public static double incompleteGamma(double a, double x){
            return regularisedGammaFunction(a, x);
        }

        // Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
        public static double complementaryRegularisedGammaFunction(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                double igf = 0.0D;

                if(x!=0.0D){
                        if(x==1.0D/0.0D)
                        {
                                igf=1.0D;
                        }
                        else{
                                if(x < a+1.0D){
                                        // Series representation
                                        igf = 1.0D - incompleteGammaSer(a, x);
                                }
                                else{
                                        // Continued fraction representation
                                        igf = 1.0D - incompleteGammaFract(a, x);
                                }
                        }
                }
                return igf;
        }

        // Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
        public static double complementaryRegularizedGammaFunction(double a, double x){
            return complementaryRegularisedGammaFunction(a , x);
        }

        // Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
        // Retained for backward compatibility
        public static double incompleteGammaComplementary(double a, double x){
            return complementaryRegularisedGammaFunction(a , x);
        }

        // Complementary Regularised Incomplete Gamma Function Q(a,x) = 1 - P(a,x) = 1 - integral from zero to x of (exp(-t)t^(a-1))dt
        // Retained for backward compatibility
        public static double regIncompleteGammaComplementary(double a, double x){
                return complementaryRegularisedGammaFunction(a , x);
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Series representation of the function - valid for x < a + 1
        public static double incompleteGammaSer(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x>=a+1) throw new IllegalArgumentException("\nx >= a+1   use Continued Fraction Representation");

                int i = 0;
                double igf = 0.0D;
                boolean check = true;

                double acopy = a;
                double sum = 1.0/a;
                double incr = sum;
                double loggamma = Stat.logGamma(a);

                while(check){
                        ++i;
                        ++a;
                        incr *= x/a;
                        sum += incr;
                        if(Math.abs(incr) < Math.abs(sum)*Stat.igfeps){
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                check = false;
                        }
                        if(i>=Stat.igfiter){
                                check=false;
                                igf = sum*Math.exp(-x+acopy*Math.log(x)- loggamma);
                                System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaSer().\nCurrent value returned.\nIncrement = "+String.valueOf(incr)+".\nSum = "+String.valueOf(sum)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                return igf;
        }

        // Regularised Incomplete Gamma Function P(a,x) = integral from zero to x of (exp(-t)t^(a-1))dt
        // Continued Fraction representation of the function - valid for x >= a + 1
        // This method follows the general procedure used in Numerical Recipes for C,
        // The Art of Scientific Computing
        // by W H Press, S A Teukolsky, W T Vetterling & B P Flannery
        // Cambridge University Press,   http://www.nr.com/
        public static double incompleteGammaFract(double a, double x){
                if(a<0.0D  || x<0.0D)throw new IllegalArgumentException("\nFunction defined only for a >= 0 and x>=0");
                if(x<a+1) throw new IllegalArgumentException("\nx < a+1   Use Series Representation");

                int i = 0;
                double ii = 0;
                double igf = 0.0D;
                boolean check = true;

                double loggamma = Stat.logGamma(a);
                double numer = 0.0D;
                double incr = 0.0D;
                double denom = x - a + 1.0D;
                double first = 1.0D/denom;
                double term = 1.0D/FPMIN;
                double prod = first;

                while(check){
                        ++i;
                        ii = (double)i;
                        numer = -ii*(ii - a);
                        denom += 2.0D;
                        first = numer*first + denom;
                        if(Math.abs(first) < Stat.FPMIN){
                            first = Stat.FPMIN;
                        }
                        term = denom + numer/term;
                        if(Math.abs(term) < Stat.FPMIN){
                            term = Stat.FPMIN;
                         }
                        first = 1.0D/first;
                        incr = first*term;
                        prod *= incr;
                        if(Math.abs(incr - 1.0D) < igfeps)check = false;
                        if(i>=Stat.igfiter){
                                check=false;
                                System.out.println("\nMaximum number of iterations were exceeded in Stat.incompleteGammaFract().\nCurrent value returned.\nIncrement - 1 = "+String.valueOf(incr-1)+".\nTolerance =  "+String.valueOf(igfeps));
                        }
                }
                igf = 1.0D - Math.exp(-x+a*Math.log(x)-loggamma)*prod;
                return igf;
        }

        // Reset the maximum number of iterations allowed in the calculation of the incomplete gamma functions
        public static void setIncGammaMaxIter(int igfiter){
                Stat.igfiter=igfiter;
        }

        // Return the maximum number of iterations allowed in the calculation of the incomplete gamma functions
        public static int getIncGammaMaxIter(){
                return Stat.igfiter;
        }

        // Reset the tolerance used in the calculation of the incomplete gamma functions
        public static void setIncGammaTol(double igfeps){
                Stat.igfeps=igfeps;
        }

        // Return the tolerance used in the calculation of the incomplete gamm functions
        public static double getIncGammaTol(){
                return Stat.igfeps;
        }

        // ERLANG DISTRIBUTION
        // Erlang distribution
        // cumulative distribution function
        public static double erlangCDF(double lambda, int kay, double upperLimit){
            return gammaCDF(0.0D, 1.0D/lambda, (double)kay, upperLimit);
        }

        public static double erlangCDF(double lambda, long kay, double upperLimit){
            return gammaCDF(0.0D, 1.0D/lambda, (double)kay, upperLimit);
        }

        public static double erlangCDF(double lambda, double kay, double upperLimit){
            if(kay - Math.round(kay)!=0.0D)throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
            return gammaCDF(0.0D, 1.0D/lambda, kay, upperLimit);
        }

        // Erlang distribution
        // probablity density function
        public static double erlangPDF(double lambda, int kay, double x){
            return  gammaPDF(0.0D, 1.0D/lambda, (double)kay, x);
        }

        public static double erlangPDF(double lambda, long kay, double x){
            return  gammaPDF(0.0D, 1.0D/lambda, (double)kay, x);
        }

        public static double erlangPDF(double lambda, double kay, double x){
            if(kay - Math.round(kay)!=0.0D)throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");

            return  gammaPDF(0.0D, 1.0D/lambda, kay, x);
        }

        // Erlang distribution
        // mean
        public static double erlangMean(double lambda, int kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return (double)kay/lambda;
        }

        public static double erlangMean(double lambda, long kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return (double)kay/lambda;
        }

        public static double erlangMean(double lambda, double kay){
            if(kay - Math.round(kay)!=0.0D)throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return kay/lambda;
        }

        // erlang distribution
        // mode
        public static double erlangMode(double lambda, int kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            double mode = Double.NaN;
            if(kay>=1)mode = ((double)kay-1.0D)/lambda;
            return mode;
        }

        public static double erlangMode(double lambda, long kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            double mode = Double.NaN;
            if(kay>=1)mode = ((double)kay-1.0D)/lambda;
            return mode;
        }

        public static double erlangMode(double lambda, double kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            if(kay - Math.round(kay)!=0.0D)throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
            double mode = Double.NaN;
            if(kay>=1)mode = (kay-1.0D)/lambda;
            return mode;
        }


        // Erlang distribution
        // standard deviation
        public static double erlangStandDev(double lambda, int kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return Math.sqrt((double)kay)/lambda;
        }

        public static double erlangStandDev(double lambda, long kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return Math.sqrt((double)kay)/lambda;
        }

        public static double erlangStandDev(double lambda, double kay){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            if(kay - Math.round(kay)!=0.0D)throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
            return Math.sqrt(kay)/lambda;
        }

        // Returns an array of Erlang random deviates - clock seed
        public static double[] erlangRand(double lambda, int kay, int n){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return gammaRand(0.0D, 1.0D/lambda, (double) kay, n);
        }

        public static double[] erlangRand(double lambda, long kay, int n){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return gammaRand(0.0D, 1.0D/lambda, (double) kay, n);
        }

        public static double[] erlangRand(double lambda, double kay, int n){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            if(kay - Math.round(kay)!=0.0D)throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
            return gammaRand(0.0D, 1.0D/lambda, kay, n);
        }

       // Returns an array of Erlang random deviates - user supplied seed
        public static double[] erlangRand(double lambda, int kay, int n, long seed){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return gammaRand(0.0D, 1.0D/lambda, (double) kay, n, seed);
        }

        public static double[] erlangRand(double lambda, long kay, int n, long seed){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            return gammaRand(0.0D, 1.0D/lambda, (double) kay, n, seed);
        }

        public static double[] erlangRand(double lambda, double kay, int n, long seed){
            if(kay<1)throw new IllegalArgumentException("The rate parameter, " + kay + "must be equal to or greater than one");
            if(kay - Math.round(kay)!=0.0D)throw new IllegalArgumentException("kay must, mathematically, be an integer even though it may be entered as a double\nTry the Gamma distribution instead of the Erlang distribution");
            return gammaRand(0.0D, 1.0D/lambda, kay, n, seed);
        }

        // ERLANG CONNECTIONS BUSY, B AND C EQUATIONS
        // returns the probablility that m resources (connections) are busy
        // totalTraffic:    total traffic in Erlangs
        // totalResouces:   total number of resources in the system
        public static double erlangMprobability(double totalTraffic, double totalResources, double em){
            double prob = 0.0D;
            if(totalTraffic>0.0D){

                double numer = totalResources*Math.log(em) - Fmath.logFactorial(em);
                double denom = 1.0D;
                double lastTerm = 1.0D;
                for(int i=1; i<=totalResources; i++){
                    lastTerm = lastTerm*totalTraffic/(double)i;
                    denom += lastTerm;
                }
                denom = Math.log(denom);
                prob = numer - denom;
                prob = Math.exp(prob);
            }
            return prob;
        }

        public static double erlangMprobability(double totalTraffic, long totalResources, long em){
            return erlangMprobability(totalTraffic, (double)totalResources, (double)em);
        }

        public static double erlangMprobability(double totalTraffic, int totalResources, int em){
            return erlangMprobability(totalTraffic, (double)totalResources, (double)em);
        }


        // Erlang B equation
        // returns the probablility that a customer will be rejected due to lack of resources
        // totalTraffic:    total traffic in Erlangs
        // totalResouces:   total number of resources in the system
        public static double erlangBprobability(double totalTraffic, double totalResources){
            double prob = 0.0D;
            if(totalTraffic>0.0D){
                double numer = totalResources*Math.log(totalTraffic) - Fmath.logFactorial(totalResources);
                double denom = 1.0D;
                double lastTerm = 1.0D;
                double iCount = 1.0D;
                while(iCount<=totalResources){
                    lastTerm = lastTerm*totalTraffic/iCount;
                    denom += lastTerm;
                    iCount = iCount + 1.0D;
                }
                denom = Math.log(denom);
                prob = numer - denom;
                prob = Math.exp(prob);
            }
            return prob;
        }

        public static double erlangBprobability(double totalTraffic, long totalResources){
            return erlangBprobability(totalTraffic, (double)totalResources);
        }

        public static double erlangBprobability(double totalTraffic, int totalResources){
            return erlangBprobability(totalTraffic, (double)totalResources);
        }

        // Erlang B equation
        // returns the maximum total traffic in Erlangs
        // blockingProbability:    probablility that a customer will be rejected due to lack of resources
        // totalResouces:   total number of resources in the system
        public static double erlangBload(double blockingProbability, double totalResources){

            // Create instance of the class holding the Erlang B equation
            ErlangBfunct eBfunc = new ErlangBfunct();

            // Set instance variables
            eBfunc.blockingProbability = blockingProbability;
            eBfunc.totalResources = totalResources;

            // lower bound
            double lowerBound = 0.0D;
            // upper bound
            double upperBound = totalResources*0.999999D;
            // required tolerance
            double tolerance = 1e-6;

            // Create instance of RealRoot
            RealRoot realR = new RealRoot();

            // Set tolerance
            realR.setTolerance(tolerance);

            // Set bounds limits
            realR.noLowerBoundExtension();

            // call root searching method
            double root = realR.bisect(eBfunc, lowerBound, upperBound);

            return root;
        }

        public static double erlangBload(double blockingProbability, long totalResources){
            return erlangBload(blockingProbability, (double)totalResources);
        }

        public static double erlangBload(double blockingProbability, int totalResources){
            return erlangBload(blockingProbability, (double)totalResources);
        }

        // Erlang C equation
        // returns the probablility that a customer will receive a non-zero delay in obtaining obtaining a resource
        // totalTraffic:    total traffic in Erlangs
        // totalResouces:   total number of resources in the system
        public static double erlangCprobability(double totalTraffic, double totalResources){
            double prob = 0.0D;
            if(totalTraffic>0.0D){

                double numer = totalResources*Math.log(totalTraffic) - Fmath.logFactorial(totalResources);
                numer = Math.exp(numer)*totalResources/(totalResources - totalTraffic);
                double denom = 1.0D;
                double lastTerm = 1.0D;
                double iCount = 1.0D;
                while(iCount<=totalResources){
                    lastTerm = lastTerm*totalTraffic/iCount;
                    denom += lastTerm;
                    iCount = iCount + 1.0D;
                }
                denom += numer;
                prob = numer/denom;
            }
            return prob;
        }

        public static double erlangCprobability(double totalTraffic, long totalResources){
            return erlangCprobability(totalTraffic, (double)totalResources);
        }

        public static double erlangCprobability(double totalTraffic, int totalResources){
            return erlangCprobability(totalTraffic, (double)totalResources);
        }

        // Erlang C equation
        // returns the maximum total traffic in Erlangs
        // nonZeroDelayProbability:    probablility that a customer will receive a non-zero delay in obtaining obtaining a resource
        // totalResouces:   total number of resources in the system
        public static double erlangCload(double nonZeroDelayProbability, double totalResources){

            // Create instance of the class holding the Erlang C equation
            ErlangCfunct eCfunc = new ErlangCfunct();

            // Set instance variables
            eCfunc.nonZeroDelayProbability = nonZeroDelayProbability;
            eCfunc.totalResources = totalResources;

            // lower bound
            double lowerBound = 0.0D;
            // upper bound
            double upperBound = 1.0D;
            // required tolerance
            double tolerance = 1e-6;

            // Create instance of RealRoot
            RealRoot realR = new RealRoot();

            // Set tolerance
            realR.setTolerance(tolerance);

            // Set bounds limits
            realR.noLowerBoundExtension();

            // call root searching method
            double root = realR.bisect(eCfunc, lowerBound, upperBound);

            return root;
        }

        public static double erlangCload(double nonZeroDelayProbability, long totalResources){
            return erlangCload(nonZeroDelayProbability, (double)totalResources);
        }

        public static double erlangCload(double nonZeroDelayProbability, int totalResources){
            return erlangCload(nonZeroDelayProbability, (double)totalResources);
        }

        // ENGSET EQUATION
        // returns the probablility that a customer will be rejected due to lack of resources
        // offeredTraffic:  total offeredtraffic in Erlangs
        // totalResouces:   total number of resources in the system
        // numberOfSources: number of sources
        public static double engsetProbability(double offeredTraffic, double totalResources, double numberOfSources){
            if(totalResources>numberOfSources-1)throw new IllegalArgumentException("total resources, " + totalResources + ", must be less than or  equal to the number of sources minus one, " + (numberOfSources - 1));

            double prob = 0.0D;
            if(totalResources==0.0D){
                prob = 1.0D;
            }
            else{
                if(offeredTraffic==0.0D){
                    prob = 0.0D;
                }
                else{
                    // Set boundaries to the probability
                    double lowerBound = 0.0D;
                    double upperBound = 1.0D;

                    // Create instance of Engset Probability Function
                    EngsetProb engProb = new EngsetProb();

                    // Set function variables
                    engProb.offeredTraffic = offeredTraffic;
                    engProb.totalResources = totalResources;
                    engProb.numberOfSources = numberOfSources;

                    // Perform a root search
                    RealRoot eprt = new RealRoot();

                    prob = eprt.bisect(engProb, lowerBound, upperBound);
                }
            }
            return prob;
        }

        public static double engsetProbability(double offeredTraffic, long totalResources, long numberOfSources){
            return engsetProbability(offeredTraffic, (double)totalResources, (double)numberOfSources);
        }

        public static double engsetProbability(double offeredTraffic, int totalResources, int numberOfSources){
            return engsetProbability(offeredTraffic, (double)totalResources, (double)numberOfSources);
        }

        // Engset equation
        // returns the maximum total traffic in Erlangs
        // blockingProbability:    probablility that a customer will be rejected due to lack of resources
        // totalResouces:   total number of resources in the system
        // numberOfSources: number of sources
        public static double engsetLoad(double blockingProbability, double totalResources, double numberOfSources){

            // Create instance of the class holding the Engset Load equation
            EngsetLoad eLfunc = new EngsetLoad();

            // Set instance variables
            eLfunc.blockingProbability = blockingProbability;
            eLfunc.totalResources = totalResources;
            eLfunc.numberOfSources = numberOfSources;

            // lower bound
            double lowerBound = 0.0D;
            // upper bound
            double upperBound = totalResources*0.999999D;
            // required tolerance
            double tolerance = 1e-6;

            // Create instance of RealRoot
            RealRoot realR = new RealRoot();

            // Set tolerance
            realR.setTolerance(tolerance);

            // Set bounds limits
            realR.noLowerBoundExtension();

            // call root searching method
            double root = realR.bisect(eLfunc, lowerBound, upperBound);

            return root;
        }

        public static double ensetLoad(double blockingProbability, long totalResources, long numberOfSources){
            return engsetLoad(blockingProbability, (double) totalResources, (double) numberOfSources);
        }

        public static double ensetLoad(double blockingProbability, int totalResources, int numberOfSources){
            return engsetLoad(blockingProbability, (double) totalResources, (double) numberOfSources);
        }

        // BETA DISTRIBUTIONS AND FUNCTIONS
        // beta distribution cdf
        public static double betaCDF(double alpha, double beta, double limit){
            return betaCDF(0.0D, 1.0D, alpha, beta, limit);
        }

        // beta distribution pdf
        public static double betaCDF(double min, double max, double alpha, double beta, double limit){
            if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
            if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
            if(limit<min)throw new IllegalArgumentException("limit, " + limit + ", must be greater than or equal to the minimum value, " + min);
            if(limit>max)throw new IllegalArgumentException("limit, " + limit + ", must be less than or equal to the maximum value, " + max);
            return Stat.regularisedBetaFunction(alpha, beta, (limit-min)/(max-min));
        }


        // beta distribution pdf
        public static double betaPDF(double alpha, double beta, double x){
            return betaPDF(0.0D, 1.0D, alpha, beta, x);
        }

        // beta distribution pdf
        public static double betaPDF(double min, double max, double alpha, double beta, double x){
            if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
            if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
            if(x<min)throw new IllegalArgumentException("x, " + x + ", must be greater than or equal to the minimum value, " + min);
            if(x>max)throw new IllegalArgumentException("x, " + x + ", must be less than or equal to the maximum value, " + max);
            double pdf = Math.pow(x - min, alpha - 1)*Math.pow(max - x, beta - 1)/Math.pow(max - min, alpha + beta - 1);
            return pdf/Stat.betaFunction(alpha, beta);
        }

    // Returns an array of Beta random deviates - clock seed
    public static double[] betaRand(double alpha, double beta, int n){
        if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
        if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
        Random rr = new Random();
        return betaRandGen(rr, 0.0D, 1.0D, alpha, beta, n);
    }

    // Returns an array of Beta random deviates - clock seed
    public static double[] betaRand(double min, double max, double alpha, double beta, int n){
        if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
        if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
        Random rr = new Random();
        return betaRandGen(rr, min, max, alpha, beta, n);
    }


    // Returns an array of Beta random deviates - user supplied seed
    public static double[] betaRand(double alpha, double beta, int n, long seed){
        if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
        if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter,  beta, " + beta + "must be greater than zero");
        Random rr = new Random(seed);
        return betaRandGen(rr, 0.0D, 1.0D, alpha, beta, n);
    }

    // Returns an array of Beta random deviates - user supplied seed
    public static double[] betaRand(double min, double max, double alpha, double beta, int n, long seed){
        if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
        if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter,  beta, " + beta + "must be greater than zero");
        Random rr = new Random(seed);
        return betaRandGen(rr, min, max, alpha, beta, n);
    }

    // private method for generating an array of Gamma random deviates
    private static double[] betaRandGen(Random rr, double min, double max, double alpha, double beta, int n){
        double[] ran = new double[n];

        // Create instance of the class holding the Gaussian cfd function
        BetaFunct bart = new BetaFunct();

        // set function variables
        bart.alpha = alpha;
        bart.beta = beta;
        bart.min = min;
        bart.max = max;

        // required tolerance
        double tolerance = 1e-10;

        for(int i=0; i<n; i++){

            // lower bound
            double lowerBound = min;
            // upper bound
            double upperBound = max;

            //  set functioncfd  variables
            bart.cfd = rr.nextDouble();

            // Create instance of RealRoot
            RealRoot realR = new RealRoot();

            // Set extension limits
            realR.noLowerBoundExtension();
            realR.noUpperBoundExtension();

            // Set tolerance
            realR.setTolerance(tolerance);

            // call root searching method
            ran[i] = realR.falsePosition(bart, lowerBound, upperBound);
        }

        return ran;

    }

        // beta distribution mean
        public static double betaMean(double alpha, double beta){
            return betaMean(0.0D, 1.0D, alpha, beta);
        }

        // beta distribution mean
        public static double betaMean(double min, double max, double alpha, double beta){
            if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
            if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
            return min + alpha*(max - min)/(alpha + beta);
        }

        // beta distribution mode
        public static double betaMode(double alpha, double beta){
            return betaMode(0.0D, 1.0D, alpha, beta);
        }

        // beta distribution mode
        public static double betaMode(double min, double max, double alpha, double beta){
            if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
            if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");

            double mode = Double.NaN;
            if(alpha>1){
                if(beta>1){
                    mode = min + (alpha + beta)*(max - min)/(alpha + beta - 2);
                }
                else{
                    mode = max;
                }
            }
            else{
                if(alpha==1){
                    if(beta>1){
                        mode = min;
                    }
                    else{
                        if(beta==1){
                            mode = Double.NaN;
                        }
                        else{
                            mode = max;
                        }
                    }
                }
                else{
                    if(beta>=1){
                        mode = min;
                    }
                    else{
                        System.out.println("Class Stat; method betaMode; distribution is bimodal wirh modes at " + min + " and " + max);
                        System.out.println("NaN returned");
                    }
                }
            }
            return mode;
        }

        // beta distribution standard deviation
        public static double betaStandDev(double alpha, double beta){
            return betaStandDev(0.0D, 1.0D, alpha, beta);
        }

        // beta distribution standard deviation
        public static double betaStandDev(double min, double max, double alpha, double beta){
            if(alpha<=0.0D)throw new IllegalArgumentException("The shape parameter, alpha, " + alpha + "must be greater than zero");
            if(beta<=0.0D)throw new IllegalArgumentException("The shape parameter, beta, " + beta + "must be greater than zero");
            return ((max - min)/(alpha + beta))*Math.sqrt(alpha*beta/(alpha + beta + 1));
        }

        // Beta function
        public static double betaFunction(double z, double w){
                return Math.exp(logGamma(z) + logGamma(w) - logGamma(z + w));
        }

        // Beta function
        // retained for compatibility reasons
        public static double beta(double z, double w){
                return Math.exp(logGamma(z) + logGamma(w) - logGamma(z + w));
        }

        // Regularised Incomplete Beta function
        // Continued Fraction approximation (see Numerical recipies for details of method)
        public static double regularisedBetaFunction(double z, double w, double x){
            if(x<0.0D || x>1.0D)throw new IllegalArgumentException("Argument x, "+x+", must be lie between 0 and 1 (inclusive)");
            double ibeta = 0.0D;
            if(x==0.0D){
                ibeta=0.0D;
            }
            else{
                if(x==1.0D){
                    ibeta=1.0D;
                }
                else{
                    // Term before continued fraction
                    ibeta = Math.exp(Stat.logGamma(z+w) - Stat.logGamma(z) - logGamma(w) + z*Math.log(x) + w*Math.log(1.0D-x));
                    // Continued fraction
                    if(x < (z+1.0D)/(z+w+2.0D)){
                        ibeta = ibeta*Stat.contFract(z, w, x)/z;
                    }
                    else{
                        // Use symmetry relationship
                        ibeta = 1.0D - ibeta*Stat.contFract(w, z, 1.0D-x)/w;
                    }
                }
            }
            return ibeta;
        }


        // Regularised Incomplete Beta function
        // Continued Fraction approximation (see Numerical recipies for details of method)
        public static double regularizedBetaFunction(double z, double w, double x){
            return regularisedBetaFunction(z, w, x);
        }

        // Regularised Incomplete Beta function
        // Continued Fraction approximation (see Numerical recipies for details of method)
        // retained for compatibility reasons
        public static double incompleteBeta(double z, double w, double x){
            return regularisedBetaFunction(z, w, x);
        }

        // Incomplete fraction summation used in the method regularisedBetaFunction
        // modified Lentz's method
        public static double contFract(double a, double b, double x){
            int maxit = 500;
            double eps = 3.0e-7;
            double aplusb = a + b;
            double aplus1 = a + 1.0D;
            double aminus1 = a - 1.0D;
            double c = 1.0D;
            double d = 1.0D - aplusb*x/aplus1;
            if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
            d = 1.0D/d;
            double h = d;
            double aa = 0.0D;
            double del = 0.0D;
            int i=1, i2=0;
            boolean test=true;
            while(test){
                i2=2*i;
                aa = i*(b-i)*x/((aminus1+i2)*(a+i2));
                d = 1.0D + aa*d;
                if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
                c = 1.0D + aa/c;
                if(Math.abs(c)<Stat.FPMIN)c = FPMIN;
                d = 1.0D/d;
                h *= d*c;
                aa = -(a+i)*(aplusb+i)*x/((a+i2)*(aplus1+i2));
                d = 1.0D + aa*d;
                if(Math.abs(d)<Stat.FPMIN)d = FPMIN;
                c = 1.0D + aa/c;
                if(Math.abs(c)<Stat.FPMIN)c = FPMIN;
                d = 1.0D/d;
                del = d*c;
                h *= del;
                i++;
                if(Math.abs(del-1.0D) < eps)test=false;
                if(i>maxit){
                    test=false;
                    System.out.println("Maximum number of iterations ("+maxit+") exceeded in Stat.contFract in Stat.incomplete Beta");
                }
            }
            return h;

        }

        // Error Function
        public static double erf(double x){
                double erf = 0.0D;
                if(x!=0.0){
                        if(x==1.0D/0.0D){
                                erf = 1.0D;
                        }
                        else{
                                if(x>=0){
                                        erf = Stat.incompleteGamma(0.5, x*x);
                                }
                                else{
                                        erf = -Stat.incompleteGamma(0.5, x*x);
                                }
                        }
                }
                return erf;
        }

        // Complementary Error Function
        public static double erfc(double x){
                double erfc = 1.0D;
                if(x!=0.0){
                        if(x==1.0D/0.0D){
                                erfc = 0.0D;
                        }
                        else{
                                if(x>=0){
                                        erfc = 1.0D - Stat.incompleteGamma(0.5, x*x);
                                }
                                else{
                                        erfc = 1.0D + Stat.incompleteGamma(0.5, x*x);
                                }
                        }
                }
                return erfc;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mean  =  the mean, sd = standard deviation
        public static double normalCDF(double mean, double sd, double upperlimit){
                double arg = (upperlimit - mean)/(sd*Math.sqrt(2.0));
                return (1.0D + Stat.erf(arg))/2.0D;
        }
        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mean  =  the mean, sd = standard deviation
        public static double normalProb(double mean, double sd, double upperlimit){
                double arg = (upperlimit - mean)/(sd*Math.sqrt(2.0));
                return (1.0D + Stat.erf(arg))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mean  =  the mean, sd = standard deviation
        public static double gaussianCDF(double mean, double sd, double upperlimit){
                double arg = (upperlimit - mean)/(sd*Math.sqrt(2.0));
                return (1.0D + Stat.erf(arg))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mean  =  the mean, sd = standard deviation
        public static double gaussianProb(double mean, double sd, double upperlimit){
                double arg = (upperlimit - mean)/(sd*Math.sqrt(2.0));
                return (1.0D + Stat.erf(arg))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mean  =  the mean, sd = standard deviation
        public static double normalCDF(double mean, double sd, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mean)/(sd*Math.sqrt(2.0));
                double arg2 = (upperlimit - mean)/(sd*Math.sqrt(2.0));

                return (Stat.erf(arg2)-Stat.erf(arg1))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mean  =  the mean, sd = standard deviation
        public static double normalProb(double mean, double sd, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mean)/(sd*Math.sqrt(2.0));
                double arg2 = (upperlimit - mean)/(sd*Math.sqrt(2.0));

                return (Stat.erf(arg2)-Stat.erf(arg1))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mean  =  the mean, sd = standard deviation
        public static double gaussianCDF(double mean, double sd, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mean)/(sd*Math.sqrt(2.0));
                double arg2 = (upperlimit - mean)/(sd*Math.sqrt(2.0));

                return (Stat.erf(arg2)-Stat.erf(arg1))/2.0D;
        }

        // Gaussian (normal) cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mean  =  the mean, sd = standard deviation
        public static double gaussianProb(double mean, double sd, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mean)/(sd*Math.sqrt(2.0));
                double arg2 = (upperlimit - mean)/(sd*Math.sqrt(2.0));

                return (Stat.erf(arg2)-Stat.erf(arg1))/2.0D;
        }

        // Gaussian (normal) probability
        // mean  =  the mean, sd = standard deviation
        public static double normalPDF(double mean, double sd, double x){
                return Math.exp(-Fmath.square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }

        // Gaussian (normal) probability
        // mean  =  the mean, sd = standard deviation
        public static double normal(double mean, double sd, double x){
                return Math.exp(-Fmath.square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }

         // Gaussian (normal) probability
        // mean  =  the mean, sd = standard deviation
        public static double gaussianPDF(double mean, double sd, double x){
                return Math.exp(-Fmath.square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }
        // Gaussian (normal) probability
        // mean  =  the mean, sd = standard deviation
        public static double gaussian(double mean, double sd, double x){
                return Math.exp(-Fmath.square((x - mean)/sd)/2.0)/(sd*Math.sqrt(2.0D*Math.PI));
        }

        // Returns an array of Gaussian (normal) random deviates - clock seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] normalRand(double mean, double sd, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }

        // Returns an array of Gaussian (normal) random deviates - clock seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] gaussianRand(double mean, double sd, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }

        // Returns an array of Gaussian (normal) random deviates - user provided seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] normalRand(double mean, double sd, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }

        // Returns an array of Gaussian (normal) random deviates - user provided seed
        // mean  =  the mean, sd = standard deviation, length of array
        public static double[] gaussianRand(double mean, double sd, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i]=rr.nextGaussian();
                    ran[i] = ran[i]*sd+mean;
                }
                return ran;
        }

        // Logistic cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mu  =  location parameter, beta = scale parameter
        public static double logisticCDF(double mu, double beta, double upperlimit){
                return 0.5D*(1.0D + Math.tanh((upperlimit - mu)/(2.0D*beta)));
        }


        // Logistic cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        // mu  =  location parameter, beta = scale parameter
        public static double logisticProb(double mu, double beta, double upperlimit){
                return 0.5D*(1.0D + Math.tanh((upperlimit - mu)/(2.0D*beta)));
        }


        // Logistic cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mu  =  location parameter, beta = scale parameter
        public static double logisticCDF(double mu, double beta, double lowerlimit, double upperlimit){
                double arg1 = 0.5D*(1.0D + Math.tanh((lowerlimit - mu)/(2.0D*beta)));
                double arg2 = 0.5D*(1.0D + Math.tanh((upperlimit - mu)/(2.0D*beta)));
                return arg2 - arg1;
        }

        // Logistic cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        // mu  =  location parameter, beta = scale parameter
        public static double logisticProb(double mu, double beta, double lowerlimit, double upperlimit){
                double arg1 = 0.5D*(1.0D + Math.tanh((lowerlimit - mu)/(2.0D*beta)));
                double arg2 = 0.5D*(1.0D + Math.tanh((upperlimit - mu)/(2.0D*beta)));
                return arg2 - arg1;
        }

         // Logistic probability density function
        // mu  =  location parameter, beta = scale parameter
        public static double logisticPDF(double mu, double beta, double x){
                return Fmath.square(Fmath.sech((x - mu)/(2.0D*beta)))/(4.0D*beta);
        }
        // Logistic probability density function
        // mu  =  location parameter, beta = scale parameter
        public static double logistic(double mu, double beta, double x){
                return Fmath.square(Fmath.sech((x - mu)/(2.0D*beta)))/(4.0D*beta);
        }

        // Returns an array of logistic distribution random deviates - clock seed
        // mu  =  location parameter, beta = scale parameter
        public static double[] logisticRand(double mu, double beta, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i] = 2.0D*beta*Fmath.atanh(2.0D*rr.nextDouble() - 1.0D) + mu;
                }
                return ran;
        }

        // Returns an array of Logistic random deviates - user provided seed
        // mu  =  location parameter, beta = scale parameter
        public static double[] logisticRand(double mu, double beta, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i] = 2.0D*beta*Fmath.atanh(2.0D*rr.nextDouble() - 1.0D) + mu;
                }
                return ran;
        }

        // Logistic distribution mean
        public static double logisticMean(double mu){
                return mu;
        }


        // Logistic distribution standard deviation
        public static double logisticStandDev(double beta){
                return Math.sqrt(Fmath.square(Math.PI*beta)/3.0D);
        }

        // Logistic distribution mode
        public static double logisticMode(double mu){
                return mu;
        }

        // Logistic distribution median
        public static double logisticMedian(double mu){
                return mu;
        }

        // Lorentzian cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double lorentzianProb(double mu, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/(gamma/2.0D);
                return (1.0D/Math.PI)*(Math.atan(arg)+Math.PI/2.0);
        }
        // Lorentzian cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double lorentzianCDF(double mu, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (upperlimit - mu)/(gamma/2.0D);
                double arg2 = (lowerlimit - mu)/(gamma/2.0D);
                return (1.0D/Math.PI)*(Math.atan(arg1)-Math.atan(arg2));
        }

        // Lorentzian cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double lorentzianProb(double mu, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (upperlimit - mu)/(gamma/2.0D);
                double arg2 = (lowerlimit - mu)/(gamma/2.0D);
                return (1.0D/Math.PI)*(Math.atan(arg1)-Math.atan(arg2));
        }

        // Lorentzian probability
        public static double lorentzianPDF(double mu, double gamma, double x){
                double arg =gamma/2.0D;
                return (1.0D/Math.PI)*arg/(Fmath.square(mu-x)+arg*arg);
        }

        // Lorentzian probability
        public static double lorentzian(double mu, double gamma, double x){
                double arg =gamma/2.0D;
                return (1.0D/Math.PI)*arg/(Fmath.square(mu-x)+arg*arg);
        }


        // Returns an array of Lorentzian random deviates - clock seed
        // mu  =  the mean, gamma = half-height width, length of array
        public static double[] lorentzianRand(double mu, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                    ran[i]=Math.tan((rr.nextDouble()-0.5)*Math.PI);
                    ran[i] = ran[i]*gamma/2.0D+mu;
                }
                return ran;
        }

        // Returns an array of Lorentzian random deviates - user provided seed
        // mu  =  the mean, gamma = half-height width, length of array
        public static double[] lorentzianRand(double mu, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                    ran[i]=Math.tan((rr.nextDouble()-0.5)*Math.PI);
                    ran[i] = ran[i]*gamma/2.0D+mu;
                }
                return ran;
        }

        // Cumulative Poisson Probability Function
        // probability that a number of Poisson random events will occur between 0 and k (inclusive)
        // k is an integer greater than equal to 1
        // mean  = mean of the Poisson distribution
        public static double poissonCDF(int k, double mean){
                if(k<1)throw new IllegalArgumentException("k must be an integer greater than or equal to 1");
                return Stat.incompleteGammaComplementary((double) k, mean);
        }

        // Cumulative Poisson Probability Function
        // probability that a number of Poisson random events will occur between 0 and k (inclusive)
        // k is an integer greater than equal to 1
        // mean  = mean of the Poisson distribution
        public static double poissonProb(int k, double mean){
                if(k<1)throw new IllegalArgumentException("k must be an integer greater than or equal to 1");
                return Stat.incompleteGammaComplementary((double) k, mean);
        }

        // Poisson Probability Function
        // k is an integer greater than or equal to zero
        // mean  = mean of the Poisson distribution
        public static double poissonPDF(int k, double mean){
                if(k<0)throw new IllegalArgumentException("k must be an integer greater than or equal to 0");
                return Math.pow(mean, k)*Math.exp(-mean)/Stat.factorial((double)k);
        }

        // Poisson Probability Function
        // k is an integer greater than or equal to zero
        // mean  = mean of the Poisson distribution
        public static double poisson(int k, double mean){
                if(k<0)throw new IllegalArgumentException("k must be an integer greater than or equal to 0");
                return Math.pow(mean, k)*Math.exp(-mean)/Stat.factorial((double)k);
        }

        // Returns an array of Poisson random deviates - clock seed
        // mean  =  the mean,  n = length of array
        // follows the ideas of Numerical Recipes
        public static double[] poissonRand(double mean, int n){

                Random rr = new Random();
                double[] ran = poissonRandCalc(rr, mean, n);
                return ran;
        }

        // Returns an array of Poisson random deviates - user provided seed
        // mean  =  the mean,  n = length of array
        // follows the ideas of Numerical Recipes
        public static double[] poissonRand(double mean, int n, long seed){

                Random rr = new Random(seed);
                double[] ran = poissonRandCalc(rr, mean, n);
                return ran;
        }

        // Calculates and returns an array of Poisson random deviates
        private static double[] poissonRandCalc(Random rr, double mean, int n){
                double[] ran = new double[n];
                double oldm = -1.0D;
                double expt = 0.0D;
                double em = 0.0D;
                double term = 0.0D;
                double sq = 0.0D;
                double lnMean = 0.0D;
                double yDev = 0.0D;

                if(mean < 12.0D){
                    for(int i=0; i<n; i++){
                        if(mean != oldm){
                            oldm = mean;
                            expt = Math.exp(-mean);
                        }
                        em = -1.0D;
                        term = 1.0D;
                        do{
                            ++em;
                            term *= rr.nextDouble();
                        }while(term>expt);
                        ran[i] = em;
                    }
                }
                else{
                    for(int i=0; i<n; i++){
                        if(mean != oldm){
                            oldm = mean;
                            sq = Math.sqrt(2.0D*mean);
                            lnMean = Math.log(mean);
                            expt = lnMean - Stat.logGamma(mean+1.0D);
                        }
                        do{
                            do{
                                yDev = Math.tan(Math.PI*rr.nextDouble());
                                em = sq*yDev+mean;
                            }while(em<0.0D);
                            em = Math.floor(em);
                            term = 0.9D*(1.0D+yDev*yDev)*Math.exp(em*lnMean - Stat.logGamma(em+1.0D)-expt);
                        }while(rr.nextDouble()>term);
                        ran[i] = em;
                    }
                }
                return ran;
        }


        // Chi-Square Probability Function
        // probability that an observed chi-square value for a correct model should be less than chiSquare
        // nu  =  the degrees of freedom
                public static double chiSquareCDF(double chiSquare, int nu){
                return Stat.incompleteGamma((double)nu/2.0D, chiSquare/2.0D);
        }

        // retained for compatability
        public static double chiSquareProb(double chiSquare, int nu){
                return Stat.incompleteGamma((double)nu/2.0D, chiSquare/2.0D);
        }

        // Chi-Square Statistic for Poisson distribution
        public static double chiSquare(double[] observed, double[] expected, double[] variance){
            int nObs = observed.length;
            int nExp = expected.length;
            int nVar = variance.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            if(nObs!=nVar)throw new IllegalArgumentException("observed array length does not equal the variance array length");
            double chi = 0.0D;
            for(int i=0; i<nObs; i++){
                chi += Fmath.square(observed[i]-expected[i])/variance[i];
            }
            return chi;
        }

        // Chi-Square Statistic for Poisson distribution for frequency data
        // and Poisson distribution for each bin
        // double arguments
        public static double chiSquareFreq(double[] observedFreq, double[] expectedFreq){
            int nObs = observedFreq.length;
            int nExp = expectedFreq.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            double chi = 0.0D;
            for(int i=0; i<nObs; i++){
                chi += Fmath.square(observedFreq[i]-expectedFreq[i])/expectedFreq[i];
            }
            return chi;
        }

        // Chi-Square Statistic for Poisson distribution for frequency data
        // and Poisson distribution for each bin
        // int arguments
        public static double chiSquareFreq(int[] observedFreq, int[] expectedFreq){
            int nObs = observedFreq.length;
            int nExp = expectedFreq.length;
            if(nObs!=nExp)throw new IllegalArgumentException("observed array length does not equal the expected array length");
            double[] observ = new double[nObs];
            double[] expect = new double[nObs];
            for(int i=0; i<nObs; i++){
                observ[i] = (int)observedFreq[i];
                expect[i] = (int)expectedFreq[i];
            }

            return chiSquareFreq(observ, expect);
        }

        // Returns the binomial cumulative probabilty
        public static double binomialCDF(double p, int n, int k){
                if(p<0.0D || p>1.0D)throw new IllegalArgumentException("\np must lie between 0 and 1");
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Stat.regularisedBetaFunction(k, n-k+1, p);
        }
        // Returns the binomial cumulative probabilty
        public static double binomialProb(double p, int n, int k){
                if(p<0.0D || p>1.0D)throw new IllegalArgumentException("\np must lie between 0 and 1");
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Stat.regularisedBetaFunction(k, n-k+1, p);
        }

         // Returns a binomial mass probabilty function
        public static double binomialPDF(double p, int n, int k){
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n-k)))*Math.pow(p, k)*Math.pow(1.0D - p, n - k);
        }

        // Returns a binomial mass probabilty function
        public static double binomial(double p, int n, int k){
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n-k)))*Math.pow(p, k)*Math.pow(1.0D - p, n - k);
        }

        // Returns a binomial Coefficient as a double
        public static double binomialCoeff(int n, int k){
                if(k<0 || n<0)throw new IllegalArgumentException("\nn and k must be greater than or equal to zero");
                if(k>n)throw new IllegalArgumentException("\nk is greater than n");
                return Math.floor(0.5D + Math.exp(Stat.logFactorial(n) - Stat.logFactorial(k) - Stat.logFactorial(n-k)));
        }

        // Returns an array of n Binomial pseudorandom deviates from a binomial - clock seed
        //  distribution of nTrial trials each of probablity, prob,
        //  after 	bndlev 	Numerical Recipes in C - W.H. Press et al. (Cambridge)
        //		            2nd edition 1992 p295.
        public double[] binomialRand(double prob, int nTrials, int n){

            if(nTrials<n)throw new IllegalArgumentException("Number of deviates requested, " + n + ", must be less than the number of trials, " + nTrials);
            if(prob<0.0D || prob>1.0D)throw new IllegalArgumentException("The probablity provided, " + prob + ", must lie between 0 and 1)");

            double[] ran = new double[n];                   // array of deviates to be returned
            Random rr = new Random();                       // instance of Random

	        double binomialDeviate = 0.0D;                  // the binomial deviate to be returned
	        double deviateMean = 0.0D;                      // mean of deviate to be produced
	        double testDeviate = 0.0D;                      // test deviate
	        double workingProb = 0.0;                       // working value of the probability
	        double logProb = 0.0;                           // working value of the probability
	        double probOld = -1.0D;                         // previous value of the working probability
	        double probC = -1.0D;                           // complementary value of the working probability
	        double logProbC = -1.0D;                        // log of the complementary value of the working probability
	        int nOld= -1;                                   // previous value of trials counter
	        double enTrials = 0.0D;                         // (double) trials counter
	        double oldGamma = 0.0D;                         // a previous log Gamma function value
	        double tanW = 0.0D;                             // a working tangent
	        double hold0 = 0.0D;                            // a working holding variable
	        int jj;                                         // counter

            double probOriginalValue = prob;
            for(int i=0; i<n; i++){
                prob = probOriginalValue;
	            workingProb=(prob <= 0.5D ? prob : 1.0-prob);    // distribution invariant on swapping prob for 1 - prob
	            deviateMean = nTrials*workingProb;

	            if(nTrials < 25) {
	                // if number of trials greater than 25 use direct method
		            binomialDeviate=0.0D;
		            for(jj=1;jj<=nTrials;jj++)if (rr.nextDouble() < workingProb) ++binomialDeviate;
	            }
	            else if(deviateMean < 1.0D) {
	                // if fewer than 1 out of 25 events - Poisson approximation is accurate
		            double expOfMean=Math.exp(-deviateMean);
		            testDeviate=1.0D;
		            for (jj=0;jj<=nTrials;jj++) {
			            testDeviate *= rr.nextDouble();
			            if (testDeviate < expOfMean) break;
		            }
		            binomialDeviate=(jj <= nTrials ? jj : nTrials);

	            }
	            else{
	                // use rejection method
		            if(nTrials != nOld) {
		                // if nTrials has changed compute useful quantities
			            enTrials = (double)nTrials;
			            oldGamma = Stat.logGamma(enTrials + 1.0D);
			            nOld = nTrials;
		            }
		            if(workingProb != probOld) {
		                // if workingProb has changed compute useful quantities
                        probC = 1.0 - workingProb;
			            logProb = Math.log(workingProb);
			            logProbC = Math.log(probC);
			            probOld = workingProb;
		            }

		            double sq = Math.sqrt(2.0*deviateMean*probC);
		            do{
			            do{
				            double angle = Math.PI*rr.nextDouble();
				            tanW = Math.tan(angle);
				            hold0 = sq*tanW + deviateMean;
			            }while(hold0 < 0.0D || hold0 >= (enTrials + 1.0D));   //rejection test
			            hold0 = Math.floor(hold0);                              // integer value distribution
			            testDeviate = 1.2D*sq*(1.0D + tanW*tanW)*Math.exp(oldGamma - Stat.logGamma(hold0 + 1.0D) - Stat.logGamma(enTrials - hold0 + 1.0D) + hold0*logProb + (enTrials - hold0)*logProbC);
		            }while(rr.nextDouble() > testDeviate);                         // rejection test
		            binomialDeviate=hold0;
	            }

	            if(workingProb != prob) binomialDeviate = nTrials - binomialDeviate;       // symmetry transformation

	            ran[i] = binomialDeviate;
	        }

	        return ran;
        }

        // Returns an array of n Binomial pseudorandom deviates from a binomial - user supplied seed
        //  distribution of nTrial trials each of probablity, prob,
        //  after 	bndlev 	Numerical Recipes in C - W.H. Press et al. (Cambridge)
        //		            2nd edition 1992 p295.
        public double[] binomialRand(double prob, int nTrials, int n, long seed){

            if(nTrials<n)throw new IllegalArgumentException("Number of deviates requested, " + n + ", must be less than the number of trials, " + nTrials);
            if(prob<0.0D || prob>1.0D)throw new IllegalArgumentException("The probablity provided, " + prob + ", must lie between 0 and 1)");

            double[] ran = new double[n];                   // array of deviates to be returned
            Random rr = new Random(seed);                       // instance of Random

	        double binomialDeviate = 0.0D;                  // the binomial deviate to be returned
	        double deviateMean = 0.0D;                      // mean of deviate to be produced
	        double testDeviate = 0.0D;                      // test deviate
	        double workingProb = 0.0;                       // working value of the probability
	        double logProb = 0.0;                           // working value of the probability
	        double probOld = -1.0D;                         // previous value of the working probability
	        double probC = -1.0D;                           // complementary value of the working probability
	        double logProbC = -1.0D;                        // log of the complementary value of the working probability
	        int nOld= -1;                                   // previous value of trials counter
	        double enTrials = 0.0D;                         // (double) trials counter
	        double oldGamma = 0.0D;                         // a previous log Gamma function value
	        double tanW = 0.0D;                             // a working tangent
	        double hold0 = 0.0D;                            // a working holding variable
	        int jj;                                         // counter

            double probOriginalValue = prob;
            for(int i=0; i<n; i++){
                prob = probOriginalValue;
	            workingProb=(prob <= 0.5D ? prob : 1.0-prob);    // distribution invariant on swapping prob for 1 - prob
	            deviateMean = nTrials*workingProb;

	            if(nTrials < 25) {
	                // if number of trials greater than 25 use direct method
		            binomialDeviate=0.0D;
		            for(jj=1;jj<=nTrials;jj++)if (rr.nextDouble() < workingProb) ++binomialDeviate;
	            }
	            else if(deviateMean < 1.0D) {
	                // if fewer than 1 out of 25 events - Poisson approximation is accurate
		            double expOfMean=Math.exp(-deviateMean);
		            testDeviate=1.0D;
		            for (jj=0;jj<=nTrials;jj++) {
			            testDeviate *= rr.nextDouble();
			            if (testDeviate < expOfMean) break;
		            }
		            binomialDeviate=(jj <= nTrials ? jj : nTrials);

	            }
	            else{
	                // use rejection method
		            if(nTrials != nOld) {
		                // if nTrials has changed compute useful quantities
			            enTrials = (double)nTrials;
			            oldGamma = Stat.logGamma(enTrials + 1.0D);
			            nOld = nTrials;
		            }
		            if(workingProb != probOld) {
		                // if workingProb has changed compute useful quantities
                        probC = 1.0 - workingProb;
			            logProb = Math.log(workingProb);
			            logProbC = Math.log(probC);
			            probOld = workingProb;
		            }

		            double sq = Math.sqrt(2.0*deviateMean*probC);
		            do{
			            do{
				            double angle = Math.PI*rr.nextDouble();
				            tanW = Math.tan(angle);
				            hold0 = sq*tanW + deviateMean;
			            }while(hold0 < 0.0D || hold0 >= (enTrials + 1.0D));   //rejection test
			            hold0 = Math.floor(hold0);                              // integer value distribution
			            testDeviate = 1.2D*sq*(1.0D + tanW*tanW)*Math.exp(oldGamma - Stat.logGamma(hold0 + 1.0D) - Stat.logGamma(enTrials - hold0 + 1.0D) + hold0*logProb + (enTrials - hold0)*logProbC);
		            }while(rr.nextDouble() > testDeviate);                         // rejection test
		            binomialDeviate=hold0;
	            }

	            if(workingProb != prob) binomialDeviate = nTrials - binomialDeviate;       // symmetry transformation

	            ran[i] = binomialDeviate;
	        }

	        return ran;
        }

        // Returns the F-distribution probabilty for degrees of freedom df1, df2
        // F ratio provided
        public static double fTestProb(double fValue, int df1, int df2){
            double ddf1 = (double)df1;
            double ddf2 = (double)df2;
            double x = ddf2/(ddf2+ddf1*fValue);
            return Stat.regularisedBetaFunction(df2/2.0D, df1/2.0D, x);
        }

        // Returns the F-distribution probabilty for degrees of freedom df1, df2
        // numerator and denominator variances provided
        public static double fTestProb(double var1, int df1, double var2, int df2){
            double fValue = var1/var2;
            double ddf1 = (double)df1;
            double ddf2 = (double)df2;
            double x = ddf2/(ddf2+ddf1*fValue);
            return Stat.regularisedBetaFunction(df2/2.0D, df1/2.0D, x);
        }

        // Returns the F-test value corresponding to a F-distribution probabilty, fProb,
        //   for degrees of freedom df1, df2
        public static double fTestValueGivenFprob(double fProb, int df1, int df2){

            // Create an array F-test value array
            int fTestsNum = 100;    // length of array
            double[] fTestValues = new double[fTestsNum];
            fTestValues[0]=0.0001D;             // lowest array value
            fTestValues[fTestsNum-1]=10000.0D;  // highest array value
            // calculate array increment - log scale
            double diff = (Fmath.log10(fTestValues[fTestsNum-1])-Fmath.log10(fTestValues[0]))/(fTestsNum-1);
            // Fill array
            for(int i=1; i<fTestsNum-1; i++){
                fTestValues[i] = Math.pow(10.0D,(Fmath.log10(fTestValues[i-1])+diff));
            }

            // calculate F test probability array corresponding to F-test value array
            double[] fTestProb = new double[fTestsNum];
            for(int i=0; i<fTestsNum; i++){
                fTestProb[i] = Stat.fTestProb(fTestValues[i], df1, df2);
            }

            // calculate F-test value for provided probability
            // using bisection procedure
            double fTest0 = 0.0D;
            double fTest1 = 0.0D;
            double fTest2 = 0.0D;

            // find bracket for the F-test probabilities and calculate F-Test value from above arrays
            boolean test0 = true;
            boolean test1 = true;
            int i=0;
            int endTest=0;
            while(test0){
                if(fProb==fTestProb[i]){
                    fTest0=fTestValues[i];
                    test0=false;
                    test1=false;
                }
                else{
                    if(fProb>fTestProb[i]){
                        test0=false;
                        if(i>0){
                            fTest1=fTestValues[i-1];
                            fTest2=fTestValues[i];
                            endTest=-1;
                        }
                        else{
                            fTest1=fTestValues[i]/10.0D;
                            fTest2=fTestValues[i];
                        }
                    }
                    else{
                        i++;
                        if(i>fTestsNum-1){
                            test0=false;
                            fTest1=fTestValues[i-1];
                            fTest2=10.0D*fTestValues[i-1];
                            endTest=1;
                        }
                    }
                }
            }

            // call bisection method
            if(test1)fTest0=fTestBisect(fProb, fTest1, fTest2, df1, df2, endTest);

            return fTest0;
        }

        // Bisection procedure for calculating and F-test value corresponding
        //   to a given F-test probability
        private static double fTestBisect(double fProb, double fTestLow, double fTestHigh, int df1, int df2, int endTest){

            double funcLow = fProb - Stat.fTestProb(fTestLow, df1, df2);
            double funcHigh = fProb - Stat.fTestProb(fTestHigh, df1, df2);
            double fTestMid = 0.0D;
            double funcMid = 0.0;
            int nExtensions = 0;
            int nIter = 1000;           // iterations allowed
            double check = fProb*1e-6;  // tolerance for bisection
            boolean test0 = true;       // test for extending bracket
            boolean test1 = true;       // test for bisection procedure
            while(test0){
                if(funcLow*funcHigh>0.0D){
                    if(endTest<0){
                        nExtensions++;
                        if(nExtensions>100){
                            System.out.println("Class: Stats\nMethod: fTestBisect\nProbability higher than range covered\nF-test value is less than "+fTestLow);
                            System.out.println("This value was returned");
                            fTestMid=fTestLow;
                            test0=false;
                            test1=false;
                        }
                        fTestLow /= 10.0D;
                        funcLow = fProb - Stat.fTestProb(fTestLow, df1, df2);
                    }
                    else{
                        nExtensions++;
                        if(nExtensions>100){
                            System.out.println("Class: Stats\nMethod: fTestBisect\nProbability lower than range covered\nF-test value is greater than "+fTestHigh);
                            System.out.println("This value was returned");
                            fTestMid=fTestHigh;
                            test0=false;
                            test1=false;
                        }
                        fTestHigh *= 10.0D;
                        funcHigh = fProb - Stat.fTestProb(fTestHigh, df1, df2);
                    }
                }
                else{
                    test0=false;
                }

                int i=0;
                while(test1){
                    fTestMid = (fTestLow+fTestHigh)/2.0D;
                    funcMid = fProb - Stat.fTestProb(fTestMid, df1, df2);
                    if(Math.abs(funcMid)<check){
                        test1=false;
                    }
                    else{
                        i++;
                        if(i>nIter){
                            System.out.println("Class: Stats\nMethod: fTestBisect\nmaximum number of iterations exceeded\ncurrent value of F-test value returned");
                            test1=false;
                        }
                        if(funcMid*funcHigh>0){
                            funcHigh=funcMid;
                            fTestHigh=fTestMid;
                        }
                        else{
                            funcLow=funcMid;
                            fTestLow=fTestMid;
                        }
                    }
                }
            }
            return fTestMid;
        }

        // Returns the Student's t probability density function
        public static double studentT(double tValue, int df){
            double ddf = (double)df;
            double dfterm = (ddf + 1.0D)/2.0D;
            return ((Stat.gamma(dfterm)/Stat.gamma(ddf/2))/Math.sqrt(ddf*Math.PI))*Math.pow(1.0D + tValue*tValue/ddf, -dfterm);
        }

        // Returns the Student's t probability density function
        public static double studentTpdf(double tValue, int df){
            double ddf = (double)df;
            double dfterm = (ddf + 1.0D)/2.0D;
            return ((Stat.gamma(dfterm)/Stat.gamma(ddf/2))/Math.sqrt(ddf*Math.PI))*Math.pow(1.0D + tValue*tValue/ddf, -dfterm);
        }

        // Returns the Student's t probability density function
        public static double studentTPDF(double tValue, int df){
            double ddf = (double)df;
            double dfterm = (ddf + 1.0D)/2.0D;
            return ((Stat.gamma(dfterm)/Stat.gamma(ddf/2))/Math.sqrt(ddf*Math.PI))*Math.pow(1.0D + tValue*tValue/ddf, -dfterm);
        }

         // Returns the Student's t cumulative distribution function probability
        public static double studentTProb(double tValue, int df){
            double ddf = (double)df;
            double x = ddf/(ddf+tValue*tValue);
            return 0.5D*(1.0D + (Stat.regularisedBetaFunction(ddf/2.0D, 0.5D, 1) - Stat.regularisedBetaFunction(ddf/2.0D, 0.5D, x))*Fmath.sign(tValue));
        }

        // Returns the Student's t cumulative distribution function probability
        public static double studentTcdf(double tValue, int df){
            double ddf = (double)df;
            double x = ddf/(ddf+tValue*tValue);
            return 0.5D*(1.0D + (Stat.regularisedBetaFunction(ddf/2.0D, 0.5D, 1) - Stat.regularisedBetaFunction(ddf/2.0D, 0.5D, x))*Fmath.sign(tValue));
        }

        // Returns the Student's t cumulative distribution function probability
        public static double studentTCDF(double tValue, int df){
            double ddf = (double)df;
            double x = ddf/(ddf+tValue*tValue);
            return 0.5D*(1.0D + (Stat.regularisedBetaFunction(ddf/2.0D, 0.5D, 1) - Stat.regularisedBetaFunction(ddf/2.0D, 0.5D, x))*Fmath.sign(tValue));
        }

        // Returns the Student's t mean,  df = degrees of freedom
        public static double studentTmean(int df){
            double mean = Double.NaN;    // mean undefined for df = 1
            if(df>1)mean = 0.0D;
            return mean;
        }

        // Returns the Student's t median
        public static double studentTmedian(){
            return 0.0;
        }

        // Returns the Student's t mode
        public static double studentTmode(){
            return 0.0;
        }

        // Returns the Student's t standard deviation,  df = degrees of freedom
        public static double studentTstandDev(int df){
            double standDev = Double.POSITIVE_INFINITY;
            if(df>2)standDev = Math.sqrt(df/(1 - df));
            return standDev;
        }

        // Returns the A(t|n) distribution probabilty
        public static double probAtn(double tValue, int df){
            double ddf = (double)df;
            double x = ddf/(ddf+tValue*tValue);
            return 1.0D - Stat.regularisedBetaFunction(ddf/2.0D, 0.5D, x);
        }

    // Returns an array of Student's t random deviates - clock seed
    // nu  =  the degrees of freedom
    public double[] studentTRand(int nu, int n){
        Random rr = new Random();
        return studentTrandGen(rr, nu, n);
    }


    // Returns an array of Student's t random deviates - clock seed
    // nu  =  the degrees of freedom
    public static double[] studentTrand(int nu, int n){
        Random rr = new Random();
        return studentTrandGen(rr, nu, n);
    }

    // Returns an array of a Student's t random deviates - user supplied seed
    // nu  =  the degrees of freedom
    public static double[] studentTrand(int nu, int n, long seed){
        Random rr = new Random(seed);
        return studentTrandGen(rr, nu, n);
    }

        // Returns an array of a Student's t random deviates - user supplied seed
    // nu  =  the degrees of freedom
    public static double[] studentTRand(int nu, int n, long seed){
        Random rr = new Random(seed);
        return studentTrandGen(rr, nu, n);
    }

    // private method for generating an array of student's t random deviates
    private static double[] studentTrandGen(Random rr, int nu, int n){
        double[] ran = new double[n];

        // Create instance of the class holding the Gaussian cfd function
        StudentTfunct strt = new StudentTfunct();

        // set function variables
        strt.nu = nu;

        // Set initial range for search
        double centre = 0.0D;
        double range = 100.0D;
        if(nu>2)range = nu/(nu-2);

        // required tolerance
        double tolerance = 1e-10;

        for(int i=0; i<n; i++){

            // lower bound
            double lowerBound = centre - 5.0D*range;
            // upper bound
            double upperBound = centre + 5.0D*range;

            //  set functioncfd  variables
            strt.cfd = rr.nextDouble();

            // Create instance of RealRoot
            RealRoot realR = new RealRoot();

            // Set tolerance
            realR.setTolerance(tolerance);

            // call root searching method
            ran[i] = realR.falsePosition(strt, lowerBound, upperBound);
        }

        return ran;

    }


        // Distribute data into bins to obtain histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero, double binUpper){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                System.out.println((m-n)+" data points, above histogram upper limit, excluded in Stat.histogramBins");
                return histogramBins(newData, binWidth, binZero);
            }
            else{
                 return histogramBins(data, binWidth, binZero);

            }
        }

        // Distribute data into bins to obtain histogram
        // zero bin position provided
        public static double[][] histogramBins(double[] data, double binWidth, double binZero){
            double dmax = Fmath.maximum(data);
            int nBins = (int) Math.ceil((dmax - binZero)/binWidth);
            if(binZero+nBins*binWidth>dmax)nBins++;
            int nPoints = data.length;
            int[] dataCheck = new int[nPoints];
            for(int i=0; i<nPoints; i++)dataCheck[i]=0;
            double[]binWall = new double[nBins+1];
            binWall[0]=binZero;
            for(int i=1; i<=nBins; i++){
                binWall[i] = binWall[i-1] + binWidth;
            }
            double[][] binFreq = new double[2][nBins];
            for(int i=0; i<nBins; i++){
                binFreq[0][i]= (binWall[i]+binWall[i+1])/2.0D;
                binFreq[1][i]= 0.0D;
            }
            boolean test = true;

            for(int i=0; i<nPoints; i++){
                test=true;
                int j=0;
                while(test){
                    if(j==nBins-1){
                        if(data[i]>=binWall[j] && data[i]<=binWall[j+1]*(1.0D + Stat.histTol)){
                            binFreq[1][j]+= 1.0D;
                            dataCheck[i]=1;
                            test=false;
                        }
                    }
                    else{
                        if(data[i]>=binWall[j] && data[i]<binWall[j+1]){
                            binFreq[1][j]+= 1.0D;
                            dataCheck[i]=1;
                            test=false;
                        }
                    }
                    if(test){
                        if(j==nBins-1){
                            test=false;
                        }
                        else{
                            j++;
                        }
                    }
                }
            }
            int nMissed=0;
            for(int i=0; i<nPoints; i++)if(dataCheck[i]==0){
                nMissed++;
                System.out.println("p " + i + " " + data[i] + " " + binWall[0] + " " + binWall[nBins]);
            }
            if(nMissed>0)System.out.println(nMissed+" data points, outside histogram limits, excluded in Stat.histogramBins");
            return binFreq;
        }

        // Distribute data into bins to obtain histogram
        // zero bin position calculated
        public static double[][] histogramBins(double[] data, double binWidth){

            double dmin = Fmath.minimum(data);
            double dmax = Fmath.maximum(data);
            double span = dmax - dmin;
            double binZero = dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double histoSpan = ((double)nBins)*binWidth;
            double rem = histoSpan - span;
            if(rem>=0){
                binZero -= rem/2.0D;
            }
            else{
                if(Math.abs(rem)/span>histTol){
                    // readjust binWidth
                    boolean testBw = true;
                    double incr = histTol/nBins;
                    int iTest = 0;
                    while(testBw){
                       binWidth += incr;
                       histoSpan = ((double)nBins)*binWidth;
                        rem = histoSpan - span;
                        if(rem<0){
                            iTest++;
                            if(iTest>1000){
                                testBw = false;
                                System.out.println("histogram method could not encompass all data within histogram\nContact Michael thomas Flanagan");
                            }
                        }
                        else{
                            testBw = false;
                        }
                    }
                }
            }

            return Stat.histogramBins(data, binWidth, binZero);
        }

        // Distribute data into bins to obtain histogram and plot histogram
        // zero bin position and upper limit provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, double binUpper){
            String xLegend = null;
            return histogramBinsPlot(data, binWidth, binZero, binUpper, xLegend);
        }

        // Distribute data into bins to obtain histogram and plot histogram
        // zero bin position, upper limit and x-axis legend provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, double binUpper, String xLegend){
            int n = 0;              // new array length
            int m = data.length;    // old array length;
            for(int i=0; i<m; i++)if(data[i]<=binUpper)n++;
            if(n!=m){
                double[] newData = new double[n];
                int j = 0;
                for(int i=0; i<m; i++){
                    if(data[i]<=binUpper){
                        newData[j] = data[i];
                        j++;
                    }
                }
                System.out.println((m-n)+" data points, above histogram upper limit, excluded in Stat.histogramBins");
                return histogramBinsPlot(newData, binWidth, binZero, xLegend);
            }
            else{
                 return histogramBinsPlot(data, binWidth, binZero, xLegend);

            }
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero){
            String xLegend = null;
            return histogramBinsPlot(data, binWidth, binZero, xLegend);
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position and x-axis legend provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, double binZero, String xLegend){
            double[][] results = histogramBins(data, binWidth, binZero);
            int nBins = results[0].length;
            int nPoints = nBins*3+1;
            double[][] cdata = PlotGraph.data(1, nPoints);
            cdata[0][0]=binZero;
            cdata[1][0]=0.0D;
            int k=1;
            for(int i=0; i<nBins; i++){
                cdata[0][k]=cdata[0][k-1];
                cdata[1][k]=results[1][i];
                k++;
                cdata[0][k]=cdata[0][k-1]+binWidth;
                cdata[1][k]=results[1][i];
                k++;
                cdata[0][k]=cdata[0][k-1];
                cdata[1][k]=0.0D;
                k++;
            }

            PlotGraph pg = new PlotGraph(cdata);
            pg.setGraphTitle("Histogram:  Bin Width = "+binWidth);
            pg.setLine(3);
            pg.setPoint(0);
            pg.setYaxisLegend("Frequency");
            if(xLegend!=null)pg.setXaxisLegend(xLegend);
            pg.plot();

            return results;
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position calculated
        public static double[][] histogramBinsPlot(double[] data, double binWidth){
            String xLegend = null;
            return Stat.histogramBinsPlot(data, binWidth, xLegend);
        }

        // Distribute data into bins to obtain histogram and plot the histogram
        // zero bin position calculated, x-axis legend provided
        public static double[][] histogramBinsPlot(double[] data, double binWidth, String xLegend){
            double dmin = Fmath.minimum(data);
            double dmax = Fmath.maximum(data);
            double span = dmax - dmin;
            int nBins = (int) Math.ceil(span/binWidth);
            double rem = ((double)nBins)*binWidth-span;
            double binZero =dmin-rem/2.0D;
            return Stat.histogramBinsPlot(data, binWidth, binZero, xLegend);
        }

// factorial of n
        // argument and return are integer, therefore limited to 0<=n<=12
        // see below for long and double arguments
        public static int factorial(int n){
            if(n<0)throw new IllegalArgumentException("n must be a positive integer");
            if(n>12)throw new IllegalArgumentException("n must less than 13 to avoid integer overflow\nTry long or double argument");
            int f = 1;
            for(int i=1; i<=n; i++)f*=i;
            return f;
        }

        // factorial of n
        // argument and return are long, therefore limited to 0<=n<=20
        // see below for double argument
        public static long factorial(long n){
            if(n<0)throw new IllegalArgumentException("n must be a positive integer");
            if(n>20)throw new IllegalArgumentException("n must less than 21 to avoid long integer overflow\nTry double argument");
            long f = 1;
            long iCount = 0L;
            while(iCount<=n){
                f*=iCount;
                iCount += 1L;
            }
            return f;
        }

        // factorial of n
        // Argument is of type double but must be, numerically, an integer
        // factorial returned as double but is, numerically, should be an integer
        // numerical rounding may makes this an approximation after n = 21
        public static double factorial(double n){
            if(n<0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 1.0D;
            double iCount = 0.0D;
            while(iCount<=n){
                f*=iCount;
                iCount += 1.0D;
            }
            return f;
        }

        // log to base e of the factorial of n
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(int n){
            if(n<0 || (n-(int)n)!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            for(int i=2; i<=n; i++)f+=Math.log(i);
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(long n){
            if(n<0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            long iCount = 2L;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1L;
            }
            return f;
        }

        // log to base e of the factorial of n
        // Argument is of type double but must be, numerically, an integer
        // log[e](factorial) returned as double
        // numerical rounding may makes this an approximation
        public static double logFactorial(double n){
            if(n<0 || (n-Math.floor(n))!=0)throw new IllegalArgumentException("\nn must be a positive integer\nIs a Gamma funtion [Fmath.gamma(x)] more appropriate?");
            double f = 0.0D;
            double iCount = 2.0D;
            while(iCount<=n){
                f+=Math.log(iCount);
                iCount += 1.0D;
            }
            return f;
        }

        // Calculate correlation coefficient
        // x y data as double
        public static double corrCoeff(double[] xx, double[]yy){

            double temp0 = 0.0D, temp1 = 0.0D;  // working variables
            int nData = xx.length;
            if(yy.length!=nData)throw new IllegalArgumentException("array lengths must be equal");
            int df = nData-1;
            // means
            double mx = 0.0D;
            double my = 0.0D;
            for(int i=0; i<nData; i++){
                mx += xx[i];
                my += yy[i];
            }
            mx /= nData;
            my /= nData;

            // calculate sample variances
            double s2xx = 0.0D;
            double s2yy = 0.0D;
            double s2xy = 0.0D;
            for(int i=0; i<nData; i++){
                s2xx += Fmath.square(xx[i]-mx);
                s2yy += Fmath.square(yy[i]-my);
                s2xy += (xx[i]-mx)*(yy[i]-my);
            }
            s2xx /= df;
            s2yy /= df;
            s2xy /= df;

            // calculate corelation coefficient
            double sampleR = s2xy/Math.sqrt(s2xx*s2yy);

            return sampleR;
        }

        // Calculate correlation coefficient
        // x y data as float
        public static float corrCoeff(float[] x, float[] y){
            int nData = x.length;
            if(y.length!=nData)throw new IllegalArgumentException("array lengths must be equal");
            int n = x.length;
            double[] xx = new double[n];
            double[] yy = new double[n];
            for(int i=0; i<n; i++){
                xx[i] = (double)x[i];
                yy[i] = (double)y[i];
            }
            return (float)Stat.corrCoeff(xx, yy);
        }

        // Calculate correlation coefficient
        // x y data as int
        public static double corrCoeff(int[] x, int[]y){
            int n = x.length;
            if(y.length!=n)throw new IllegalArgumentException("array lengths must be equal");

            double[] xx = new double[n];
            double[] yy = new double[n];
            for(int i=0; i<n; i++){
                xx[i] = (double)x[i];
                yy[i] = (double)y[i];
            }
            return Stat.corrCoeff(xx, yy);
        }

        // Calculate weighted correlation coefficient
        // x y data and weights w as double
        public static double corrCoeff(double[] x, double[]y, double[] w){
            int n = x.length;
            if(y.length!=n)throw new IllegalArgumentException("x and y array lengths must be equal");
            if(w.length!=n)throw new IllegalArgumentException("x and weight array lengths must be equal");

            double sxy = Stat.covariance(x, y, w);
            double sx = Stat.variance(x, w);
            double sy = Stat.variance(y, w);
            return sxy/Math.sqrt(sx*sy);
        }

        // Calculate correlation coefficient
        // Binary data x and y
        // Input is the frequency matrix, F, elements, f(i,j)
        // f(0,0) - element00 - frequency of x and y both = 1
        // f(0,1) - element01 - frequency of x = 0 and y = 1
        // f(1,0) - element10 - frequency of x = 1 and y = 0
        // f(1,1) - element11 - frequency of x and y both = 0
        public static double corrCoeff(int element00, int element01, int element10, int element11){
            return ((double)(element00*element11 - element01*element10))/Math.sqrt((double)((element00+element01)*(element10+element11)*(element00+element10)*(element01+element11)));
        }

        // Calculate correlation coefficient
        // Binary data x and y
        // Input is the frequency matrix, F
        // F(0,0) - frequency of x and y both = 1
        // F(0,1) - frequency of x = 0 and y = 1
        // F(1,0) - frequency of x = 1 and y = 0
        // F(1,1) - frequency of x and y both = 0
        public static double corrCoeff(int[][] freqMatrix){
            double element00 = (double)freqMatrix[0][0];
            double element01 = (double)freqMatrix[0][1];
            double element10 = (double)freqMatrix[1][0];
            double element11 = (double)freqMatrix[1][1];
            return ((element00*element11 - element01*element10))/Math.sqrt(((element00+element01)*(element10+element11)*(element00+element10)*(element01+element11)));
        }

        // Linear correlation coefficient cumulative probablity
        // old name calls renamed method
        public static double linearCorrCoeffProb(double rCoeff, int nu){
            return corrCoeffProb(rCoeff, nu);
        }

        // Linear correlation coefficient cumulative probablity
        public static double corrCoeffProb(double rCoeff, int nu){
            if(Math.abs(rCoeff)>1.0D)throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

            // Create instances of the classes holding the function evaluation methods
            CorrCoeff cc = new CorrCoeff();

            // Assign values to constant in the function
            cc.a = ((double)nu - 2.0D)/2.0D;


            double integral = Integration.gaussQuad(cc, Math.abs(rCoeff), 1.0D, 128);

            double preterm = Math.exp(Stat.logGamma((nu+1.0D)/2.0)-Stat.logGamma(nu/2.0D))/Math.sqrt(Math.PI);

            return preterm*integral;
        }

        // Linear correlation coefficient single probablity
        // old name calls renamed method
        public static double linearCorrCoeff(double rCoeff, int nu){
            return Stat.corrCoeffPDF(rCoeff, nu);
        }

        // Linear correlation coefficient single probablity
        public static double corrCoeffPDF(double rCoeff, int nu){
            if(Math.abs(rCoeff)>1.0D)throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

            double a = ((double)nu - 2.0D)/2.0D;
            double y = Math.pow((1.0D - Fmath.square(rCoeff)),a);

            double preterm = Math.exp(Stat.logGamma((nu+1.0D)/2.0)-Stat.logGamma(nu/2.0D))/Math.sqrt(Math.PI);

            return preterm*y;
        }

        // Linear correlation coefficient single probablity
        public static double corrCoeffPdf(double rCoeff, int nu){
            if(Math.abs(rCoeff)>1.0D)throw new IllegalArgumentException("|Correlation coefficient| > 1 :  " + rCoeff);

            double a = ((double)nu - 2.0D)/2.0D;
            double y = Math.pow((1.0D - Fmath.square(rCoeff)),a);

            double preterm = Math.exp(Stat.logGamma((nu+1.0D)/2.0)-Stat.logGamma(nu/2.0D))/Math.sqrt(Math.PI);

            return preterm*y;
        }

         // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMinProbCDF(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return Math.exp(-Math.exp(arg));
        }

        // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMinProb(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return Math.exp(-Math.exp(arg));
        }

        // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMaxCDF(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return 1.0D-Math.exp(-Math.exp(arg));
        }

        // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value less than the upperlimit
        public static double gumbelMaxProb(double mu, double sigma, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg = -(upperlimit - mu)/sigma;
                return 1.0D-Math.exp(-Math.exp(arg));
        }


         // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMinCDF(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = -(lowerlimit - mu)/sigma;
                double arg2 = -(upperlimit - mu)/sigma;
                double term1 = Math.exp(-Math.exp(arg1));
                double term2 = Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

        // Minimum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMinProb(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = -(lowerlimit - mu)/sigma;
                double arg2 = -(upperlimit - mu)/sigma;
                double term1 = Math.exp(-Math.exp(arg1));
                double term2 = Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

         // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMaxCDF(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = -Math.exp(-Math.exp(arg1));
                double term2 = -Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

        // Maximum Gumbel cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double gumbelMaxProb(double mu, double sigma, double lowerlimit, double upperlimit){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = -Math.exp(-Math.exp(arg1));
                double term2 = -Math.exp(-Math.exp(arg2));
                return term2-term1;
        }

        // Minimum Gumbel probability
        public static double gumbelMinPDF(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

                // Minimum Gumbel probability
        public static double gumbelMin(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

         // Maximum Gumbel probability
        public static double gumbelMaxPDF(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =-(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

        // Maximum Gumbel probability
        public static double gumbelMax(double mu,double sigma, double x){
                if(sigma<0.0D)throw new IllegalArgumentException("sigma must be positive");
                double arg =-(x-mu)/sigma;
                return (1.0D/sigma)*Math.exp(arg)*Math.exp(-Math.exp(arg));
        }

        // Returns an array of minimal Gumbel (Type I EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMinRand(double mu, double sigma,  int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
                }
                return ran;
        }

        // Returns an array of minimal Gumbel (Type I EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMinRand(double mu, double sigma,  int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma+mu;
                }
                return ran;
        }

        // Returns an array of maximal Gumbel (Type I EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMaxRand(double mu, double sigma,  int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
                }
                return ran;
        }

       // Returns an array of maximal Gumbel (Type I EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = scale parameter, n = length of array
        public static double[] gumbelMaxRand(double mu, double sigma,  int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = mu-Math.log(Math.log(1.0D/(1.0D-rr.nextDouble())))*sigma;
                }
                return ran;
        }

        // Minimum Gumbel mean
        public static double gumbelMinMean(double mu,double sigma){
                return mu - sigma*Fmath.EULER_CONSTANT_GAMMA;
        }

        // Maximum Gumbel mean
        public static double gumbelMaxMean(double mu,double sigma){
                return mu + sigma*Fmath.EULER_CONSTANT_GAMMA;
        }

        // Minimum Gumbel standard deviation
        public static double gumbelMinStandDev(double sigma){
                return sigma*Math.PI/Math.sqrt(6.0D);
        }

        // Maximum Gumbel standard deviation
        public static double gumbelMaxStandDev(double sigma){
                return sigma*Math.PI/Math.sqrt(6.0D);
        }

        // Minimum Gumbel mode
        public static double gumbelMinMode(double mu,double sigma){
            return mu;
        }

        // Maximum Gumbel mode
        public static double gumbelMaxMode(double mu,double sigma){
            return mu;
        }

        // Minimum Gumbel median
        public static double gumbelMinMedian(double mu,double sigma){
            return mu + sigma*Math.log(Math.log(2.0D));
        }

        // Maximum Gumbel median
        public static double gumbelMaxMedian(double mu,double sigma){
            return mu - sigma*Math.log(Math.log(2.0D));
        }


        // Weibull cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double weibullCDF(double mu, double sigma, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-Math.pow(arg, gamma));
                return y;
        }

        // Weibull cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double weibullProb(double mu, double sigma, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-Math.pow(arg, gamma));
                return y;
        }


         // Weibull cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double weibullCDF(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-Math.pow(arg1, gamma));
                if(arg2>=0.0D)term2 = -Math.exp(-Math.pow(arg2, gamma));
                return term2-term1;
        }

        // Weibull cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double weibullProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-Math.pow(arg1, gamma));
                if(arg2>=0.0D)term2 = -Math.exp(-Math.pow(arg2, gamma));
                return term2-term1;
        }

         // Weibull probability
        public static double weibullPDF(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, gamma-1.0D)*Math.exp(-Math.pow(arg, gamma));
                }
                return y;
        }

        // Weibull probability
        public static double weibull(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, gamma-1.0D)*Math.exp(-Math.pow(arg, gamma));
                }
                return y;
        }

        // Weibull mean
        public static double weibullMean(double mu,double sigma, double gamma){
                return mu + sigma*Stat.gamma(1.0D/gamma+1.0D);
        }

        // Weibull standard deviation
        public static double weibullStandDev(double sigma, double gamma){
                double y = Stat.gamma(2.0D/gamma+1.0D)-Fmath.square(Stat.gamma(1.0D/gamma+1.0D));
                return sigma*Math.sqrt(y);
        }

        // Weibull mode
        public static double weibullMode(double mu,double sigma, double gamma){
            double y=mu;
            if(gamma>1.0D){
                y = mu + sigma*Math.pow((gamma-1.0D)/gamma, 1.0D/gamma);
            }
            return y;
        }

        // Weibull median
        public static double weibullMedian(double mu,double sigma, double gamma){
            return mu + sigma*Math.pow(Math.log(2.0D),1.0D/gamma);
        }

        // Returns an array of Weibull (Type III EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] weibullRand(double mu, double sigma, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Returns an array of Weibull (Type III EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] weibullRand(double mu, double sigma, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(-Math.log(1.0D-rr.nextDouble()),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Frechet cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double frechetProb(double mu, double sigma, double gamma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = Math.exp(-Math.pow(arg, -gamma));
                return y;
        }


        // Frechet cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double frechetCDF(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = Math.exp(-Math.pow(arg1, -gamma));
                if(arg2>=0.0D)term2 = Math.exp(-Math.pow(arg2, -gamma));
                return term2-term1;
        }

        // Frechet cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double frechetProb(double mu, double sigma, double gamma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = Math.exp(-Math.pow(arg1, -gamma));
                if(arg2>=0.0D)term2 = Math.exp(-Math.pow(arg2, -gamma));
                return term2-term1;
        }

        // Frechet probability
        public static double frechetPDF(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, -gamma-1.0D)*Math.exp(-Math.pow(arg, -gamma));
                }
                return y;
        }

        // Frechet probability
        public static double frechet(double mu,double sigma, double gamma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (gamma/sigma)*Math.pow(arg, -gamma-1.0D)*Math.exp(-Math.pow(arg, -gamma));
                }
                return y;
        }

        // Frechet mean
        public static double frechetMean(double mu,double sigma, double gamma){
                double y = Double.NaN;
                if(gamma>1.0D){
                    y = mu + sigma*Stat.gamma(1.0D-1.0D/gamma);
                }
                return y;
        }

        // Frechet standard deviation
        public static double frechetStandDev(double sigma, double gamma){
                double y = Double.NaN;
                if(gamma>2.0D){
                    y = Stat.gamma(1.0D-2.0D/gamma)-Fmath.square(Stat.gamma(1.0D-1.0D/gamma));
                    y = sigma*Math.sqrt(y);
                }
                return y;
        }

        // Frechet mode
        public static double frechetMode(double mu,double sigma, double gamma){
                return mu + sigma*Math.pow(gamma/(1.0D+gamma), 1.0D/gamma);
         }

        // Returns an array of Frechet (Type II EVD) random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] frechetRand(double mu, double sigma, double gamma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

        // Returns an array of Frechet (Type II EVD) random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] frechetRand(double mu, double sigma, double gamma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow((1.0D/(Math.log(1.0D/rr.nextDouble()))),1.0D/gamma)*sigma + mu;
                }
                return ran;
        }

         // Exponential cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double exponentialCDF(double mu, double sigma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg);
                return y;
        }

        // Exponential cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double exponentialProb(double mu, double sigma, double upperlimit){
                double arg = (upperlimit - mu)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg);
                return y;
        }

        // Exponential cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double exponentialCDF(double mu, double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2);
                return term2-term1;
        }

        // Exponential cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double exponentialProb(double mu, double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit - mu)/sigma;
                double arg2 = (upperlimit - mu)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2);
                return term2-term1;
        }

        // Exponential probability
        public static double exponentialPDF(double mu,double sigma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = Math.exp(-arg)/sigma;
                }
                return y;
        }

        // Exponential probability
        public static double exponential(double mu,double sigma, double x){
                double arg =(x-mu)/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = Math.exp(-arg)/sigma;
                }
                return y;
        }

        // Exponential mean
        public static double exponentialMean(double mu, double sigma){
                return mu + sigma;
        }

        // Exponential standard deviation
        public static double exponentialStandDev(double sigma){
                return sigma;
        }

        // Exponential mode
        public static double exponentialMode(double mu){
            return mu;
        }

        // Exponential median
        public static double exponentialMedian(double mu,double sigma){
            return mu + sigma*Math.log(2.0D);
        }

        // Returns an array of Exponential random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] exponentialRand(double mu, double sigma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = mu - Math.log(1.0D-rr.nextDouble())*sigma;
                }
                return ran;
        }

        // Returns an array of Exponential random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] exponentialRand(double mu, double sigma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = mu - Math.log(1.0D-rr.nextDouble())*sigma;
                }
                return ran;
        }

        // Rayleigh cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double rayleighCDF(double sigma, double upperlimit){
                double arg = (upperlimit)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg*arg/2.0D);
                return y;
        }

        // Rayleigh cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double rayleighProb(double sigma, double upperlimit){
                double arg = (upperlimit)/sigma;
                double y = 0.0D;
                if(arg>0.0D)y = 1.0D - Math.exp(-arg*arg/2.0D);
                return y;
        }

        // Rayleigh cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double rayleighCDF(double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit)/sigma;
                double arg2 = (upperlimit)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1*arg1/2.0D);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2*arg2/2.0D);
                return term2-term1;
        }

        // Rayleigh cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double rayleighProb(double sigma, double lowerlimit, double upperlimit){
                double arg1 = (lowerlimit)/sigma;
                double arg2 = (upperlimit)/sigma;
                double term1 = 0.0D, term2 = 0.0D;
                if(arg1>=0.0D)term1 = -Math.exp(-arg1*arg1/2.0D);
                if(arg2>=0.0D)term2 = -Math.exp(-arg2*arg2/2.0D);
                return term2-term1;
        }

        // Rayleigh probability
        public static double rayleighPDF(double sigma, double x){
                double arg =x/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (arg/sigma)*Math.exp(-arg*arg/2.0D)/sigma;
                }
                return y;
        }

        // Rayleigh probability
        public static double rayleigh(double sigma, double x){
                double arg =x/sigma;
                double y = 0.0D;
                if(arg>=0.0D){
                    y = (arg/sigma)*Math.exp(-arg*arg/2.0D)/sigma;
                }
                return y;
        }

        // Rayleigh mean
        public static double rayleighMean(double sigma){
                return sigma*Math.sqrt(Math.PI/2.0D);
        }

        // Rayleigh standard deviation
        public static double rayleighStandDev(double sigma){
                return sigma*Math.sqrt(2.0D-Math.PI/2.0D);
        }

        // Rayleigh mode
        public static double rayleighMode(double sigma){
            return sigma;
        }

        // Rayleigh median
        public static double rayleighMedian(double sigma){
            return sigma*Math.sqrt(Math.log(2.0D));
        }

        // Returns an array of Rayleigh random deviates - clock seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] rayleighRand(double sigma, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
                }
                return ran;
        }

        // Returns an array of Rayleigh random deviates - user supplied seed
        // mu  =  location parameter, sigma = cale parameter, gamma = shape parametern = length of array
        public static double[] rayleighRand(double sigma, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.sqrt(-2.0D*Math.log(1.0D-rr.nextDouble()))*sigma;
                }
                return ran;
        }

        // Pareto cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double paretoCDF(double alpha, double beta, double upperlimit){
                double y = 0.0D;
                if(upperlimit>=beta)y = 1.0D - Math.pow(beta/upperlimit, alpha);
                return y;
        }

        // Pareto cumulative distribution function
        // probability that a variate will assume  a value less than the upperlimit
        public static double paretoProb(double alpha, double beta, double upperlimit){
                double y = 0.0D;
                if(upperlimit>=beta)y = 1.0D - Math.pow(beta/upperlimit, alpha);
                return y;
        }

         // Pareto cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double paretoCDF(double alpha, double beta, double lowerlimit, double upperlimit){
                double term1 = 0.0D, term2 = 0.0D;
                if(lowerlimit>=beta)term1 = -Math.pow(beta/lowerlimit, alpha);
                if(upperlimit>=beta)term2 = -Math.pow(beta/upperlimit, alpha);
                return term2-term1;
        }

        // Pareto cumulative distribution function
        // probability that a variate will assume a value between the lower and  the upper limits
        public static double paretoProb(double alpha, double beta, double lowerlimit, double upperlimit){
                double term1 = 0.0D, term2 = 0.0D;
                if(lowerlimit>=beta)term1 = -Math.pow(beta/lowerlimit, alpha);
                if(upperlimit>=beta)term2 = -Math.pow(beta/upperlimit, alpha);
                return term2-term1;
        }

         // Pareto probability
        public static double paretoPDF(double alpha, double beta, double x){
                double y = 0.0D;
                if(x>=beta){
                    y = alpha*Math.pow(beta, alpha)/Math.pow(x, alpha+1.0D);
                }
                return y;
        }

        // Pareto probability
        public static double pareto(double alpha, double beta, double x){
                double y = 0.0D;
                if(x>=beta){
                    y = alpha*Math.pow(beta, alpha)/Math.pow(x, alpha+1.0D);
                }
                return y;
        }

        // Pareto mean
        public static double paretoMean(double alpha, double beta){
                double y = Double.NaN;
                if(alpha>1.0D)y = alpha*beta/(alpha-1);
                return y;
        }

        // Pareto standard deviation
        public static double paretoStandDev(double alpha, double beta){
                double y = Double.NaN;
                if(alpha>1.0D)y = alpha*Fmath.square(beta)/(Fmath.square(alpha-1)*(alpha-2));
                return y;
        }

        // Pareto mode
        public static double paretoMode(double beta){
            return beta;
        }

        // Returns an array of Pareto random deviates - clock seed
        public static double[] paretoRand(double alpha, double beta, int n){
                double[] ran = new double[n];
                Random rr = new Random();
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
                }
                return ran;
        }

        // Returns an array of Pareto random deviates - user supplied seed
        public static double[] paretoRand(double alpha, double beta, int n, long seed){
                double[] ran = new double[n];
                Random rr = new Random(seed);
                for(int i=0; i<n; i++){
                     ran[i] = Math.pow(1.0D-rr.nextDouble(), -1.0D/alpha)*beta;
                }
                return ran;
        }

        // Fit a data set to one, several or all of the above distributions
        public static void fitOneOrSeveralDistributions(double[] array){
            Regression.fitOneOrSeveralDistributions(array);
        }

        // Anscombe test for a upper outlier
        public static Vector<Object> upperOutliersAnscombe(double[] values, double constant){
            boolean test=true;
            double[] copy0 = (double[])values.clone();
            int nValues = values.length;
            int nValues0 = nValues;
            Vector<Double> outers = new Vector<Double>();
            int nOutliers = 0;

            while(test){
                boolean copyCheck = true;
                double mean = Stat.mean(copy0);
                double standDev = Stat.standardDeviation(copy0);
                double max = Fmath.maximum(copy0);
                double statistic = (max - mean)/standDev;
                int j=0;
                if(statistic>constant){
                    outers.addElement(new Double(max));
                    nOutliers++;
                    double[] copy1 = new double[nValues-1];
                    for(int i=0; i<nValues; i++){
                        if(copy0[i]==max && copyCheck){
                            copyCheck = false;
                        }
                        else{
                            copy1[j] = copy0[i];
                            j++;
                        }
                    }
                    nValues--;
                    copy0 = (double[])copy1.clone();
                }
                else{
                    test=false;
                }
            }

            double[] outliers = null;
            int[] outIndices = null;
            if(nOutliers>0){
                outliers = new double[nOutliers];
                outIndices = new int[nOutliers];
                boolean[] indCheck = new boolean[nValues0];
                for(int i=0; i<nValues0; i++)indCheck[i]=true;
                for(int i=0; i<nOutliers; i++)outliers[i] = ((Double)outers.elementAt(i)).doubleValue();
                for(int j=0; j<nOutliers; j++){
                    boolean testO = true;
                    int i = 0;
                    while(testO){
                        if(outliers[j]==values[i] && indCheck[i]){
                            outIndices[j] = i;
                            indCheck[i]=false;
                            testO = false;
                        }
                        else{
                            i++;
                            if(i>=nValues0){
                                testO=false;
                                System.out.println("Stat.outliersUpperAnscombe");
                                System.out.println("Could not find an outlier index!!!!!!!!!");
                            }
                        }
                    }
                }
            }

            Vector<Object> ret = new Vector<Object>();
            ret.addElement(new Integer(nOutliers));
            ret.addElement(outliers);
            ret.addElement(outIndices);
            ret.addElement(copy0);

            return ret;
        }


        // Anscombe test for a lower outlier
        public static Vector<Object> lowerOutliersAnscombe(double[] values, double constant){
            boolean test=true;
            double[] copy0 = (double[])values.clone();
            int nValues = values.length;
            int nValues0 = nValues;
            Vector<Double> outers = new Vector<Double>();
            int nOutliers = 0;

            while(test){
                boolean copyCheck = true;
                double mean = Stat.mean(copy0);
                double standDev = Stat.standardDeviation(copy0);
                double min = Fmath.minimum(copy0);
                double statistic = (mean - min)/standDev;
                int j=0;
                if(statistic>constant){
                    outers.addElement(new Double(min));
                    nOutliers++;
                    double[] copy1 = new double[nValues-1];
                    for(int i=0; i<nValues; i++){
                        if(copy0[i]==min && copyCheck){
                            copyCheck = false;
                        }
                        else{
                            copy1[j] = copy0[i];
                            j++;
                        }
                    }
                    nValues--;
                    copy0 = (double[])copy1.clone();
                }
                else{
                    test=false;
                }
            }

            double[] outliers = null;
            int[] outIndices = null;
            if(nOutliers>0){
                outliers = new double[nOutliers];
                outIndices = new int[nOutliers];
                boolean[] indCheck = new boolean[nValues0];
                for(int i=0; i<nValues0; i++)indCheck[i]=true;
                for(int i=0; i<nOutliers; i++)outliers[i] = ((Double)outers.elementAt(i)).doubleValue();
                for(int j=0; j<nOutliers; j++){
                    boolean testO = true;
                    int i = 0;
                    while(testO){
                        if(outliers[j]==values[i] && indCheck[i]){
                            outIndices[j] = i;
                            indCheck[i]=false;
                            testO = false;
                        }
                        else{
                            i++;
                            if(i>=nValues0){
                                testO=false;
                                System.out.println("Stat.outliersUpperAnscombe");
                                System.out.println("Could not find an outlier index!!!!!!!!!");
                            }
                        }
                    }
                }
            }

            Vector<Object> ret = new Vector<Object>();
            ret.addElement(new Integer(nOutliers));
            ret.addElement(outliers);
            ret.addElement(outIndices);
            ret.addElement(copy0);

            return ret;
        }

 }


// Class to evaluate the Student's t-function
class StudentTfunct implements RealRootFunction{
        public int nu = 0;
        public double cfd = 0.0D;

        public double function(double x){

            double y = cfd - Stat.studentTcdf(x, nu);

            return y;
        }
}

// Class to evaluate the Gamma distribution function
class GammaFunct implements RealRootFunction{
        public double mu = 0.0D;
        public double beta = 0.0D;
        public double gamma = 0.0D;
        public double cfd = 0.0D;

        public double function(double x){

            double y = cfd - Stat.gammaCDF(mu, beta, gamma, x);

            return y;
        }
}

// Class to evaluate the Beta distribution function
class BetaFunct implements RealRootFunction{
        public double alpha = 0.0D;
        public double beta = 0.0D;
        public double min = 0.0D;
        public double max = 0.0D;
        public double cfd = 0.0D;

        public double function(double x){

            double y = cfd - Stat.betaCDF(min, max, alpha, beta, x);

            return y;
        }
}

// Class to evaluate the Erlang B equation
class ErlangBfunct implements RealRootFunction{

         public double blockingProbability = 0.0D;
         public double totalResources = 0.0D;

         public double function(double x){
                return blockingProbability - Stat.erlangBprobability(x, totalResources);
         }
}

// Class to evaluate the Erlang C equation
class ErlangCfunct implements RealRootFunction{

         public double nonZeroDelayProbability = 0.0D;
         public double totalResources = 0.0D;

         public double function(double x){
                return nonZeroDelayProbability - Stat.erlangCprobability(x, totalResources);
         }
}

// Class to evaluate the Engset probability equation
class EngsetProb implements RealRootFunction{

         public double offeredTraffic = 0.0D;
         public double totalResources = 0.0D;
         public double numberOfSources = 0.0D;

         public double function(double x){
                double mTerm = offeredTraffic/(numberOfSources - offeredTraffic*(1.0D - x));
                double pNumer = Stat.logFactorial(numberOfSources-1) - Stat.logFactorial(totalResources) - Stat.logFactorial(numberOfSources-1-totalResources);
                double pDenom = 0.0D;
                double iDenom = 0.0D;
                double iCount = 0.0D;
                double pTerm = 0.0D;
                while(iCount<=totalResources){
                    iDenom = Stat.logFactorial(numberOfSources-1) - Stat.logFactorial(iCount) - Stat.logFactorial(numberOfSources-1-iCount);
                    iDenom += (iCount-totalResources)*Math.log(mTerm);
                    pDenom += Math.exp(iDenom);
                    iCount += 1.0D;
                }
                pTerm = Math.exp(pNumer - Math.log(pDenom));

                return x - pTerm;
         }
}


// Class to evaluate the  Engset load equation
class EngsetLoad implements RealRootFunction{

         public double blockingProbability = 0.0D;
         public double totalResources = 0.0D;
         public double numberOfSources = 0.0D;


         public double function(double x){
                return blockingProbability - Stat.engsetProbability(x, totalResources, numberOfSources);
         }
}