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
package com.googlecode.clearnlp.generation;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.googlecode.clearnlp.component.morph.EnglishMPAnalyzer;
import com.googlecode.clearnlp.constant.universal.STPunct;
import com.googlecode.clearnlp.constant.universal.STConstant;
import com.googlecode.clearnlp.morphology.MPLibEn;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGVerbEn
{
	final Pattern TENSE_MULTI   = Pattern.compile("\\"+STPunct.PIPE);
	final Pattern TENSE_BETWEEN = Pattern.compile(STConstant.TAB);
	final String  VERB_TENSE    = EnglishMPAnalyzer.LANG_DIR+"verb.tense";
	
	private Map<String,String> m_vbd, m_vbn;
	
	public LGVerbEn(ZipInputStream inputStream)
	{
		try
		{
			init(inputStream);
		}
		catch (IOException e) {e.printStackTrace();}
	}
	
	public void init(ZipInputStream inputStream) throws IOException
	{
		ZipEntry zEntry;
		String filename;
		
		while ((zEntry = inputStream.getNextEntry()) != null)
		{
			filename = zEntry.getName();
			
			if (filename.equals(VERB_TENSE))
			{
				initTenseMap(inputStream);
				break;
			}
		}
		
		inputStream.close();
	}
	
	private void initTenseMap(ZipInputStream inputStream) throws IOException
	{
		BufferedReader fin = new BufferedReader(new InputStreamReader(inputStream));
		String line, base, past, part;
		String[] t;
		
		m_vbd = new HashMap<String,String>();
		m_vbn = new HashMap<String,String>();
		
		while ((line = fin.readLine()) != null)
		{
			t = TENSE_BETWEEN.split(line);
			base = t[0];
			past = t[1];
			part = t[2];
			
			m_vbd.put(base, past);
			m_vbn.put(base, part);
		}
	}
	
	public String getPastForm(String baseForm)
	{
		return getPastFormAux(baseForm, m_vbd);
	}
	
	public String getPastParticipleForm(String baseForm)
	{
		return getPastFormAux(baseForm, m_vbn);
	}
	
	private String getPastFormAux(String baseForm, Map<String,String> map)
	{
		String past = map.get(baseForm);
		return (past != null) ? past : getPastRegularForm(baseForm);
	}
	
	static public String getPastRegularForm(String baseForm)
	{
		if (baseForm.endsWith("e"))
			return baseForm+"d";
		
		if (baseForm.endsWith("y"))
		{
			int len = baseForm.length();
			
			if (len-2 >= 0 && MPLibEn.isVowel(baseForm.charAt(len-2)))
				return baseForm+"ed";
			else
				return baseForm.substring(0, len-1)+"ied";
		}
		
		return baseForm+"ed";
	}
	
	static public String get3rdSingularForm(String baseForm)
	{
		if (baseForm.equals("be"))
			return "is";
		
		if (baseForm.equals("have"))
			return "has";
		
		if (baseForm.endsWith("y"))
		{
			int len = baseForm.length();
			
			if (len-2 >= 0 && MPLibEn.isVowel(baseForm.charAt(len-2)))
				return baseForm+"s";
			else
				return baseForm.substring(0, len-1)+"ies";
		}
		
		if (baseForm.endsWith("ch") || baseForm.endsWith("sh") || baseForm.endsWith("s") || baseForm.endsWith("z") || baseForm.endsWith("x") || baseForm.endsWith("o"))
			return baseForm+"es"; 
		
		return baseForm+"s";
	}

	public static void main(String[] args) throws FileNotFoundException
	{
		LGVerbEn verb = new LGVerbEn(new ZipInputStream(new BufferedInputStream(new FileInputStream(args[0]))));
		
		System.out.println(verb.getPastForm("foreshow"));
		System.out.println(verb.getPastParticipleForm("foreshow"));
		
		System.out.println(verb.getPastForm("hope"));
		System.out.println(verb.getPastParticipleForm("hope"));
		
		System.out.println(verb.getPastForm("go"));
		System.out.println(verb.getPastParticipleForm("go"));
	}
}
