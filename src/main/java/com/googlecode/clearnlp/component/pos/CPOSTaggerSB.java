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

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.googlecode.clearnlp.classification.algorithm.AbstractAlgorithm;
import com.googlecode.clearnlp.classification.feature.FtrToken;
import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.classification.prediction.StringPrediction;
import com.googlecode.clearnlp.classification.train.StringTrainSpace;
import com.googlecode.clearnlp.classification.vector.StringFeatureVector;
import com.googlecode.clearnlp.component.AbstractStatisticalComponent;
import com.googlecode.clearnlp.component.AbstractStatisticalComponentSB;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineProcess;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.pos.POSState;
import com.googlecode.clearnlp.reader.AbstractColumnReader;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;
import com.googlecode.clearnlp.util.UTString;
import com.googlecode.clearnlp.util.map.Prob2DMap;
import com.googlecode.clearnlp.util.pair.ObjectDoublePair;
import com.googlecode.clearnlp.util.pair.Pair;
import com.googlecode.clearnlp.util.pair.StringDoublePair;
import com.googlecode.clearnlp.util.triple.Triple;

/**
 * Part-of-speech tagger using document frequency cutoffs.
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CPOSTaggerSB extends AbstractStatisticalComponentSB
{
	protected final String ENTRY_CONFIGURATION = NLPLib.MODE_POS + NLPLib.ENTRY_CONFIGURATION;
	protected final String ENTRY_FEATURE	   = NLPLib.MODE_POS + NLPLib.ENTRY_FEATURE;
	protected final String ENTRY_LEXICA		   = NLPLib.MODE_POS + NLPLib.ENTRY_LEXICA;
	protected final String ENTRY_MODEL		   = NLPLib.MODE_POS + NLPLib.ENTRY_MODEL;
	
	protected final int LEXICA_LOWER_SIMPLIFIED_FORMS = 0;
	protected final int LEXICA_AMBIGUITY_CLASSES      = 1;
	
	protected Set<String>			s_lsfs;
	protected Prob2DMap				p_ambi;		// only for collecting
	protected Map<String,String>	m_ambi;
	protected String[]          	g_tags;
	protected int 					i_input;
	
//	protected Map<String,Pair<StringPrediction,StringPrediction>> m_labels;
	
//	====================================== CONSTRUCTORS ======================================

	public CPOSTaggerSB() {}
	
	/** Constructs a part-of-speech tagger for collecting lexica. */
	public CPOSTaggerSB(JointFtrXml[] xmls, Set<String> sLsfs)
	{
		super(xmls);
		s_lsfs = sLsfs;
		p_ambi = new Prob2DMap();
	}
	
	/** Constructs a part-of-speech tagger for training. */
	public CPOSTaggerSB(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, lexica, margin, beams);
	}
	
	/** Constructs a part-of-speech tagger for developing. */
	public CPOSTaggerSB(JointFtrXml[] xmls, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, models, lexica, margin, beams);
	}
	
	/** Constructs a part-of-speech tagger for bootsrapping. */
	public CPOSTaggerSB(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, models, lexica, margin, beams);
	}
	
	/** Constructs a part-of-speech tagger for decoding. */
	public CPOSTaggerSB(ZipInputStream in)
	{
		super(in);
	}
	
	@Override @SuppressWarnings("unchecked")
	protected void initLexia(Object[] lexica)
	{
		s_lsfs = (Set<String>)lexica[LEXICA_LOWER_SIMPLIFIED_FORMS];
		m_ambi = (Map<String,String>)lexica[LEXICA_AMBIGUITY_CLASSES];
	}
	
//	====================================== LOAD/SAVE MODELS ======================================
	
	@Override
	public void loadModels(ZipInputStream zin)
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
					loadSBConfiguration(zin);
				else if (entry.startsWith(ENTRY_FEATURE))
					loadFeatureTemplates(zin, Integer.parseInt(entry.substring(fLen)));
				else if (entry.startsWith(ENTRY_MODEL))
					loadStatisticalModels(zin, Integer.parseInt(entry.substring(mLen)));
				else if (entry.equals(ENTRY_LEXICA))
					loadLexica(zin);
			}		
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	protected void loadLexica(ZipInputStream zin) throws Exception
	{
		BufferedReader fin = UTInput.createBufferedReader(zin);
		LOG.info("Loading lexica.\n");
		
		s_lsfs = UTInput.getStringSet(fin);
		m_ambi = UTInput.getStringMap(fin, " ");
	}

	@Override
	public void saveModels(ZipOutputStream zout)
	{
		try
		{
			saveSBConfiguration  (zout, ENTRY_CONFIGURATION);
			saveFeatureTemplates (zout, ENTRY_FEATURE);
			saveLexica           (zout);
			saveStatisticalModels(zout, ENTRY_MODEL);
			zout.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	protected void saveLexica(ZipOutputStream zout) throws Exception
	{
		zout.putNextEntry(new ZipEntry(ENTRY_LEXICA));
		PrintStream fout = UTOutput.createPrintBufferedStream(zout);
		LOG.info("Saving lexica.\n");
		
		UTOutput.printSet(fout, s_lsfs);		fout.flush();
		UTOutput.printMap(fout, m_ambi, " ");	fout.flush();
		
		zout.closeEntry();
	}
	
//	====================================== GETTERS AND SETTERS ======================================

	@Override
	public Object[] getLexica()
	{
		Object[] lexica = new Object[2];
		
		lexica[LEXICA_LOWER_SIMPLIFIED_FORMS] = s_lsfs;
		lexica[LEXICA_AMBIGUITY_CLASSES] = (i_flag == FLAG_LEXICA) ? getAmbiguityClasses() : m_ambi;
		
		return lexica;
	}
	
	/** {@link AbstractStatisticalComponent#FLAG_LEXICA}. */
	public Set<String> getLowerSimplifiedForms()
	{
		return s_lsfs;
	}
	
	/** {@link AbstractStatisticalComponent#FLAG_LEXICA}. */
	public void clearLowerSimplifiedForms()
	{
		s_lsfs.clear();
	}
	
	/** Called by {@link CPOSTaggerSB#getLexica()}. */
	private Map<String,String> getAmbiguityClasses()
	{
		double threshold = f_xmls[0].getAmbiguityClassThreshold();
		Map<String,String> mAmbi = new HashMap<String,String>();
		StringDoublePair[] ps;
		StringBuilder build;
		
		for (String key : p_ambi.keySet())
		{
			build = new StringBuilder();
			ps = p_ambi.getProb1D(key);
			Arrays.sort(ps);
			
			for (StringDoublePair p : ps)
			{
				if (p.d <= threshold)	break;
				
				build.append(AbstractColumnReader.BLANK_COLUMN);
				build.append(p.s);
			}
			
			if (build.length() > 0)
				mAmbi.put(key, build.substring(1));				
		}
		
		return mAmbi;
	}
	
	@Override
	public void countAccuracy(int[] counts)
	{
		int i, correct = 0;
		
		for (i=1; i<t_size; i++)
		{
			if (d_tree.get(i).pos.equals(g_tags[i]))
				correct++;
		}
		
		counts[0] += t_size - 1;
		counts[1] += correct;
	}
	
//	====================================== PROCESS ======================================
	
	@Override
	public void process(DEPTree tree)
	{
		init(tree);
		processAux();
	}
	
	/** Called by {@link CPOSTaggerSB#process(DEPTree)}. */
	protected void init(DEPTree tree)
	{
	 	d_tree  = tree;
	 	t_size  = tree.size();
	 	b_first = true;
	 	
	 //	m_labels = new HashMap<String, Pair<StringPrediction,StringPrediction>>();
	 	
	 	if (i_flag != FLAG_DECODE)
	 	{
	 		g_tags = tree.getPOSTags();
	 		tree.clearPOSTags();
	 	}
	 	
	 	initAux();
	 	EngineProcess.normalizeForms(tree);
	}
	
	protected void initAux()
	{
		i_input = 1;
		d_score = 0;
	}
	
	/** Called by {@link CPOSTaggerSB#process(DEPTree)}. */
	protected void processAux()
	{
		if (i_flag == FLAG_LEXICA)
			addLexica();
		else
		{
			List<Pair<String,StringFeatureVector>> insts = tag();
			
			if (i_flag == FLAG_TRAIN || i_flag == FLAG_BOOTSTRAP)
			{
				for (Pair<String,StringFeatureVector> inst : insts)
					s_spaces[0].addInstance(inst.o1, inst.o2);				
			}
		}
	}
	
	/** Called by {@link CPOSTaggerSB#processAux()}. */
	protected void addLexica()
	{
		DEPNode node;
		int i;

		for (i=1; i<t_size; i++)
		{
			node = d_tree.get(i);
			
			if (s_lsfs.contains(node.lowerSimplifiedForm))			
				p_ambi.add(node.simplifiedForm, g_tags[i]);
		}
	}
	
	/** Called by {@link CPOSTaggerSB#processAux()}. */
	protected List<Pair<String,StringFeatureVector>> tag()
	{
		return (i_flag == FLAG_TRAIN) ? tagMain().o2 : tagBranches();
	}
	
	protected Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>> tagMain()
	{
		List<Pair<String,StringFeatureVector>> insts = new ArrayList<Pair<String,StringFeatureVector>>();
		List<POSState> states = new ArrayList<POSState>();
		
		while (i_input < t_size)
			tagAux(getLabel(insts, states));
		
		if (states.size() > n_beams - 1)
		{
			Collections.sort(states);
			states = states.subList(0, n_beams - 1);
		}
		
		return new Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>(d_tree.getPOSTags(), insts, states);
	}
	
	private void tagAux(StringPrediction p)
	{
		d_tree.get(i_input).pos = p.label;
		d_score += p.score;
		i_input++;
	}
	
	/** Called by {@link CPOSTaggerSB#tag()}. */
	private StringPrediction getLabel(List<Pair<String,StringFeatureVector>> insts, List<POSState> states)
	{
		StringFeatureVector vector = getFeatureVector(f_xmls[0]);
		StringPrediction label = null;
		
		if (i_flag == FLAG_TRAIN)
		{
			label = getGoldLabel();
			if (vector.size() > 0)	insts.add(new Pair<String,StringFeatureVector>(label.label, vector));
		}
		else if (i_flag == FLAG_DECODE || i_flag == FLAG_DEVELOP)
		{
			label = getAutoLabel(vector, states);
		}
		else if (i_flag == FLAG_BOOTSTRAP)
		{
			label = getAutoLabel(vector, states);
			if (vector.size() > 0)	insts.add(new Pair<String,StringFeatureVector>(getGoldLabel().label, vector));
		}
		
		return label;
	}
	
	/** Called by {@link CPOSTaggerSB#getLabel()}. */
	private StringPrediction getGoldLabel()
	{
		return new StringPrediction(g_tags[i_input], 1);
	}
	
	/** Called by {@link CPOSTaggerSB#getLabel()}. */
	private StringPrediction getAutoLabel(StringFeatureVector vector, List<POSState> states)
	{
		List<StringPrediction> ps = s_models[0].predictAll(vector);
		AbstractAlgorithm.normalize(ps);
		
		StringPrediction fst = ps.get(0);
		StringPrediction snd = ps.get(1);
		
		if (b_first && n_beams > 1 && fst.score - snd.score < d_margin)
			states.add(new POSState(i_input, d_score, snd));

		return fst;
	}

//	====================================== FEATURE EXTRACTION ======================================

	@Override
	protected String getField(FtrToken token)
	{
		DEPNode node = getNode(token);
		if (node == null)	return null;
		Matcher m;
		
		if (token.isField(JointFtrXml.F_SIMPLIFIED_FORM))
		{
			return (s_lsfs.contains(node.lowerSimplifiedForm)) ? node.simplifiedForm : null;
		}
		else if (token.isField(JointFtrXml.F_LOWER_SIMPLIFIED_FORM))
		{
			return (s_lsfs.contains(node.lowerSimplifiedForm)) ? node.lowerSimplifiedForm : null;
		}
		else if (token.isField(JointFtrXml.F_POS))
		{
			return node.pos;
		}
		else if (token.isField(JointFtrXml.F_AMBIGUITY_CLASS))
		{
			return m_ambi.get(node.simplifiedForm);
		}
		else if ((m = JointFtrXml.P_BOOLEAN.matcher(token.field)).find())
		{
			int field = Integer.parseInt(m.group(1));
			
			switch (field)
			{
			case  0: return UTString.isAllUpperCase(node.simplifiedForm) ? token.field : null;
			case  1: return UTString.isAllLowerCase(node.simplifiedForm) ? token.field : null;
			case  2: return UTString.beginsWithUpperCase(node.simplifiedForm) & (i_input != 1) ? token.field : null;
			case  3: return UTString.getNumOfCapitalsNotAtBeginning(node.simplifiedForm) == 1 ? token.field : null;
			case  4: return UTString.getNumOfCapitalsNotAtBeginning(node.simplifiedForm)  > 1 ? token.field : null;
			case  5: return node.simplifiedForm.contains(".") ? token.field : null;
			case  6: return UTString.containsDigit(node.simplifiedForm) ? token.field : null;
			case  7: return node.simplifiedForm.contains("-") ? token.field : null;
			case  8: return (i_input == t_size-1) ? token.field : null;
			case  9: return (i_input == 1) ? token.field : null;
			default: throw new IllegalArgumentException("Unsupported feature: "+field);
			}
		}
		else if ((m = JointFtrXml.P_FEAT.matcher(token.field)).find())
		{
			return node.getFeat(m.group(1));
		}
		else if ((m = JointFtrXml.P_PREFIX.matcher(token.field)).find())
		{
			int n = Integer.parseInt(m.group(1)), len = node.lowerSimplifiedForm.length();
			return (n <= len) ? node.lowerSimplifiedForm.substring(0, n) : null;
		}
		else if ((m = JointFtrXml.P_SUFFIX.matcher(token.field)).find())
		{
			int n = Integer.parseInt(m.group(1)), len = node.lowerSimplifiedForm.length();
			return (n <= len) ? node.lowerSimplifiedForm.substring(len-n, len) : null;
		}
		
		return null;
	}
	
	@Override
	protected String[] getFields(FtrToken token)
	{
		DEPNode node = getNode(token);
		if (node == null)	return null;
		Matcher m;
		
		if ((m = JointFtrXml.P_PREFIX.matcher(token.field)).find())
		{
			String[] fields = UTString.getPrefixes(node.lowerSimplifiedForm, Integer.parseInt(m.group(1)));
			return fields.length == 0 ? null : fields;
		}
		else if ((m = JointFtrXml.P_SUFFIX.matcher(token.field)).find())
		{
			String[] fields = UTString.getSuffixes(node.lowerSimplifiedForm, Integer.parseInt(m.group(1)));
			return fields.length == 0 ? null : fields;
		}
		
		return null;
	}
	
//	====================================== NODE GETTER ======================================
	
	protected DEPNode getNode(FtrToken token)
	{
		int index = i_input + token.offset;
		return (0 < index && index < t_size) ? d_tree.get(index) : null;
	}
	
//	================================ SELECTIONAL BRANCHING ================================
	
	@SuppressWarnings("unchecked")
	protected List<Pair<String,StringFeatureVector>> tagBranches()
	{
		List<ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>>> list;
		Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>> t0 = tagMain();
		
		if (t0.o3.isEmpty())
			return (i_flag == FLAG_DECODE || i_flag == FLAG_DEVELOP) ? null : t0.o2;
		
		Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>> tm;
		b_first = false;
		
		list = new ArrayList<ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>>>();
		list.add(new ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>>(t0, d_score));
		branch(list, t0.o3);
		
		if (i_flag == FLAG_DECODE || i_flag == FLAG_DEVELOP)
		{
			tm = (Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>)getMax(list).o;
			d_tree.resetPOSTags(tm.o1);
			return null;
		}
		else
		{
			List<Pair<String,StringFeatureVector>> insts = new ArrayList<Pair<String,StringFeatureVector>>(t0.o2);
			setGoldScores(list);
			
			tm = (Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>)getMax(list).o;
			insts.addAll(tm.o2);
			
			return insts;
		}
	}
	
	private void branch(List<ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>>> list, List<POSState> states)
	{
		Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>> t1;
		double s1;
		
		for (POSState state : states)
		{
			resetState(state);
			t1 = tagMain();
			s1 = d_score;
			list.add(new ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>>(t1, s1));
		}
	}
	
	private void resetState(POSState state)
	{
		i_input = state.input;
		d_score = state.score;
		tagAux(state.label);
		
		int i; for (i=i_input+1; i<t_size; i++)
			d_tree.get(i).pos = null;
	}
	
	private ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>> getMax(List<ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>>> list)
	{
		ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>> max = list.get(0), t;
		int i, size = list.size();
		
		for (i=1; i<size; i++)
		{
			t = list.get(i);
			if (max.d < t.d) max = t;
		}
		
		return max;
	}
	
	@SuppressWarnings("unchecked")
	private void setGoldScores(List<ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>>> list)
	{
		String[] tags;
		int i, c;
		
		for (ObjectDoublePair<Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>> p : list)
		{
			tags = ((Triple<String[],List<Pair<String,StringFeatureVector>>,List<POSState>>)p.o).o1;
			
			for (i=1,c=0; i<t_size; i++)
			{
				if (g_tags[i].equals(tags[i]))
					c++;
			}
			
			p.d = c;
		}
	}
}
