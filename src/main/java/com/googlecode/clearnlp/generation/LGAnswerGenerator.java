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

import java.util.List;
import java.util.regex.Pattern;

import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.dependency.srl.SRLArc;
import com.googlecode.clearnlp.dependency.srl.SRLLib;
import com.googlecode.clearnlp.dependency.srl.SRLTree;
import com.googlecode.clearnlp.util.UTString;

/**
 * Designed for Eliza at IPsoft.
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGAnswerGenerator
{
	public String getAnswer(DEPTree qdTree, DEPTree rdTree, int qVerbID, int rVerbID, String delim)
	{
		qdTree = qdTree.cloneSRL();	qdTree.setDependents();
		rdTree = rdTree.cloneSRL();	rdTree.setDependents();
		
		LGLibEn.convertUnI(qdTree);
		LGLibEn.convertUnI(rdTree);
		
		SRLTree qsTree = qdTree.getSRLTree(qVerbID);
		SRLTree rsTree = rdTree.getSRLTree(rVerbID);
		
		SRLLib.relinkRelativeClause(rsTree);
		
		if (isShortAnswer(qsTree, rsTree))
			return getShortAnswer(qdTree, rdTree, qsTree, rsTree, delim);
		else
			return getLongAnswer(qdTree, qsTree, rsTree.getPredicate(), delim);
	}
	
	private boolean isShortAnswer(SRLTree qsTree, SRLTree rsTree)
	{
		SRLArc rA0 = rsTree.getFirstArgument("A0");
		
		if (matchPassive(qsTree.getFirstArgument("R-A0"), rA0))
			return true;
		
		if (matchPassive(qsTree.getFirstArgument("A0"), rA0))
			return true;
		
		DEPNode rVerb = rsTree.getPredicate();
		
		for (DEPArc arc : rVerb.getDependents())
		{
			if (arc.isLabel(DEPLibEn.P_SBJ))
				return false;
		}
		
		return true;
	}
	
	private boolean matchPassive(SRLArc qArc, SRLArc rArc)
	{
		return qArc != null && rArc != null && (qArc.getNode().isLabel(DEPLibEn.P_SBJ) && rArc.getNode().isLabel(DEPLibEn.DEP_AGENT) || qArc.getNode().isLabel(DEPLibEn.DEP_AGENT) && rArc.getNode().isLabel(DEPLibEn.P_SBJ));
	}
	
	private String getShortAnswer(DEPTree qdTree, DEPTree rdTree, SRLTree qsTree, SRLTree rsTree, String delim)
	{
		DEPNode qArg, rArg;
		List<SRLArc> arcs;
		String answer;
		
		for (SRLArc qArc : qsTree.getArguments())
		{
			if (qArc.isLabel(SRLLib.P_ARG_REF))
			{
				arcs = rsTree.getArguments(getBaseLabels(qArc.getLabel()));
				return arcs.isEmpty() ? null : getAnswer(arcs, delim);
			}
		}
		
		for (SRLArc qArc : qsTree.getArguments())
		{
			qArg = qArc.getNode();
			
			if (qArg.getFeat(DEPLibEn.FEAT_PB) != null)
			{
				arcs = rsTree.getArguments(getBaseLabels(qArc.getLabel()));
				
				for (SRLArc rArc : arcs)
				{
					rArg = rArc.getNode();
					
					if (rArg.getFeat(DEPLibEn.FEAT_PB) != null && rArg.isLemma(qArg.lemma))
					{
						answer = getShortAnswer(qdTree, rdTree, qdTree.getSRLTree(qArg.id), rdTree.getSRLTree(rArg.id), delim);
						if (answer != null)	return answer;
					}
				}
			}
		}
		
		return null;
	}
	
	private String getLongAnswer(DEPTree qdTree, SRLTree qsTree, DEPNode rVerb, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		getLongAnswerAux(qdTree, qsTree, rVerb, delim, build);
		return UTString.setFirstCharToUpper(build.substring(delim.length()));
	}
	
	private void getLongAnswerAux(DEPTree qdTree, SRLTree qsTree, DEPNode rVerb, String delim, StringBuilder build)
	{
		boolean added = false;
		DEPNode rDep;
		DEPArc  rHead;
		SRLArc  qArc;
		
		for (DEPArc rArc : rVerb.getDependents())
		{
			rDep  = rArc.getNode();
			rHead = rDep.getSHead(rVerb);
			
			if (!added && rDep.id > rVerb.id)
			{
				build.append(delim);
				build.append(rVerb.form);
				added = true;
		//		System.out.println("A: "+build.toString());
			}
			
			if (rArc.isLabel(DEPLibEn.DEP_COMPLM) || rArc.isLabel(DEPLibEn.DEP_CONJ) || rArc.isLabel(DEPLibEn.DEP_CC) || rArc.isLabel(DEPLibEn.DEP_PRECONJ))
				continue;
			else if (rHead == null || rHead.isLabel(SRLLib.S_ARGM_MOD) || rHead.isLabel(SRLLib.S_ARGM_NEG))
			{
				build.append(delim);
				build.append(rDep.getSubForms(delim));
		//		System.out.println("1: "+rDep.form+" "+build.toString());
			}
			else if (qsTree.containsLabel(rHead.getLabel()))
			{
				if (rDep.getFeat(DEPLibEn.FEAT_PB) != null && (qArc = qsTree.getFirstArgument(rHead.getLabel())) != null && qArc.getNode().getFeat(DEPLibEn.FEAT_PB) != null)
				{
					getLongAnswerAux(qdTree, qdTree.getSRLTree(qArc.getNode().id), rDep, delim, build);
		//			System.out.println("2: "+rDep.form+" "+build.toString());
				}
				else
				{
					build.append(delim);
					build.append(rDep.getSubForms(delim));
		//			System.out.println("3: "+rDep.form+" "+build.toString());
				}
			}
		}
		
		if (!added)
		{
			build.append(delim);
			build.append(rVerb.form);
		//	System.out.println("B: "+build.toString());
		}
	}
	
	private String getAnswer(List<SRLArc> arcs, String delim)
	{
		StringBuilder build = new StringBuilder();
		
		for (SRLArc arc : arcs)
		{
			build.append(delim);
			build.append(LGLibEn.getForms(arc.getNode(), delim));
		}
		
		String s = build.substring(delim.length());
		s = UTString.stripPunctuation(s);
		s = UTString.setFirstCharToUpper(s);
		
		SRLArc arc = arcs.get(0);
		
		if (arc.getNode().isLabel(DEPLibEn.DEP_AGENT) && s.startsWith("By"))
			s = s.substring(2).trim();
		
		return s;
	}
	
	private Pattern getBaseLabels(String label)
	{
		label = SRLLib.getBaseLabel(label);
		return Pattern.compile("^"+label+"|"+SRLLib.S_PREFIX_CONCATENATION+label+"$");
	}
}
