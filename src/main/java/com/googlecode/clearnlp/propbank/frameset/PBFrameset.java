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
package com.googlecode.clearnlp.propbank.frameset;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.googlecode.clearnlp.io.FileExtFilter;
import com.googlecode.clearnlp.util.UTXml;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBFrameset implements Serializable
{
	private static final long serialVersionUID = 2617344226897601182L;
	private final Pattern P_VERB_PRT = Pattern.compile("_");
	
	private Map<String,Set<String>> m_verb_lemma;
	
	/**
	 * @param framesetDir a directory containing PropBank frameset files.
	 * @param ext the extension of frameset files (e.g., {@code "-v.xml"}).
	 */
	public PBFrameset(String framesetDir, String ext)
	{
		String[] filelist = new File(framesetDir).list(new FileExtFilter(ext));
		
		try
		{
			init();
			
			for (String framesetFile : filelist)
				addFrameset(new BufferedInputStream(new FileInputStream(framesetDir+"/"+framesetFile)));
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	/** @param in an input-stream from a PropBank frameset file. */
	public PBFrameset(InputStream in)
	{
		try
		{
			init();
			addFrameset(in);
		}
		catch (Exception e) {e.printStackTrace();}
	}
	
	private void init() throws ParserConfigurationException
	{
		m_verb_lemma = new HashMap<String,Set<String>>();
	}

	/** @param in an input-stream from a PropBank frameset file. */
	public void addFrameset(InputStream in) throws Exception
	{
		DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
		
		NodeList list = dBuilder.parse(in).getElementsByTagName(PBFLib.E_PREDICATE);
		int i, size = list.getLength();
		Element ePredicate;
		String lemma;
		
		for (i=0; i<size; i++)
		{
			ePredicate = (Element)list.item(i);
			lemma = UTXml.getTrimmedAttribute(ePredicate, PBFLib.A_LEMMA).toLowerCase();
			
			addLemma(lemma);
		}
	}
	
	/**
	 * Called by {@link PBFrameset#addFrameset(InputStream)}.
	 * @param lemma "run" or "run_out".
	 */
	private void addLemma(String lemma)
	{
		String[] t = P_VERB_PRT.split(lemma);
		Set<String> set;
		String vb;
		
		if (t.length > 2)
			System.err.println("Too many particles: "+lemma);
		else
		{
			vb  = t[0];
			set = m_verb_lemma.get(vb);
			
			if (set == null)
			{
				set = new HashSet<String>();
				m_verb_lemma.put(vb, set);
			}

			if (t.length > 1)
				set.add(t[1]);	// t[1] is a particle
		}
	}
	
	/** @param assumed to be in its base-form. */
	public boolean isVerb(String verb)
	{
		return m_verb_lemma.containsKey(verb);
	}
	
	/** Parameters are assumed to be in its base-form. */
	public boolean isVerbParticleConstruction(String verb, String particle)
	{
		Set<String> set = m_verb_lemma.get(verb);
		return (set != null) ? set.contains(particle) : false;
	}
}
