/**
 * Copyright (c) 2009/09-2012/08, Regents of the University of Colorado
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Copyright 2012/09-2013/04, University of Massachusetts Amherst
 * Copyright 2013/05-Present, IPSoft Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.googlecode.clearnlp.classification.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.carrotsearch.hppc.IntArrayList;
import com.googlecode.clearnlp.classification.train.AbstractTrainSpace;

/**
 * AdaGrad algorithm using logistic regression.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class AdaGradLR extends AbstractAdaGrad
{
	/**
	 * @param alpha the learning rate.
	 * @param rho the smoothing denominator.
	 */
	public AdaGradLR(int iter, double alpha, double rho, Random rand)
	{
		super(iter, alpha, rho, rand);
	}
	
	@Override
	public double[] getWeight(AbstractTrainSpace space, int numThreads)
	{
		double[] weights = new double[space.getFeatureSize() * space.getLabelSize()];
		
		updateWeight(space, weights);
		return weights;
	}
	
	public void updateWeight(AbstractTrainSpace space)
	{
		updateWeight(space, space.getModel().getWeights());
	}
	
	public void updateWeight(AbstractTrainSpace space, double[] weights)
	{	
		final int D = space.getFeatureSize();
		final int L = space.getLabelSize();
		final int N = space.getInstanceSize();
		double[] gs = new double[D*L];
		
		IntArrayList        ys = space.getYs();
		ArrayList<int[]>    xs = space.getXs();
		ArrayList<double[]> vs = space.getVs();
		
		int i, j, size = weights.length;
		int[] indices;
		
		int      yi;
		int[]    xi;
		double[] vi = null, grad;
		double[] pWeights = new double[size];
		
		for (i=0; i<n_iter; i++)
		{
			System.arraycopy(weights, 0, pWeights, 0, size);
			indices = getShuffledIndices(N);
			Arrays.fill(gs, 0);
			
			for (j=0; j<N; j++)
			{
				yi = ys.get(indices[j]);
				xi = xs.get(indices[j]);
				if (space.hasWeight())	vi = vs.get(indices[j]);
				
				grad = getGradients(L, yi, xi, vi, weights);
				updateCounts(L, gs, grad, xi, vi);
				updateWeights(L, gs, grad, xi, vi, weights);
			}
		}
	}
	
	protected double[] getGradients(int L, int y, int[] x, double[] v, double[] weights)
	{
		double[] scores = getScores(L, x, v, weights);
		normalize(scores);

		int i; for (i=0; i<L; i++) scores[i] *= -1;
		scores[y] += 1;
		
		return scores;
	}
	
	protected void updateCounts(int L, double[] gs, double[] grad, int[] x, double[] v)
	{
		int i, label, len = x.length;
		double[] g = new double[L];
		double d;

		for (label=0; label<L; label++)
			g[label] = grad[label] * grad[label];
		
		if (v != null)
		{
			for (i=0; i<len; i++)
			{
				d = v[i] * v[i];
				
				for (label=0; label<L; label++)
					gs[getWeightIndex(L, label, x[i])] += d * g[label];
			}
		}
		else
		{
			for (i=0; i<len; i++)
				for (label=0; label<L; label++)
					gs[getWeightIndex(L, label, x[i])] += g[label];
		}
	}
	
	protected void updateWeights(int L, double[] gs, double[] grad, int[] x, double[] v, double[] weights)
	{
		int i, label, len = x.length;
		
		if (v != null)
		{
			for (i=0; i<len; i++)
				for (label=0; label<L; label++)
					weights[getWeightIndex(L, label, x[i])] += getUpdate(L, gs, label, x[i]) * grad[label] * v[i];
		}
		else
		{
			for (i=0; i<len; i++)
				for (label=0; label<L; label++)
					weights[getWeightIndex(L, label, x[i])] += getUpdate(L, gs, label, x[i]) * grad[label];
		}
	}
	
/*	protected void regularize(double[] pWeights, double[] weights, int size)
	{
		int i;
		
		for (i=0; i<size; i++)
			weights[i] -= NLPDevelop.d_reg * pWeights[i]; 
	}*/
}
	