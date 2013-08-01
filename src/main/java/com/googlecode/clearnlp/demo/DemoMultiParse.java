/**
* Copyright 2012-2013 University of Massachusetts Amherst
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
package com.googlecode.clearnlp.demo;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import com.googlecode.clearnlp.component.AbstractComponent;
import com.googlecode.clearnlp.component.dep.AbstractDEPParser;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.engine.EngineGetter;
import com.googlecode.clearnlp.nlp.NLPDecode;
import com.googlecode.clearnlp.nlp.NLPLib;
import com.googlecode.clearnlp.reader.AbstractReader;
import com.googlecode.clearnlp.segmentation.AbstractSegmenter;
import com.googlecode.clearnlp.tokenization.AbstractTokenizer;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;
import com.googlecode.clearnlp.util.pair.ObjectDoublePair;

/**
 * @since 1.1.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DemoMultiParse
{
	final String language = AbstractReader.LANG_EN;
	
	public DemoMultiParse(String dictFile, String posModelFile, String depModelFile, String predModelFile, String roleModelFile, String srlModelFile, String inputFile, String outputFile) throws Exception
	{
		AbstractTokenizer tokenizer  = EngineGetter.getTokenizer(language, new FileInputStream(dictFile));
		AbstractComponent tagger     = EngineGetter.getComponent(new FileInputStream(posModelFile) , language, NLPLib.MODE_POS);
		AbstractComponent analyzer   = EngineGetter.getComponent(new FileInputStream(dictFile)     , language, NLPLib.MODE_MORPH);
		AbstractDEPParser parser     = EngineGetter.getDEPParser(new ZipInputStream(new FileInputStream(depModelFile)), language);
		AbstractComponent identifier = EngineGetter.getComponent(new FileInputStream(predModelFile), language, NLPLib.MODE_PRED);
		AbstractComponent classifier = EngineGetter.getComponent(new FileInputStream(roleModelFile), language, NLPLib.MODE_ROLE);
		AbstractComponent labeler    = EngineGetter.getComponent(new FileInputStream(srlModelFile) , language, NLPLib.MODE_SRL);
		
		AbstractComponent[] preComponents  = {tagger, analyzer};	// components used before parsing
		AbstractComponent[] postComponents = {identifier, classifier, labeler};	// components used after parsing
		
		String sentence = "I'd like to meet Dr. Choi.";
		process(tokenizer, parser, preComponents, postComponents, sentence);
		process(tokenizer, parser, preComponents, postComponents, UTInput.createBufferedFileReader(inputFile), UTOutput.createPrintBufferedFileStream(outputFile));
	}
	
	public void process(AbstractTokenizer tokenizer, AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, String sentence)
	{
		DEPTree tree = NLPDecode.toDEPTree(tokenizer.getTokens(sentence));
		List<ObjectDoublePair<DEPTree>> trees = getParses(parser, preComponents, postComponents, tree);
		
		for (ObjectDoublePair<DEPTree> p : trees)
		{
			tree = (DEPTree)p.o;
			System.out.println("Score: "+p.d);
			System.out.println(tree.toStringSRL()+"\n");
		}
	}
	
	public void process(AbstractTokenizer tokenizer, AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, BufferedReader reader, PrintStream fout)
	{
		AbstractSegmenter segmenter = EngineGetter.getSegmenter(language, tokenizer);
		List<ObjectDoublePair<DEPTree>> trees;
		DEPTree tree;
		
		for (List<String> tokens : segmenter.getSentences(reader))
		{
			tree  = NLPDecode.toDEPTree(tokens);
			trees = getParses(parser, preComponents, postComponents, tree);
			
			for (ObjectDoublePair<DEPTree> p : trees)
			{
				tree = (DEPTree)p.o;
				fout.println("Score: "+p.d);
				fout.println(tree.toStringSRL()+"\n");
			}
		}
		
		fout.close();
	}
	
	private List<ObjectDoublePair<DEPTree>> getParses(AbstractDEPParser parser, AbstractComponent[] preComponents, AbstractComponent[] postComponents, DEPTree tree)
	{
		List<ObjectDoublePair<DEPTree>> trees;
		boolean uniqueOnly = true;	// return only unique trees given a sentence 
		
		for (AbstractComponent component : preComponents)
			component.process(tree);
		
		parser.process(tree);
		trees = parser.getParsedTrees(uniqueOnly);	// parses are already sorted by their scores in descending order
		
		for (ObjectDoublePair<DEPTree> p : trees)
		{
			tree = (DEPTree)p.o;
			
			for (AbstractComponent component : postComponents)
				component.process(tree);
		}
		
		return trees;
	}

	public static void main(String[] args)
	{
		String dictFile      = args[0];	// e.g., dictionary.zip
		String posModelFile  = args[1];	// e.g., ontonotes-en-pos.tgz
		String depModelFile  = args[2];	// e.g., ontonotes-en-dep.tgz
		String predModelFile = args[3];	// e.g., ontonotes-en-pred.tgz
		String roleModelFile = args[4];	// e.g., ontonotes-en-role.tgz
		String srlModelFile  = args[5];	// e.g., ontonotes-en-srl.tgz
		String inputFile     = args[6];
		String outputFile    = args[7];

		try
		{
			new DemoMultiParse(dictFile, posModelFile, depModelFile, predModelFile, roleModelFile, srlModelFile, inputFile, outputFile);
		}
		catch (Exception e) {e.printStackTrace();}
	}
}
