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
package com.googlecode.clearnlp.experiment;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.clearnlp.dependency.DEPLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.generation.LGAsk;
import com.googlecode.clearnlp.morphology.MPLibEn;
import com.googlecode.clearnlp.reader.JointReader;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class GenerateSenTypes
{
	private List<DEPTree> ints;
	private List<DEPTree> imps;	
	
	public GenerateSenTypes()
	{
		init();
	}
	
	public void init()
	{
		ints = new ArrayList<DEPTree>();
		imps = new ArrayList<DEPTree>();
	}
	
	public void addInterrogativeOrImperative(String filename)
	{
		JointReader fin = new JointReader(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1);
		DEPTree tree;
		
		fin.open(UTInput.createBufferedFileReader(filename));

		while ((tree = fin.next()) != null)
			addInterrogativeOrImperative(tree);
	}

	public void addInterrogativeOrImperative(DEPTree tree)
	{
		int i, size = tree.size();
		DEPTree clone;
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			if (MPLibEn.isVerb(node.pos) && (node.isLabel(DEPLibEn.DEP_CCOMP) || node.isLabel(DEPLibEn.DEP_XCOMP)))
			{
				clone = tree.clone();
				clone.setDependents();
				clone = LGAsk.generateInterrogativeOrImperative(clone.get(i));
				
				if (clone != null)
				{
					if (node.isLabel(DEPLibEn.DEP_CCOMP))	ints.add(clone);
					else									imps.add(clone);
				}
			}
		}
	}
	
	public List<DEPTree> getInterrogativeTrees()
	{
		return ints;
	}
	
	public List<DEPTree> getImperativeTrees()
	{
		return imps;
	}

	static public void main(String[] args)
	{
		JointReader fin = new JointReader(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, -1);
		GenerateSenTypes gs = new GenerateSenTypes();
		String filename = args[0];

		gs.addInterrogativeOrImperative(filename);
		
		fin.open(UTInput.createBufferedFileReader(filename));
		PrintStream fint = UTOutput.createPrintBufferedFileStream(filename+".int");
		PrintStream fimp = UTOutput.createPrintBufferedFileStream(filename+".imp");
		
		for (DEPTree tree : gs.getInterrogativeTrees())
			fint.println(tree.toString()+"\n");
		
		fint.close();
		
		for (DEPTree tree : gs.getImperativeTrees())
			fimp.println(tree.toString()+"\n");
		
		fimp.close();
	}
}
