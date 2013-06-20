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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.googlecode.clearnlp.constituent.CTLibEn;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPFeat;
import com.googlecode.clearnlp.dependency.DEPLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.morphology.MPLibEn;
import com.googlecode.clearnlp.reader.SRLReader;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTString;
import com.googlecode.clearnlp.util.pair.Pair;
import com.googlecode.clearnlp.util.pair.StringIntPair;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGAsk
{
	final String FEAT_INF = "inf";
	
	public String genarateQuestionFromAsk(DEPTree tree, String delim)
	{
		tree = tree.cloneSRL();
		tree.setDependents();
		
		DEPNode root = tree.getFirstRoot();
		if (root == null)	return null;
		Pair<DEPTree,DEPNode> p;
		DEPNode dep;
		
		for (DEPArc arc : root.getDependents())
		{
			dep = arc.getNode();
			
			if (MPLibEn.isVerb(dep.pos))
			{
				p = generateQuestion(dep);
				if (p.o2 != null) matchNumber(dep, p.o2);
				return generateQuestionForms(dep, delim);
			}
		}
		
		return null;
	}
	
	private void matchNumber(DEPNode verb, DEPNode aux)
	{
		for (DEPArc arc : verb.getDependents())
		{
			if (arc.isLabel(DEPLibEn.P_SBJ))
			{
				DEPNode dep = arc.getNode();
				
				if (dep.isLemma("user") || (dep.isPos(CTLibEn.POS_PRP) && !dep.isLemma("i")))
				{
					if (aux.isLemma("do"))
					{
						if (!aux.isPos(CTLibEn.POS_VBD) && !aux.isPos(CTLibEn.POS_VBN))
						{
							aux.form = "do";
							aux.pos  = CTLibEn.POS_VBP;
						}
					}
					else if (aux.isLemma("be"))
					{
						if (aux.isPos(CTLibEn.POS_VBD) || aux.isPos(CTLibEn.POS_VBN))
						{
							aux.form = "were";
							aux.pos  = CTLibEn.POS_VBD;
						}
						else
						{
							aux.form = "are";
							aux.pos  = CTLibEn.POS_VBP;
						}
					}
					else if (aux.isLemma("have"))
					{
						if (!aux.isPos(CTLibEn.POS_VBD) && !aux.isPos(CTLibEn.POS_VBN))
						{
							aux.form = "have";
							aux.pos  = CTLibEn.POS_VBP;
						}
					}
				}
				
				break;
			}
		}
	}
	
	private String generateQuestionForms(DEPNode verb, String delim)
	{
		List<StringIntPair> list = new ArrayList<StringIntPair>();
		generateQuestionFormsRec(list, verb, delim);
		Collections.sort(list);
		
		StringBuilder build = new StringBuilder();
		String end = "?";
		int i;
		
		if (verb.getFeat(FEAT_INF) != null)
		{
			build.append(delim);
			build.append("please");
			end = ".";
		}
		
		for (i=list.size()-1; i>=0; i--)
		{
			build.append(delim);
			build.append(list.get(i).s);
		}
		
		String tmp = build.substring(delim.length());
		
		if (tmp.endsWith(".") || tmp.endsWith("!"))
			tmp = tmp.substring(0, tmp.length()-1) + end;
		else
			tmp += end;
		
		return UTString.setFirstCharToUpper(tmp);
	}
	
	private void generateQuestionFormsRec(List<StringIntPair> list, DEPNode node, String delim)
	{
		List<DEPArc> deps = node.getDependents();
		
		if (node.isLabel(DEPLibEn.DEP_POSS) && !node.isLemma("my"))
			list.add(new StringIntPair("your", node.id));
		else if (node.isPos(CTLibEn.POS_PRP) && !node.isLemma("i") && !node.isLemma("me"))
			list.add(new StringIntPair("you", node.id));
		else if (node.isLemma("user"))
			list.add(new StringIntPair("you", node.id));
		else if (deps.isEmpty())
			list.add(new StringIntPair(node.form, node.id));
		else
		{
			boolean added = false;
			boolean hasPoss = !node.getDependentsByLabels(DEPLibEn.DEP_POSS).isEmpty();
			DEPNode dep;
			
			for (DEPArc arc : deps)
			{
				if (hasPoss && arc.isLabel(DEPLibEn.DEP_DET))
					continue;
				
				dep = arc.getNode();
				
				if (!added && dep.id > node.id)
				{
					list.add(new StringIntPair(node.form, node.id));
					added = true;
				}
				
				generateQuestionFormsRec(list, arc.getNode(), delim);
			}
			
			if (!added)
				list.add(new StringIntPair(node.form, node.id));
		}
	}
	
	/**
	 * Returns a dependency tree representing an interrogative form of the specific subordinating clause.
	 * @param verb the root of the subordinating clause.
	 * @return a dependency tree representing an interrogative form of the specific subordinating clause.
	 */
	public Pair<DEPTree,DEPNode> generateQuestion(DEPNode verb)
	{
		Set<DEPNode> added = new HashSet<DEPNode>();
		DEPTree tree = new DEPTree();
		DEPNode rel, aux;
		
		rel = setRelativizer(tree, verb, added);
		aux = setAuxiliary(tree, verb, added, rel);
		setRest(tree, verb, added);
		setRoot(tree, verb);
		
		return new Pair<DEPTree,DEPNode>(tree, aux);
	}
	
	private DEPNode setRelativizer(DEPTree tree, DEPNode verb, Set<DEPNode> added)
	{
		DEPNode dep, rel, head;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			rel = DEPLibEn.getRefDependentNode(dep);
			
			if (rel != null)
			{
				if (verb.id < rel.id)
				{
					head = rel.getHead();
					
					while (head != verb && !head.isPos(CTLibEn.POS_IN) && !MPLibEn.isVerb(head.pos))
					{
						rel  = head;
						head = head.getHead();
					}
				}
				else
				{
					head = rel.getHead();
					
					while (head != verb && head.id < verb.id)
					{
						rel  = head;
						head = head.getHead();
					}
				}

				addSubtree(tree, rel, added);
				return rel;
			}
		}
		
		return null;
	}
	
	private DEPNode setAuxiliary(DEPTree tree, DEPNode verb, Set<DEPNode> added, DEPNode rel)
	{
		if (rel != null && DEPLibEn.P_SBJ.matcher(rel.getLabel()).find())
			return null;

		DEPNode dep;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			
			if (arc.isLabel(DEPLibEn.P_AUX) && !dep.isPos(CTLibEn.POS_TO))
			{
				if (dep.isLemma("get"))
					return addDoAuxiliary(tree, dep);
				else
				{
					addSubtree(tree, dep, added);
					return dep;
				}
			}
		}

		if (verb.isLabel(DEPLibEn.DEP_XCOMP))
		{
			toNonFinite(verb);
			
			if (rel != null)
			{
				dep = getNode(verb, "should", "should", CTLibEn.POS_MD, DEPLibEn.DEP_AUX, "AM-MOD");
				tree.add(dep);
				tree.add(getNode(verb, "I", "i", CTLibEn.POS_PRP, DEPLibEn.DEP_NSUBJ, "A01"));
				return dep;
			}
			else
			{
				verb.addFeat(FEAT_INF, "true");
				return null;
			}
		}
		else if (verb.isLemma("be"))
		{
			tree .add(verb);
			added.add(verb);
			return verb;
		}
		else
			return addDoAuxiliary(tree, verb);
	}
	
	private DEPNode addDoAuxiliary(DEPTree tree, DEPNode verb)
	{
		DEPNode aux;
		
		if (verb.isPos(CTLibEn.POS_VBZ))
			tree.add(aux = getNode(verb, "does", "do", verb.pos, DEPLibEn.DEP_AUX, null));
		else if (verb.isPos(CTLibEn.POS_VBD) || verb.isPos(CTLibEn.POS_VBN))
			tree.add(aux = getNode(verb, "did" , "do", CTLibEn.POS_VBD, DEPLibEn.DEP_AUX, null));
		else
			tree.add(aux = getNode(verb, "do"  , "do", verb.pos, DEPLibEn.DEP_AUX, null));
		
		toNonFinite(verb);
		return aux;
	}
	
	private void setRest(DEPTree tree, DEPNode verb,  Set<DEPNode> added)
	{
		for (DEPNode node : verb.getSubNodeSortedList())
		{
			if (added.contains(node))
				continue;
			else if (node.isDependentOf(verb) && (node.isPos(CTLibEn.POS_TO) || node.isLabel(DEPLibEn.DEP_COMPLM) || node.isLabel(DEPLibEn.DEP_MARK)))
				continue;
			else
				tree.add(node);
		}
	}
	
	private void setRoot(DEPTree tree, DEPNode verb)
	{
		verb.setHead(tree.get(0), DEPLibEn.DEP_ROOT);
		tree.resetIDs();
		tree.resetDependents();
	}
	
	private void addSubtree(DEPTree tree, DEPNode head, Set<DEPNode> added)
	{
		List<DEPNode> list = head.getSubNodeSortedList();
		
		tree .addAll(list);
		added.addAll(list);
	}
	
	private void toNonFinite(DEPNode verb)
	{
		verb.form = verb.lemma;
		verb.pos  = CTLibEn.POS_VB;
	}
	
	private DEPNode getNode(DEPNode verb, String form, String lemma, String pos, String deprel, String label)
	{
		DEPNode aux = new DEPNode(0, form, lemma, pos, new DEPFeat());
		aux.initSHeads();
		
		aux.setHead (verb, deprel);
		if (label != null)	aux.addSHead(verb, label);

		return aux;
	}
	
	static public void main(String[] args)
	{
		SRLReader reader = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		reader.open(UTInput.createBufferedFileReader(args[0]));
		LGAsk ask = new LGAsk();
		DEPTree tree;
		
		while ((tree = reader.next()) != null)
			System.out.println(ask.genarateQuestionFromAsk(tree, " "));
	}
}
