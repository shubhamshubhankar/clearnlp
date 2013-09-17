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
package com.googlecode.clearnlp.component.srl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.carrotsearch.hppc.ObjectIntOpenHashMap;
import com.googlecode.clearnlp.classification.feature.FtrToken;
import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.classification.prediction.StringPrediction;
import com.googlecode.clearnlp.classification.train.StringTrainSpace;
import com.googlecode.clearnlp.classification.vector.StringFeatureVector;
import com.googlecode.clearnlp.component.AbstractStatisticalComponent;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;

/**
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CRolesetClassifier extends AbstractStatisticalComponent
{
	private final String ENTRY_CONFIGURATION = NLPLib.MODE_ROLE + NLPLib.ENTRY_CONFIGURATION;
	private final String ENTRY_FEATURE		 = NLPLib.MODE_ROLE + NLPLib.ENTRY_FEATURE;
	private final String ENTRY_LEXICA		 = NLPLib.MODE_ROLE + NLPLib.ENTRY_LEXICA;
	private final String ENTRY_MODEL		 = NLPLib.MODE_ROLE + NLPLib.ENTRY_MODEL;
	private final String ENTRY_WEIGHTS	     = NLPLib.MODE_ROLE + NLPLib.ENTRY_WEIGHTS;
	
	protected final int LEXICA_ROLESETS  = 0;
	protected final int LEXICA_LEMMAS    = 1;
	
	protected Map<String,Set<String>>		m_collect;	// for collecting lexica
	protected Map<String,String>			m_rolesets;
	protected ObjectIntOpenHashMap<String>	m_lemmas;
	protected String[]						g_rolesets;
	protected int 							i_pred;
	
//	====================================== CONSTRUCTORS ======================================

	/** Constructs a roleset classifier for collecting lexica. */
	public CRolesetClassifier(JointFtrXml[] xmls)
	{
		super(xmls);
		m_collect = new HashMap<String,Set<String>>();
	}
		
	/** Constructs a roleset classifier for training. */
	public CRolesetClassifier(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a roleset classifier for developing. */
	public CRolesetClassifier(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
	}
	
	/** Constructs a roleset classifier for decoding. */
	public CRolesetClassifier(ZipInputStream in)
	{
		super(in);
	}
	
	@Override @SuppressWarnings("unchecked")
	protected void initLexia(Object[] lexica)
	{
		m_rolesets = (Map<String,String>)lexica[LEXICA_ROLESETS];
		m_lemmas   = (ObjectIntOpenHashMap<String>)lexica[LEXICA_LEMMAS];
	}
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	@Override
	public void loadModels(ZipInputStream zin)
	{
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
					loadFeatureTemplates(zin, Integer.parseInt(entry.substring(ENTRY_FEATURE.length())));
				else if (entry.equals(ENTRY_LEXICA))
					loadLexica(zin);
				else if (entry.startsWith(ENTRY_MODEL))
					loadStatisticalModels(zin, Integer.parseInt(entry.substring(ENTRY_MODEL.length())));
				else if (entry.startsWith(ENTRY_WEIGHTS))
					loadWeightVector(zin, Integer.parseInt(entry.substring(ENTRY_WEIGHTS.length())));
			}		
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void loadLexica(ZipInputStream zin) throws Exception
	{
		BufferedReader fin = new BufferedReader(new InputStreamReader(zin));
		LOG.info("Loading lexica.\n");
		
		m_rolesets = UTInput.getStringMap(fin, " ");
		m_lemmas   = UTInput.getStringIntOpenHashMap(fin, " ");
	}

	@Override
	public void saveModels(ZipOutputStream zout)
	{
		try
		{
			saveDefaultConfiguration(zout, ENTRY_CONFIGURATION);
			saveFeatureTemplates    (zout, ENTRY_FEATURE);
			saveLexica              (zout);
			saveStatisticalModels   (zout, ENTRY_MODEL);
			saveWeightVector        (zout, ENTRY_WEIGHTS);
			zout.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void saveLexica(ZipOutputStream zout) throws Exception
	{
		zout.putNextEntry(new ZipEntry(ENTRY_LEXICA));
		PrintStream fout = UTOutput.createPrintBufferedStream(zout);
		LOG.info("Saving lexica.\n");
		
		UTOutput.printMap(fout, m_rolesets, " ");	fout.flush();
		UTOutput.printMap(fout, m_lemmas, " ");		fout.flush();
		
		zout.closeEntry();
	}
	
//	====================================== GETTERS AND SETTERS ======================================

	@Override
	public Object[] getLexica()
	{
		Object[] lexica = new Object[2];
		Map<String,String> mRolesets = getRolesetMap();
		
		lexica[LEXICA_ROLESETS] = mRolesets;
		lexica[LEXICA_LEMMAS]   = getLemmas(m_collect.keySet(), mRolesets);
		
		return lexica;
	}
	
	private Map<String,String> getRolesetMap()
	{
		Map<String,String> map = new HashMap<String,String>();
		Set<String> set;
		
		for (String lemma : m_collect.keySet())
		{
			set = m_collect.get(lemma);
			
			if (set.size() == 1)
				map.put(lemma, new ArrayList<String>(set).get(0));
		}
		
		return map;
	}
	
	private ObjectIntOpenHashMap<String> getLemmas(Set<String> sLemmas, Map<String,String> mRolesets)
	{
		ObjectIntOpenHashMap<String> map = new ObjectIntOpenHashMap<String>();
		int idx = 0;
		
		for (String lemma : sLemmas)
		{
			if (!mRolesets.containsKey(lemma))
				map.put(lemma, idx++);
		}
		
		return map;
	}
	
	@Override
	public void countAccuracy(int[] counts)
	{
		int i, correct = 0, total = 0;
		String gRoleset;
		DEPNode node;
		
		for (i=1; i<t_size; i++)
		{
			node = d_tree.get(i);
			gRoleset = g_rolesets[i];
			
			if (gRoleset != null)
			{
				total++;
				
				if (gRoleset.equals(node.getFeat(DEPLib.FEAT_PB)))
					correct++;
			}
		}
		
		counts[0] += total;
		counts[1] += correct;
	}
	
//	====================================== PROCESS ======================================
	
	@Override
	public void process(DEPTree tree)
	{
		init(tree);
		processAux();
	}
	
	/** Called by {@link CRolesetClassifier#process(DEPTree)}. */
	protected void init(DEPTree tree)
	{
	 	d_tree = tree;
	 	t_size = tree.size();

	 	if (i_flag != FLAG_DECODE)
	 		g_rolesets = d_tree.getRolesetIDs();
	 	
	 	tree.setDependents();
	}
	
	/** Called by {@link CRolesetClassifier#process(DEPTree)}. */
	protected void processAux()
	{
		if (i_flag == FLAG_LEXICA)	addLexica();
		else						classify();
	}
	
	protected void addLexica()
	{
		String roleset, lemma;
		Set<String> set;
		
		for (i_pred=1; i_pred<t_size; i_pred++)
		{
			roleset = g_rolesets[i_pred];
			lemma   = d_tree.get(i_pred).lemma;
			
			if (roleset != null)
			{
				set = m_collect.get(lemma);
				
				if (set == null)
				{
					set = new HashSet<String>();
					m_collect.put(lemma, set);
				}
				
				set.add(roleset);
			}
		}
	}
	
	/** Called by {@link CRolesetClassifier#processAux()}. */
	protected void classify()
	{
		DEPNode pred;
		String  roleset;
		
		for (i_pred=1; i_pred<t_size; i_pred++)
		{
			pred = d_tree.get(i_pred);
			
			if (pred.getFeat(DEPLib.FEAT_PB) != null)
			{
				if ((roleset = m_rolesets.get(pred.lemma)) == null)
				{
					if (m_lemmas.containsKey(pred.lemma))
						roleset = getLabel(m_lemmas.get(pred.lemma));
					else
						roleset = pred.lemma+".01";
				}
				
				pred.addFeat(DEPLib.FEAT_PB, roleset);				
			}
		}
	}
	
	/** Called by {@link CRolesetClassifier#classify()}. */
	protected String getLabel(int modelId)
 	 {
		StringFeatureVector vector = getFeatureVector(f_xmls[0]);
		String label = null;
		
		if (i_flag == FLAG_TRAIN)
		{
			label = getGoldLabel();
			s_spaces[modelId].addInstance(label, vector);
		}
		else if (i_flag == FLAG_DECODE || i_flag == FLAG_DEVELOP)
		{
			label = getAutoLabel(vector, modelId);
		}
		
		return label;
	}
	
	/** Called by {@link CRolesetClassifier#getLabel()}. */
	private String getGoldLabel()
	{
		return g_rolesets[i_pred];
	}
	
	/** Called by {@link CRolesetClassifier#getLabel()}. */
	private String getAutoLabel(StringFeatureVector vector, int modelId)
	{
		StringPrediction p = s_models[modelId].predictBest(vector);
		return p.label;
	}

//	====================================== FEATURE EXTRACTION ======================================

	@Override
	protected String getField(FtrToken token)
	{
		DEPNode node = getNode(token);
		if (node == null)	return null;
		Matcher m;
		
		if (token.isField(JointFtrXml.F_FORM))
		{
			return node.form;
		}
		else if (token.isField(JointFtrXml.F_LEMMA))
		{
			return node.lemma;
		}
		else if (token.isField(JointFtrXml.F_POS))
		{
			return node.pos;
		}
		else if (token.isField(JointFtrXml.F_DEPREL))
		{
			return node.getLabel();
		}
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
		{
			return node.getFeat(m.group(1));
		}
		
		return null;
	}
	
	@Override
	protected String[] getFields(FtrToken token)
	{
		DEPNode node = getNode(token);
		if (node == null)	return null;
		
		if (token.isField(JointFtrXml.F_DEPREL_SET))
		{
			return getDeprelSet(node.getDependents());
		}
		
		return null;
	}
	
//	====================================== NODE GETTER ======================================
	
	protected DEPNode getNode(FtrToken token)
	{
		DEPNode node = getNodeAux(token);
		if (node == null)	return null;
		
		if (token.relation != null)
		{
			     if (token.isRelation(JointFtrXml.R_H))	node = node.getHead();
			else if (token.isRelation(JointFtrXml.R_LMD))	node = node.getLeftMostDependent();
			else if (token.isRelation(JointFtrXml.R_RMD))	node = node.getRightMostDependent();
			else if (token.isRelation(JointFtrXml.R_LND))	node = node.getLeftNearestDependent();
			else if (token.isRelation(JointFtrXml.R_RND))	node = node.getRightNearestDependent();
		}
		
		return node;
	}
	
	/** Called by {@link PredIdentifier#getNode(FtrToken)}. */
	private DEPNode getNodeAux(FtrToken token)
	{
		if (token.offset == 0)
			return d_tree.get(i_pred);
		
		int cIndex = i_pred + token.offset;
		
		if (0 < cIndex && cIndex < d_tree.size())
			return d_tree.get(cIndex);
		
		return null;
	}
}