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
package com.googlecode.clearnlp.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.component.dep.AbstractDEPParser;
import com.googlecode.clearnlp.component.dep.DefaultDEPParser;
import com.googlecode.clearnlp.component.dep.EnglishDEPParser;
import com.googlecode.clearnlp.component.morph.AbstractMPAnalyzer;
import com.googlecode.clearnlp.component.morph.DefaultMPAnalyzer;
import com.googlecode.clearnlp.component.morph.EnglishMPAnalyzer;
import com.googlecode.clearnlp.component.pos.AbstractPOSTagger;
import com.googlecode.clearnlp.component.pos.DefaultPOSTagger;
import com.googlecode.clearnlp.component.pos.EnglishPOSTagger;
import com.googlecode.clearnlp.component.srl.AbstractSRLabeler;
import com.googlecode.clearnlp.component.srl.CPredIdentifier;
import com.googlecode.clearnlp.component.srl.CRolesetClassifier;
import com.googlecode.clearnlp.component.srl.CSenseClassifier;
import com.googlecode.clearnlp.component.srl.DefaultSRLabeler;
import com.googlecode.clearnlp.component.srl.EnglishSRLabeler;
import com.googlecode.clearnlp.conversion.AbstractC2DConverter;
import com.googlecode.clearnlp.conversion.EnglishC2DConverter;
import com.googlecode.clearnlp.headrule.HeadRuleMap;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.segmentation.EnglishSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import com.googlecode.clearnlp.tokenization.EnglishTokenizer;
import com.googlecode.clearnlp.util.UTInput;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EngineGetter implements EngineLib
{
	// ============================= getter: constituent-to-dependency converter =============================
	
	static public AbstractC2DConverter getC2DConverter(String language, String headruleFile, String mergeLabels)
	{
		HeadRuleMap headrules = new HeadRuleMap(UTInput.createBufferedFileReader(headruleFile));
		
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishC2DConverter(headrules, mergeLabels);
		
		throw new IllegalArgumentException("The requested language '"+language+"' is not currently supported.");
	}
	
	// ============================= getter: word tokenizer =============================
	
	static public AbstractTokenizer getTokenizer(String language, String dictFile)
	{
		AbstractTokenizer tokenizer = null;
		
		try
		{
			tokenizer = getTokenizer(language, new FileInputStream(dictFile));
		}
		catch (Exception e) {e.printStackTrace();}
		
		return tokenizer;
	}
	
	static public AbstractTokenizer getTokenizer(String language, InputStream stream)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishTokenizer(new ZipInputStream(stream));
		
		throw new IllegalArgumentException("The requested language '"+language+"' is not currently supported.");
	}
	
	// ============================= getter: sentence segmenter =============================
	
	static public AbstractSegmenter getSegmenter(String language, AbstractTokenizer tokenizer)
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishSegmenter(tokenizer);
		
		throw new IllegalArgumentException("The requested language '"+language+"' is not currently supported.");
	}
	
	// ============================= getter: component =============================
	
	static public AbstractComponent getComponent(InputStream stream, String language, String mode) throws IOException
	{
		ZipInputStream zin = new ZipInputStream(stream);
		
		if      (mode.equals(NLPLib.MODE_POS))
			return getPOSTagger(zin, language);
		else if (mode.equals(NLPLib.MODE_MORPH))
			return getMPAnalyzer(zin, language);
		else if (mode.equals(NLPLib.MODE_DEP))
			return getDEPParser(zin, language);
		else if (mode.equals(NLPLib.MODE_PRED))
			return new CPredIdentifier(zin);
		else if (mode.equals(NLPLib.MODE_ROLE))
			return new CRolesetClassifier(zin);
		else if (mode.startsWith(NLPLib.MODE_SENSE))
			return new CSenseClassifier(zin, mode.substring(mode.lastIndexOf("_")+1));
		else if (mode.equals(NLPLib.MODE_SRL))
			return getSRLabeler(zin, language);
		
		throw new IllegalArgumentException("The requested mode '"+mode+"' is not supported.");
	}
	
	static public AbstractPOSTagger getPOSTagger(ZipInputStream zin, String language) throws IOException
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishPOSTagger(zin);
		
		return new DefaultPOSTagger(zin);
	}
	
	static public AbstractDEPParser getDEPParser(ZipInputStream zin, String language) throws IOException
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishDEPParser(zin);
		
		return new DefaultDEPParser(zin);
	}
	
	static public AbstractMPAnalyzer getMPAnalyzer(ZipInputStream zin, String language) throws IOException
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishMPAnalyzer(zin);
		
		return new DefaultMPAnalyzer();
	}
	
	static public AbstractSRLabeler getSRLabeler(ZipInputStream zin, String language) throws IOException
	{
		if (language.equals(AbstractReader.LANG_EN))
			return new EnglishSRLabeler(zin);
		
		return new DefaultSRLabeler(zin);
	}
}
