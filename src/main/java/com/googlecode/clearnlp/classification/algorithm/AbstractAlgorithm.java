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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.clearnlp.classification.prediction.StringPrediction;
import com.googlecode.clearnlp.classification.train.AbstractTrainSpace;

/**
 * Abstract algorithm.
 * @since 1.0.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractAlgorithm
{
	protected final Logger LOG = LoggerFactory.getLogger(this.getClass());
	
	/** The flag to indicate L2-regularized L1-loss support vector classification (dual). */
	static public final byte SOLVER_LIBLINEAR_LR2_L1_SV = 0;
	/** The flag to indicate L2-regularized L2-loss support vector classification (dual). */
	static public final byte SOLVER_LIBLINEAR_LR2_L2_SV = 1;
	/** The flag to indicate L2-regularized logistic regression (dual). */
	static public final byte SOLVER_LIBLINEAR_LR2_LR = 2;
	/** The flag to indicate adaptive gradient method using hinge loss. */
	static public final byte SOLVER_ADAGRAD_HINGE = 3;
	/** The flag to indicate adaptive gradient method using logistic regression. */
	static public final byte SOLVER_ADAGRAD_LR = 4;
	
	/**
	 * Returns the weight vector for the specific label given the training space.
	 * @param space the training space.
	 * @param currLabel the label to get the weight vector for.
	 * @return the weight vector for the specific label given the training space.
	 */
	abstract public double[] getWeight(AbstractTrainSpace space, int currLabel);
	
	/** @param L the number of labels. */
	protected int getWeightIndex(int L, int label, int index)
	{
		return index * L + label;
	}
	
	protected void normalize(double[] scores)
	{
		int i, size = scores.length;
		double d, sum = 0;
		
		for (i=0; i<size; i++)
		{
			d = Math.exp(scores[i]);
			scores[i] = d;
			sum += d;
		}
		
		for (i=0; i<size; i++)
			scores[i] /= sum;
	}
	
	static public void normalize(List<StringPrediction> ps)
	{
		int i, size = ps.size();
		StringPrediction p;
		double d, sum = 0;
		
		for (i=0; i<size; i++)
		{
			p = ps.get(i);
			d = Math.exp(p.score);
			p.score = d;
			sum += d;
		}
		
		for (i=0; i<size; i++)
			ps.get(i).score /= sum; 
	}
}
