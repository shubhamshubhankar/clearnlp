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
package com.googlecode.clearnlp.generation;

import com.googlecode.clearnlp.constituent.CTLibEn;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.morphology.MPLibEn;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGLibEn
{
	/** 
	 * Converts all forms of "you" to "I" and vice versa.
	 * PRE: {@link DEPTree#setDependents()} is called.
	 */
	static public void convertUnI(DEPTree tree)
	{
		int i, size = tree.size();
		DEPNode node, head;
		String lower;
		
		for (i=1; i<size; i++)
		{
			node  = tree.get(i);
			head  = node.getHead();
			lower = node.form.toLowerCase();
			
			if (node.form.equals("I"))
			{
				node.form = "you";
				
				if (head.form.equalsIgnoreCase("am"))
					head.form = "are";
				else if (head.form.equalsIgnoreCase("was"))
					head.form = "were";
			}
			else if (lower.equals("you"))
			{
				if (node.isLabel(DEPLibEn.P_SBJ) || (node.isLabel(DEPLibEn.DEP_CONJ) && head.isLabel(DEPLibEn.P_SBJ)))
				{
					node.form = "I";
					
					if (!node.isLabel(DEPLibEn.DEP_CONJ) && !node.containsDependent(DEPLibEn.DEP_CONJ))
					{
						if (head.form.equalsIgnoreCase("are"))
							head.form = "am";
						else if (head.form.equalsIgnoreCase("were"))
							head.form = "was";						
					}
				}
				else
					node.form = "me";
			}
			else if (lower.equals("my"))		node.form = "your";
			else if (lower.equals("me"))		node.form = "you";
			else if (lower.equals("mine"))		node.form = "yours";
			else if (lower.equals("your"))		node.form = "my";
			else if (lower.equals("yours"))		node.form = "mine";
		}
	}
	
	static public String getReferentValueOf3rdPronoun(DEPNode node)
	{
		String coref = node.getFeat(DEPLibEn.FEAT_COREF);
		if (coref == null)	return null;
		
		if (node.isLemma("his") || node.isLemma("hers") || node.isLemma("its") || node.lemma.startsWith("our") || node.lemma.startsWith("your") || node.lemma.startsWith("their"))
			return getPossessiveForm(coref);
		
		if (node.isLemma("he") || node.isLemma("him") || node.isLemma("she") || node.isLemma("it") || node.isLemma("we") || node.isLemma("us") || node.isLemma("they") || node.isLemma("them"))
			return coref;
		
		if (node.isLemma("her"))
			return node.isPos(CTLibEn.POS_PRPS) ? getPossessiveForm(coref) : coref;
		
		return null;
	}
	
	static public String getPossessiveForm(String form)
	{
		String suffix = form.endsWith("s") ? "'" : "'s";
		return form + suffix;
	}
	
	/** PRE: {@link DEPTree#setDependents()} is called. */
	static public String getForms(DEPNode root, String delim)
	{
		StringBuilder build = new StringBuilder();
		String s;
		
		getSubFormsAux(build, root, delim);
		s = build.toString();
		
		if (s.startsWith(delim))
			s = s.substring(delim.length());
		
		return s;
	}
	
	/** Called by {@link LGLibEn#getForms(DEPNode, String)}. */
	static private void getSubFormsAux(StringBuilder build, DEPNode node, String delim)
	{
		boolean notAdded = true;
		DEPNode dep;
		
		for (DEPArc arc : node.getDependents())
		{
			dep = arc.getNode();
			
			if (notAdded && dep.id > node.id)
			{
				addForm(build, node, delim);
				notAdded = false;
			}
			
			if (dep.getDependents().isEmpty())
				addForm(build, dep, delim);
			else
				getSubFormsAux(build, dep, delim);
		}
		
		if (notAdded)
			addForm(build, node, delim);
	}
	
	/** Called by {@link LGLibEn#getSubFormsAux(StringBuilder, DEPNode, String)}. */
	static private void addForm(StringBuilder build, DEPNode node, String delim)
	{
		if (!attachLeft(node))	build.append(delim);
		String coref = getReferentValueOf3rdPronoun(node);
		
		if (coref != null)	build.append(coref);
		else				build.append(node.form);
	}
	
	/** Called by {@link DEPNode#getSubFormsAux(DEPNode, String, StringBuilder)} */
	static private boolean attachLeft(DEPNode node)
	{
		if (node.form.startsWith("'"))
		{
			if (node.isLabel(DEPLibEn.DEP_POSSESSIVE))
				return true;
			
			if (MPLibEn.isVerb(node.pos) || node.isPos(CTLibEn.POS_MD) || node.isLabel(DEPLibEn.DEP_NEG))
				return true;
		}
		else if (node.form.equalsIgnoreCase("n't"))
			return true;
		else if (node.isLabel(DEPLibEn.DEP_HYPH) || node.isLabel(DEPLibEn.DEP_HMOD) || node.isPos(CTLibEn.POS_HYPH))
			return true;
		else if (node.isPos(CTLibEn.POS_COLON) || node.isPos(CTLibEn.POS_COMMA) || node.isPos(CTLibEn.POS_PERIOD))
			return true;
		
		return false;
	}
}
