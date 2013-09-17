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
package com.googlecode.clearnlp.component.pos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.classification.model.ONStringModel;
import com.googlecode.clearnlp.classification.vector.StringFeatureVector;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.util.pair.Pair;

/**
 * Part-of-speech tagger using document frequency cutoffs.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ONPOSTagger extends EnglishPOSTagger
{
	ONStringModel o_model;
	
//	====================================== CONSTRUCTORS ======================================
	
	public ONPOSTagger(JointFtrXml[] xmls, Object[] lexica, double alpha, double rho)
	{
		f_xmls   = xmls;
		s_models = new ONStringModel[]{new ONStringModel(alpha, rho)};
		initLexia(lexica);
		initOnline();
	}
	
	public ONPOSTagger(ZipInputStream zin, double alpha, double rho)
	{
		loadModels(zin, alpha, rho);
		initOnline();
	}
	
	private void initOnline()
	{
		i_flag  = FLAG_DECODE;
		o_model = (ONStringModel)s_models[0];
	}
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	public void loadModels(ZipInputStream zin, double alpha, double rho)
	{
		int fLen = ENTRY_FEATURE.length(), mLen = ENTRY_MODEL.length();
		f_xmls   = new JointFtrXml[1];
		s_models = null;
		ZipEntry zEntry;
		String   entry;
				
		try
		{
			while ((zEntry = zin.getNextEntry()) != null)
			{
				entry = zEntry.getName();
				
				if      (entry.equals(ENTRY_CONFIGURATION))
					loadDefaultConfiguration(zin);
				else if (entry.startsWith(ENTRY_FEATURE))
					loadFeatureTemplates(zin, Integer.parseInt(entry.substring(fLen)));
				else if (entry.startsWith(ENTRY_MODEL))
					loadOnlineModels(zin, Integer.parseInt(entry.substring(mLen)), alpha, rho);
				else if (entry.equals(ENTRY_LEXICA))
					loadLexica(zin);
			}		
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
//	====================================== TRAIN ======================================
	
	public void trainHard(DEPTree tree, int maxIter)
	{
		List<Pair<String,StringFeatureVector>> insts;
		int[] counts = new int[2];
		byte flag = i_flag;
		int i;
		
		i_flag = FLAG_BOOTSTRAP;
		init(tree);
		
		for (i=0; i<maxIter; i++)
		{
			tree.clearPOSTags();
			Arrays.fill(counts, 0);
			
			insts = tag();
			countAccuracy(counts);
			
			if (counts[0] == counts[1])	break;
			o_model.updateWeights(insts);
		}
		
		i_flag = flag;
	}
	
	public void train(List<DEPTree> trees, int bIdx, int eIdx)
	{
		List<Pair<String,StringFeatureVector>> insts = new ArrayList<Pair<String,StringFeatureVector>>();
		byte flag = i_flag;
		int i;
		
		i_flag = FLAG_TRAIN;
		
		for (i=bIdx; i<eIdx; i++)
		{
			init(trees.get(i));
			insts.addAll(tag());
		}

		o_model.updateWeights(insts);
		i_flag = flag;		
	}
	
	public void train(DEPTree tree)
	{
		byte flag = i_flag;
		
		i_flag = FLAG_BOOTSTRAP;
		init(tree);
		
		o_model.updateWeights(tag());
		i_flag = flag;		
	}
	
	public void develop(DEPTree tree)
	{
		byte flag = i_flag;
		
		i_flag = FLAG_DEVELOP;
		process(tree);
		
		i_flag = flag;
	}
	
	public void resetGold()
	{
		int i; for (i=1; i<t_size; i++)
			d_tree.get(i).pos = g_tags[i];
	}
}
