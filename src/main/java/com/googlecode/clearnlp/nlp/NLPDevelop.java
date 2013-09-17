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
package com.googlecode.clearnlp.nlp;

import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

import org.kohsuke.args4j.Option;
import org.w3c.dom.Element;

import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.classification.train.StringTrainSpace;
import com.googlecode.clearnlp.component.AbstractStatisticalComponent;
import com.googlecode.clearnlp.component.pos.CPOSTaggerSB;
import com.googlecode.clearnlp.component.srl.CRolesetClassifier;
import com.googlecode.clearnlp.component.srl.CSenseClassifier;
import com.googlecode.clearnlp.constant.universal.STPunct;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.dependency.srl.SRLEval;
import com.googlecode.clearnlp.reader.JointReader;
import com.googlecode.clearnlp.util.UTFile;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;
import com.googlecode.clearnlp.util.UTXml;
import com.googlecode.clearnlp.util.pair.ObjectDoublePair;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class NLPDevelop extends NLPTrain
{
	@Option(name="-d", usage="the directory containing development files (required)", required=true, metaVar="<directory>")
	protected String s_devDir;
	@Option(name="-g", usage="if set, generate files", required=false, metaVar="<boolean>")
	protected boolean b_generate = false;
	
	public NLPDevelop() {}
	
	public NLPDevelop(String[] args)
	{
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
		String     language = getLanguage(eConfig);
		
		if      (mode.equals(NLPLib.MODE_POS))
			developComponentBoot(eConfig, reader, xmls, trainFiles, devFiles, getPOSTaggerForCollect(reader, xmls, trainFiles, -1, language), mode, -1);
		else if (mode.equals(NLPLib.MODE_DEP))
			developComponentBoot(eConfig, reader, xmls, trainFiles, devFiles, null, mode, -1);
		else if (mode.equals(NLPLib.MODE_PRED))
			decode(reader, getTrainedComponent(eConfig, xmls, trainFiles, null, null, mode, 0, -1), devFiles, mode, mode);
		else if (mode.equals(NLPLib.MODE_ROLE))
			decode(reader, getTrainedComponent(eConfig, reader, xmls, trainFiles, new CRolesetClassifier(xmls), mode, -1), devFiles, mode, mode);
		else if (mode.equals(NLPLib.MODE_SRL))
			developComponentBoot(eConfig, reader, xmls, trainFiles, devFiles, getSRLabelerForCollect(xmls, language), mode, -1);
		else if (mode.equals(NLPLib.MODE_POS_SB))
			developComponentBoot(eConfig, reader, xmls, trainFiles, devFiles, new CPOSTaggerSB(xmls, getLowerSimplifiedForms(reader, xmls[0], trainFiles, -1)), mode, -1);
		else if (mode.startsWith(NLPLib.MODE_SENSE))
			decode(reader, getTrainedComponent(eConfig, reader, xmls, trainFiles, new CSenseClassifier(xmls, mode.substring(mode.lastIndexOf(STPunct.UNDERSCORE)+1)), mode, -1), devFiles, mode, mode);
	}
	
	protected double developComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, Object[] lexica, String mode, int devId) throws Exception
	{
		StringTrainSpace[] spaces = getStringTrainSpaces(eConfig, xmls, trainFiles, null, lexica, mode, 0, devId);
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, mode);
		int i, mSize = spaces.length, nUpdate = 1;
		AbstractStatisticalComponent processor;
		String language = getLanguage(eConfig);
		
		StringModel[] models = new StringModel[mSize];
		double prevScore = -1, currScore = 0;
		Random rand = new Random(i_rand);
		int iter = 0;
		
		do
		{
			prevScore = currScore;
			
			for (i=0; i<mSize; i++)
			{
				updateModel(eTrain, spaces[i], rand, nUpdate++, i);
				models[i] = (StringModel)spaces[i].getModel();
			}

			processor = getComponent(xmls, models, lexica, mode, language);
			currScore = decode(reader, processor, devFiles, mode, Integer.toString(iter));
			iter++;
		}
		while (prevScore < currScore);
		
		return prevScore;
	}
	
	protected double developComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, AbstractStatisticalComponent component, String mode, int devId) throws Exception
	{
		Object[] lexica = (component != null) ? getLexica(component, reader, xmls, trainFiles, devId) : null;
		return developComponent(eConfig, reader, xmls, trainFiles, devFiles, lexica, mode, devId);
	}
	
	protected void developComponentBoot(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, AbstractStatisticalComponent component, String mode, int devId) throws Exception
	{
		Object[] lexica = (component != null) ? getLexica(component, reader, xmls, trainFiles, devId) : null;
		ObjectDoublePair<StringModel[]> p;
		double prevScore, currScore = 0;
		StringModel[] models = null;
		int boot = 0;
		
		do
		{
			prevScore = currScore;
			p = developComponent(eConfig, reader, xmls, trainFiles, devFiles, lexica, models, mode, boot, devId);
			models = (StringModel[])p.o;
			currScore = p.d;
			boot++;
		}
		while (prevScore < currScore);
	}
	
	private ObjectDoublePair<StringModel[]> developComponent(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, Object[] lexica, StringModel[] models, String mode, int boot, int devId) throws Exception
	{
		StringTrainSpace[] spaces = getStringTrainSpaces(eConfig, xmls, trainFiles, models, lexica, mode, boot, devId);
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, mode);
		int i, mSize = spaces.length, nUpdate = 1;
		AbstractStatisticalComponent component;
		String language = getLanguage(eConfig);
		
		double prevScore = -1, currScore = 0;
		Random[] rands = new Random[mSize];
		models = new StringModel[mSize];

		double[][] prevWeights = new double[mSize][];
		double[] d;
		
		for (i=0; i<mSize; i++)
			rands[i] = new Random(i_rand);
		
		do
		{
			prevScore = currScore;
			
			for (i=0; i<mSize; i++)
			{
				if (models[i] != null)
				{
					d = models[i].getWeights();
					prevWeights[i] = Arrays.copyOf(d, d.length);
				}
				
				updateModel(eTrain, spaces[i], rands[i], nUpdate, i);
				models[i] = (StringModel)spaces[i].getModel();
			}
			
			component = getComponent(xmls, models, lexica, mode, language);
			currScore = decode(reader, component, devFiles, mode, boot+"."+nUpdate);
			nUpdate++;
		}
		while (prevScore < currScore);
		
		for (i=0; i<mSize; i++)
			models[i].setWeights(prevWeights[i]);
		
		return new ObjectDoublePair<StringModel[]>(models, prevScore);
	}
	
	protected double decode(JointReader reader, AbstractStatisticalComponent component, String[] devFiles, String mode, String ext) throws Exception
	{
		int[] counts = getCounts(mode);
		PrintStream fout = null;
		DEPTree tree;
		
		for (String devFile : devFiles)
		{
			if (b_generate) fout = UTOutput.createPrintBufferedFileStream(devFile+"."+ext);
			reader.open(UTInput.createBufferedFileReader(devFile));
			
			while ((tree = reader.next()) != null)
			{
				component.process(tree);
				component.countAccuracy(counts);
				if (b_generate)	fout.println(toString(tree, mode)+"\n");
			}
			
			reader.close();
			if (b_generate)	fout.close();
		}

		return getScore(mode, counts);
	}
	
	protected int[] getCounts(String mode)
	{
		if      (mode.startsWith(NLPLib.MODE_POS) || mode.equals(NLPLib.MODE_ROLE) || mode.startsWith(NLPLib.MODE_SENSE))
			return new int[2];
		else if (mode.equals(NLPLib.MODE_DEP))
			return new int[4];
		else if (mode.equals(NLPLib.MODE_PRED) || mode.equals(NLPLib.MODE_SRL))
			return new int[3];
		
		return null;
	}
	
	protected double getScore(String mode, int[] counts)
	{
		double score = 0;
		
		if (mode.startsWith(NLPLib.MODE_POS) || mode.equals(NLPLib.MODE_ROLE) || mode.startsWith(NLPLib.MODE_SENSE))
		{
			score = 100d * counts[1] / counts[0];
			LOG.info(String.format("- ACC: %5.2f (%d/%d)\n", score, counts[1], counts[0]));
		}
		else if (mode.equals(NLPLib.MODE_DEP))
		{
			String[] labels = {"T","LAS","UAS","LS"};
			printScores(labels, counts);

			score = 100d * counts[1] / counts[0];
		}
		else if (mode.equals(NLPLib.MODE_PRED) || mode.equals(NLPLib.MODE_SRL))
		{
			double p = 100d * counts[0] / counts[1];
			double r = 100d * counts[0] / counts[2];
			score = SRLEval.getF1(p, r);
			
			LOG.info(String.format("P: %5.2f ", p));
			LOG.info(String.format("R: %5.2f ", r));
			LOG.info(String.format("F1: %5.2f\n", score));
		}
		
		return score;
	}
	
	private void printScores(String[] labels, int[] counts)
	{
		int i, t = counts[0], size = counts.length;
		
		for (i=1; i<size; i++)
			LOG.info(String.format("%3s: %5.2f (%d/%d)\n", labels[i], 100d*counts[i]/t, counts[i], t));
	}
	
	static public void main(String[] args)
	{
		new NLPDevelop(args);
	}
	
/*	protected void developComponentBoot2(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, AbstractStatisticalComponent component, String mode, int devId) throws Exception
	{
		Object[] lexica = getLexica(component, reader, xmls, trainFiles, devId);
		ObjectDoublePair<StringModel[]> p;
		double prevScore, currScore = 0;
		StringModel[] models = null;
		int boot = 0;
		
		do
		{
			prevScore = currScore;
			p = developComponent2(eConfig, reader, xmls, trainFiles, devFiles, lexica, models, mode, boot, devId);
			models = (StringModel[])p.o;
			currScore = p.d;
			boot++;
		}
		while (prevScore < currScore);
	}
	
	private ObjectDoublePair<StringModel[]> developComponent2(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles, Object[] lexica, StringModel[] models, String mode, int boot, int devId) throws Exception
	{
		StringTrainSpace[] spaces = getStringTrainSpaces(eConfig, xmls, trainFiles, models, lexica, mode, boot, devId);
		Element eTrain = UTXml.getFirstElementByTagName(eConfig, mode);
		int nUpdate, i, j, mSize = spaces.length;
		AbstractStatisticalComponent component;
		double prevScore = -1, currScore;
		double[] prevWeights, d;
		StringModel[] tmp;
		Random rand;
		
		for (i=0; i<mSize; i++)
		{
			tmp = models;
			models = new StringModel[i+1];
			
			for (j=0; j<i; j++)
				models[j] = tmp[j];
			
			rand = new Random(i_rand);
			prevWeights = null;
			currScore = 0;
			nUpdate = 1;
			
			do
			{
				prevScore = currScore;
				
				if (models[i] != null)
				{
					d = models[i].getWeights();
					prevWeights = Arrays.copyOf(d, d.length);
				}
				
				updateModel(eTrain, spaces[i], rand, nUpdate, i);
				models[i] = (StringModel)spaces[i].getModel();

				component = getComponent(xmls, models, lexica, mode);
				currScore = decode(reader, component, devFiles, mode, Integer.toString(100*boot+nUpdate));
				nUpdate++;
			}
			while (prevScore < currScore);
			
			models[i].setWeights(prevWeights);
		}
		
		return new ObjectDoublePair<StringModel[]>(models, prevScore);
	}

	protected void developPOSTagger(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles) throws Exception
	{
		CPOSTagger tagger = getTrainedPOSTagger(eConfig, reader, xmls, trainFiles, -1);
		predict(reader, tagger, devFiles, COMLib.MODE_POS);
	}
	
	protected void developDEPParser(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles) throws Exception
	{
		Object[] lexica = getLexica(new CDEPPassParser(xmls), reader, xmls, trainFiles, -1);
		double prevScore, currScore = 0;
		StringModel[] models = null;
		CDEPPassParser parser = null;
		
		do
		{
			prevScore = currScore;
			
			parser = (CDEPPassParser)getTrainedComponent(eConfig, xmls, trainFiles, models, lexica, COMLib.MODE_DEP, -1);
			models = parser.getModels();

			currScore = decode(reader, parser, devFiles, COMLib.MODE_DEP);
		}
		while (prevScore < currScore);
	}
	
	protected void developSRLabeler(Element eConfig, JointReader reader, JointFtrXml[] xmls, String[] trainFiles, String[] devFiles) throws Exception
	{
		Object[] lexica = getLexica(new CSRLabeler(xmls), reader, xmls, trainFiles, -1);
		AbstractStatisticalComponent labeler = null;
		double prevScore, currScore = 0;
		StringModel[] models = null;
		
		do
		{
			prevScore = currScore;
			
			labeler = getTrainedComponent(eConfig, xmls, trainFiles, models, lexica, COMLib.MODE_SRL, 0, -1);
			models  = labeler.getModels();

			currScore = decode(reader, labeler, devFiles, COMLib.MODE_SRL);
		}
		while (prevScore < currScore);
	}
	*/
}
