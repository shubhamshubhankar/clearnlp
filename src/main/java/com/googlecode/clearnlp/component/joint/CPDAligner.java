/**
* Copyright 2013 IPSoft Inc.
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
*   
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.googlecode.clearnlp.component.joint;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.morphology.MPLibEn;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.util.map.Prob2DMap;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class CPDAligner extends AbstractComponent
{
	protected final String ENTRY_CURR_MAP = NLPLib.MODE_PD_ALIGN + "_CURR";
	protected final String ENTRY_HEAD_MAP = NLPLib.MODE_PD_ALIGN + "_HEAD";
	
	private Prob2DMap p_curr;	// for training
	private Prob2DMap p_head;	// for training
	private Map<String,Object2DoubleOpenHashMap<String>> m_curr;	// for decoding
	private Map<String,Object2DoubleOpenHashMap<String>> m_head;	// for decoding

	/** Initializes a matcher for training. */
	public CPDAligner()
	{
		i_flag = FLAG_TRAIN;
		p_curr = new Prob2DMap();
		p_head = new Prob2DMap();
	}
	
	public CPDAligner(ZipInputStream zin)
	{
		i_flag = FLAG_DECODE;
		
		try
		{
			loadModels(zin);
		}
		catch (Exception e) {e.printStackTrace();}
	}

	public void loadModels(ZipInputStream zin) throws Exception
	{
		ZipEntry zEntry;
		String   entry;
				
		try
		{
			while ((zEntry = zin.getNextEntry()) != null)
			{
				entry = zEntry.getName();
				
				if      (entry.equals(ENTRY_CURR_MAP))
					m_curr = getMap(zin);
				else if (entry.equals(ENTRY_HEAD_MAP))
					m_head = getMap(zin);;
			}		
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	@SuppressWarnings("unchecked")
	private Map<String,Object2DoubleOpenHashMap<String>> getMap(ZipInputStream zin) throws Exception
	{
		ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(zin));
		return (Map<String,Object2DoubleOpenHashMap<String>>)oin.readObject();
	}
	
	public void saveModels(ZipOutputStream zout, double threshold)
	{
		try
		{
			saveMap(zout, p_curr.getProb1DMap(threshold), ENTRY_CURR_MAP);
			saveMap(zout, p_head.getProb1DMap(threshold), ENTRY_HEAD_MAP);
			zout.close();
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void saveMap(ZipOutputStream zout, Map<String,Object2DoubleOpenHashMap<String>> map, String entryName) throws Exception
	{
		zout.putNextEntry(new ZipEntry(entryName));
		ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(zout));
		oout.writeObject(map);
		oout.flush();
		zout.closeEntry();	
	}
	
	@Override
	public void process(DEPTree tree)
	{
		int i, size = tree.size();
		String deprel;
		DEPNode curr;
		
		tree.setDependents();
		
		for (i=1; i<size; i++)
		{
			curr   = tree.get(i);
			deprel = curr.getLabel();
			
			if (i_flag == FLAG_TRAIN)
				train(curr, deprel);
			else
				decode(curr, deprel);
		}
	}
	
	private void train(DEPNode curr, String deprel)
	{
		p_curr.add(deprel, MPLibEn.toCPOSTag(curr.pos));
		p_head.add(deprel, MPLibEn.toCPOSTag(curr.getHead().pos));
	}
	
	private void decode(DEPNode curr, String deprel)
	{
		double d1, d2;
		String p2;
		
		if ((p2 = curr.getFeat(DEPLib.FEAT_POS2)) != null)
		{
			d1 = getScore(curr, deprel, MPLibEn.toCPOSTag(curr.pos));
			d2 = getScore(curr, deprel, MPLibEn.toCPOSTag(p2));
			
			if (d1 < d2)
			{
				curr.addFeat(DEPLib.FEAT_POS2, curr.pos);
				curr.pos = p2;
			}
		}
	}
	
	private double getScore(DEPNode curr, String deprel, String cpos)
	{
		Object2DoubleOpenHashMap<String> map;
		double score = 0;
		DEPNode dep;
		
		if ((map = m_curr.get(deprel)) != null)
			score += map.getDouble(cpos);
		
		for (DEPArc arc : curr.getDependents())
		{
			dep = arc.getNode();
			
			if ((map = m_head.get(dep.getLabel())) != null)
				score += map.getDouble(cpos);
		}
		
		return score;
	}
}
