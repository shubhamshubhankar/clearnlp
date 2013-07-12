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
import java.util.zip.ZipInputStream;

import com.googlecode.clearnlp.component.morph.CEnglishMPAnalyzer;
import com.googlecode.clearnlp.constant.english.STConstant;
import com.googlecode.clearnlp.constituent.CTLibEn;
import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPFeat;
import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.dependency.srl.SRLLib;
import com.googlecode.clearnlp.morphology.MPLibEn;
import com.googlecode.clearnlp.util.UTString;
import com.googlecode.clearnlp.util.pair.Pair;
import com.googlecode.clearnlp.util.pair.StringIntPair;

/**
 * Used for Eliza.
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGAsk
{
	final String VERB_TENSE = CEnglishMPAnalyzer.LANG_DIR+"verb.tense";
	
	static private final String NON_FINITE    = "non-finite";
	static private final String WH_NON_FINITE = "wh-non-finite";
	
	private LGVerbEn g_verb;
	
	public LGAsk(ZipInputStream inputStream)
	{
		g_verb = new LGVerbEn(inputStream);
	}
	
	public String genarateAskFromQuestion(DEPTree tree, String delim)
	{
		tree = tree.clone();
		tree.setDependents();
		
		DEPNode root = tree.getFirstRoot();
		return (root == null) ? null : genarateAskFromQuestionAux(tree, root, delim);
	}
	
	private String genarateAskFromQuestionAux(DEPTree tree, DEPNode verb, String delim)
	{
		DEPLibEn.convertFirstFormToLowerCase(tree);
		DEPNode ref = getReferentArgument(verb);
		
		if (ref == null || !ref.isLabel(DEPLibEn.P_SBJ))
			relocateAuxiliary(tree, verb);

		tree.resetIDs();
		tree.resetDependents();
		
		return getAsk(tree, verb, ref, delim);
	}
	
	private DEPNode getReferentArgument(DEPNode verb)
	{
		DEPNode dep;
		
		for (DEPArc arc : verb.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.containsSHead(verb, SRLLib.P_ARG_REF))
				return dep;
		}
		
		return null;
	}
	
	private void relocateAuxiliary(DEPTree tree, DEPNode verb)
	{
		List<DEPNode> auxes = new ArrayList<DEPNode>();
		DEPNode sbj = null;

		for (DEPArc arc : verb.getDependents())
		{
			if (arc.isLabel(DEPLibEn.P_AUX))
				auxes.add(arc.getNode());
			else if (arc.isLabel(DEPLibEn.P_SBJ))
				sbj = arc.getNode();
		}
		
		if (sbj != null)
		{
			if (!auxes.isEmpty() && auxes.get(0).id < sbj.id)
			{
				relocateAuxiliaryAux(tree, verb, auxes, sbj);
			}
			else if (verb.isLemma(STConstant.BE) && verb.id < sbj.id)
			{
				tree.remove(verb);
				tree.add(sbj.getLastNode().id, verb);
				setBeVerbForm(verb, sbj);
			}
		}
	}
	
	private void relocateAuxiliaryAux(DEPTree tree, DEPNode verb, List<DEPNode> auxes, DEPNode sbj)
	{
		DEPNode aux = auxes.get(0);
		tree.remove(aux);
		
		if (aux.isLemma(STConstant.DO))
		{
			if (auxes.size() > 1)
			{
				DEPNode node = auxes.get(1);
				
				if (MPLibEn.isVerb(node.pos))
					verb = node;
			}
			
			verb.pos = aux.pos;
			
			if (aux.isPos(CTLibEn.POS_VBD))
				verb.form = g_verb.getPastForm(verb.lemma);
			else if (aux.isPos(CTLibEn.POS_VBZ))
				verb.form = LGVerbEn.get3rdSingularForm(verb.lemma);
			else if (aux.isPos(CTLibEn.POS_VBP) && sbj.isLemma(STConstant.YOU))
			{
				verb.form = LGVerbEn.get3rdSingularForm(verb.lemma);
				verb.pos  = CTLibEn.POS_VBZ;
			}
		}
		else
		{
			tree.add(sbj.getLastNode().id, aux);
			
			if (aux.isLemma(STConstant.BE))
				setBeVerbForm(aux, sbj);
			else if (aux.isLemma(STConstant.HAVE))
				set3rdSingularVerbForm(aux, sbj);
		}
	}
	
	private void setBeVerbForm(DEPNode verb, DEPNode sbj)
	{
		if (sbj.isLemma(STConstant.YOU))
		{
			if (verb.isPos(CTLibEn.POS_VBD))
				verb.form = "was";
			else if (verb.isPos(CTLibEn.POS_VBP))
			{
				verb.form = "is";
				verb.pos  = CTLibEn.POS_VBZ;
			}
		}
	}
	
	private void set3rdSingularVerbForm(DEPNode verb, DEPNode sbj)
	{
		if (sbj.isLemma(STConstant.YOU))
		{
			if (verb.isPos(CTLibEn.POS_VBP))
			{
				verb.form = LGVerbEn.get3rdSingularForm(verb.lemma);
				verb.pos  = CTLibEn.POS_VBZ;
			}
		}
	}
	
	private boolean hasRelativizer(DEPTree tree)
	{
		int i, size = tree.size();
		DEPNode node;
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			if (node.containsSHead(SRLLib.P_ARG_REF))
				return true;
		}
		
		return false;
	}
	
	private String getAsk(DEPTree tree, DEPNode verb, DEPNode ref, String delim)
	{
		StringBuilder build = new StringBuilder();
		build.append("Ask");
		
		if (ref == null && !hasRelativizer(tree))
		{
			build.append(delim);
			build.append("whether");
		}
		
		for (DEPNode node : verb.getSubNodeSortedList())
		{
			build.append(delim);
			
			if (node.isLemma(STConstant.YOU))
				build.append("the user");
			else if (node.isLemma(STConstant.YOUR) || node.isLemma(STConstant.YOURS))
				build.append("the user's");
			else
				build.append(node.form);
		}
		
		return UTString.stripPunctuation(build.toString())+".";
	}
	
	/** Generates a question from the ask lemma. */
	static public String genarateQuestionFromAsk(DEPTree tree, String delim)
	{
		tree = tree.clone();
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
	
	/** Called by {@link LGAsk#genarateQuestionFromAsk(DEPTree, String)}. */
	static private void matchNumber(DEPNode verb, DEPNode aux)
	{
		for (DEPArc arc : verb.getDependents())
		{
			if (arc.isLabel(DEPLibEn.P_SBJ))
			{
				DEPNode dep = arc.getNode();
				
				if (dep.isLemma("user") || (dep.isPos(CTLibEn.POS_PRP) && !dep.isLemma("I")))
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
	
	/** Called by {@link LGAsk#genarateQuestionFromAsk(DEPTree, String)}. */
	static private String generateQuestionForms(DEPNode verb, String delim)
	{
		List<StringIntPair> list = new ArrayList<StringIntPair>();
		generateQuestionFormsRec(list, verb, delim);
		Collections.sort(list);
		
		StringBuilder build = new StringBuilder();
		String end = "?";
		String vtype;
		int i;
		
		if ((vtype = verb.getFeat(DEPLib.FEAT_VERB_TYPE)) != null && vtype.equals(NON_FINITE))
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
		
		return UTString.convertFirstCharToUpper(tmp);
	}
	
	/** Called by {@link LGAsk#generateQuestionForms(DEPNode, String)}. */
	static private void generateQuestionFormsRec(List<StringIntPair> list, DEPNode node, String delim)
	{
		List<DEPArc> deps = node.getDependents();
		
		if (node.isLabel(DEPLibEn.DEP_POSS) && !node.isLemma("my"))
			list.add(new StringIntPair("your", node.id));
		else if (node.isPos(CTLibEn.POS_PRP) && !node.isLemma("I") && !node.isLemma("me"))
			list.add(new StringIntPair("you", node.id));
		else if (node.isLemma("user"))
			list.add(new StringIntPair("you", node.id));
		else if (deps.isEmpty())
			list.add(new StringIntPair(node.form, node.id));
		else
		{
			boolean notAdded = true;
			boolean hasPoss = !node.getDependentsByLabels(DEPLibEn.DEP_POSS).isEmpty();
			DEPNode dep;
			
			for (DEPArc arc : deps)
			{
				if (hasPoss && arc.isLabel(DEPLibEn.DEP_DET))
					continue;
				
				dep = arc.getNode();
				
				if (notAdded && dep.id > node.id)
				{
					list.add(new StringIntPair(node.form, node.id));
					notAdded = false;
				}
				
				generateQuestionFormsRec(list, arc.getNode(), delim);
			}
			
			if (notAdded)
				list.add(new StringIntPair(node.form, node.id));
		}
	}

	/** @param verb a dependecy node whose pos tag is a verb type and dependency relation is either {@link DEPLibEn#DEP_CCOMP} or {@link DEPLibEn#DEP_XCOMP}. */
	static public DEPTree generateInterrogativeOrImperative(DEPNode verb)
	{
		DEPTree tree = generateQuestion(verb).o1;
		String vType = verb.getFeat(DEPLib.FEAT_VERB_TYPE);
		return (vType == null || !vType.equals(WH_NON_FINITE)) ? tree : null;
	}
	
	/** Called by {@link LGAsk#genarateQuestionFromAsk(DEPTree, String)}. */
	static public Pair<DEPTree,DEPNode> generateQuestion(DEPNode verb)
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
	
	static private DEPNode setRelativizer(DEPTree tree, DEPNode verb, Set<DEPNode> added)
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
	
	static private DEPNode setAuxiliary(DEPTree tree, DEPNode verb, Set<DEPNode> added, DEPNode rel)
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
				tree.add(getNode(verb, "I", "I", CTLibEn.POS_PRP, DEPLibEn.DEP_NSUBJ, "A01"));
				verb.addFeat(DEPLib.FEAT_VERB_TYPE, WH_NON_FINITE);
				return dep;
			}
			else
			{
				verb.addFeat(DEPLib.FEAT_VERB_TYPE, NON_FINITE);
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
	
	static private DEPNode addDoAuxiliary(DEPTree tree, DEPNode verb)
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
	
	static private void setRest(DEPTree tree, DEPNode verb,  Set<DEPNode> added)
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
	
	static private void setRoot(DEPTree tree, DEPNode verb)
	{
		verb.setHead(tree.get(0), DEPLibEn.DEP_ROOT);
		tree.resetIDs();
		tree.resetDependents();
	}
	
	static private void addSubtree(DEPTree tree, DEPNode head, Set<DEPNode> added)
	{
		List<DEPNode> list = head.getSubNodeSortedList();
		
		tree .addAll(list);
		added.addAll(list);
	}
	
	static private void toNonFinite(DEPNode verb)
	{
		verb.form = verb.lemma;
		verb.pos  = CTLibEn.POS_VB;
	}
	
	static private DEPNode getNode(DEPNode verb, String form, String lemma, String pos, String deprel, String label)
	{
		DEPNode aux = new DEPNode(0, form, lemma, pos, new DEPFeat());
		aux.initXHeads();
		aux.initSHeads();
		
		aux.setHead (verb, deprel);
		if (label != null)	aux.addSHead(verb, label);

		return aux;
	}
}
