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
package com.googlecode.clearnlp.experiment;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.component.AbstractStatisticalComponent;
import com.googlecode.clearnlp.component.pos.EnglishPOSTagger;
import com.googlecode.clearnlp.component.pos.ONPOSTagger;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.nlp.NLPDevelop;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.JointReader;
import com.googlecode.clearnlp.util.UTFile;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTXml;

public class ONPOSTrain extends NLPDevelop
{
	public ONPOSTrain(String[] args)
	{
		super();
		initArgs(args);
		
		try
		{
			develop(s_configFile, s_featureFiles.split(DELIM_FILES), s_trainDir, s_devDir, s_mode);
		}
		catch (Exception e) {e.printStackTrace();}
	}

	public void develop(String configFile, String[] featureFiles, String trainDir, String devDir, String mode) throws Exception
	{
		Element     eConfig = UTXml.getDocumentElement(new FileInputStream(configFile));
		JointFtrXml[]  xmls = getFeatureTemplates(featureFiles);
		String[] trainFiles = UTFile.getSortedFileListBySize(trainDir, ".*", true);
		String[]   devFiles = UTFile.getSortedFileListBySize(devDir, ".*", true);
		JointReader  reader = getJointReader(UTXml.getFirstElementByTagName(eConfig, TAG_READER));
		
		AbstractStatisticalComponent component = new EnglishPOSTagger(xmls, getLowerSimplifiedForms(reader, xmls[0], trainFiles, -1));
		Object[] lexica = (component != null) ? getLexica(component, reader, xmls, trainFiles, -1) : null;
		
		ONPOSTagger tagger = new ONPOSTagger(xmls, lexica, 0.01, 0.1);
		List<DEPTree> trainTrees = getTrees(reader, trainFiles);
		List<DEPTree> devTrees = getTrees(reader, devFiles);
		int i, size = trainTrees.size(), cut = size / 10;
		double prevScore = -1, currScore;
		
		tagger.train(trainTrees, 0, cut);
				
		for (i=cut; i<size; i++)
			tagger.train(trainTrees.get(i));
		
		currScore = decode(devTrees, tagger);
		
		while (prevScore < currScore)
		{
			prevScore = currScore;
			
			for (i=0; i<size; i++)
				tagger.train(trainTrees.get(i));
			
			currScore = decode(devTrees, tagger);
		}
	}
	
	protected List<DEPTree> getTrees(JointReader reader, String[] inputFiles)
	{
		List<DEPTree> trees = new ArrayList<DEPTree>();
		DEPTree tree;
		
		for (String inputFile : inputFiles)
		{
			reader.open(UTInput.createBufferedFileReader(inputFile));
			
			while ((tree = reader.next()) != null)
				trees.add(tree);	

			reader.close();
		}

		return trees;
	}
	
	protected double decode(List<DEPTree> devTrees, ONPOSTagger tagger)
	{
		int[] counts = getCounts(NLPLib.MODE_POS);
		
		for (DEPTree tree : devTrees)
		{
			tagger.develop(tree);
			tagger.countAccuracy(counts);
			tagger.resetGold();
		}
		
		return getScore(NLPLib.MODE_POS, counts);
	}
	
	static public void main(String[] args)
	{
		new ONPOSTrain(args);
	}
}
