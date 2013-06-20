/**
* Copyright (c) 2009-2012, Regents of the University of Colorado
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
*
* Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
* Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
* Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
* AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
* IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
* ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
* LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
* CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
* SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
* INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
* CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
* ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
* POSSIBILITY OF SUCH DAMAGE.
*/
package com.googlecode.clearnlp.dependency.srl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.googlecode.clearnlp.dependency.DEPArc;
import com.googlecode.clearnlp.dependency.DEPLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;

public class SRLLib
{
	static public final String DELIM_PATH_UP	= "^";
	static public final String DELIM_PATH_DOWN	= "|";
	static public final String DELIM_SUBCAT		= "_";
	
	static public final String S_PREFIX_CONCATENATION = "C-";
	static public final String S_PREFIX_REFERENT = "R-";
	static public final String S_ARGM_MOD = "AM-MOD";
	static public final String S_ARGM_NEG = "AM-NEG";
	
	static public final Pattern P_ARG_CONCATENATION = Pattern.compile("^"+S_PREFIX_CONCATENATION+".+$");
	static public final Pattern P_ARG_REF = Pattern.compile("^"+S_PREFIX_REFERENT+".+$");
	static public final Pattern P_ARGN_CORE = Pattern.compile("^A\\d");
	
	static public final Pattern P_ARGN = Pattern.compile("^(A|C-A|R-A)\\d");
	static public final Pattern P_ARGM = Pattern.compile("^AM");
	
	
	static public String getBaseLabel(String label)
	{
		if (label.startsWith(SRLLib.S_PREFIX_CONCATENATION))
			return label.substring(SRLLib.S_PREFIX_CONCATENATION.length());
		else if (label.startsWith(SRLLib.S_PREFIX_REFERENT))
			return label.substring(SRLLib.S_PREFIX_REFERENT.length());
		else
			return label;
	}
	
	static public boolean isNumberedArgument(String label)
	{
		return P_ARGN.matcher(label).find();
	}
	
	static public boolean isCoreNumberedArgument(String label)
	{
		return P_ARGN_CORE.matcher(label).find();
	}
	
	static public boolean isModifier(String label)
	{
		return P_ARGM.matcher(label).find();
	}
	
	static public List<List<DEPArc>> getArgumentList(DEPTree tree)
	{
		int i, size = tree.size();
		List<DEPArc> args;
		DEPNode node;
		
		List<List<DEPArc>> list = new ArrayList<List<DEPArc>>();
		for (i=0; i<size; i++)	list.add(new ArrayList<DEPArc>());
		
		for (i=1; i<size; i++)
		{
			node = tree.get(i);
			
			for (DEPArc arc : node.getSHeads())
			{
				args = list.get(arc.getNode().id);
				args.add(new DEPArc(node, arc.getLabel()));
			}
		}
		
		return list;
	}

	static public void relinkRelativeClause(SRLTree sTree)
	{
		DEPNode pred = sTree.getPredicate();
		DEPArc ref = null, tmp;
		DEPNode arg, dep;
		
		for (DEPArc arc : pred.getDependents())
		{
			dep = arc.getNode();
			
			if (dep.containsSHead(pred, P_ARG_REF))
			{
				ref = arc;
				break;
			}
		}
		
		for (SRLArc sArc : sTree.getArguments())
		{
			arg = sArc.getNode();
			
			for (DEPArc dArc : arg.getDependents())
			{
				dep = dArc.getNode();
				
				if (dep == pred || pred.isDescendentOf(dep))
				{
					arg.removeDependent(dArc);
					dep.setHead(arg.getHead(), arg.getLabel());
					
					if (ref != null) // && ref.isLabel(SRLLib.S_PREFIX_REFERENT+arg.getLabel())
					{
						if (ref.isLabel(DEPLibEn.DEP_PREP))
						{
							DEPNode prep = ref.getNode();
							tmp = new DEPArc(arg, ref.getLabel());
							arg.setHead(prep, ref.getLabel());
							arg.id = prep.id + 1;
							prep.clearDependents();
							prep.addDependent(tmp);
							prep.getSHead(pred).setLabel(sArc.getLabel());
							arg.removeSHead(pred);
						}
						else if (ref.isLabel(DEPLibEn.P_SBJ))
						{
							arg.setHead(pred, ref.getLabel());
							arg.id = ref.getNode().id;
							ref.setNode(arg);
						}
						else
						{
							tmp = new DEPArc(arg, ref.getLabel());
							arg.setHead(pred, ref.getLabel());
							arg.id = pred.id + 1;
							
							if (ref.isLabel(DEPLibEn.P_OBJ) || ref.isLabel(DEPLibEn.DEP_ATTR))
								pred.addDependentRightNextToSelf(tmp);
							else
								pred.addDependent(tmp);
							
							pred.removeDependent(ref);
						}
					}
					else
					{
						tmp = new DEPArc(arg, DEPLibEn.DEP_DEP);
						arg.setHead(pred, tmp.getLabel());
						arg.id = pred.id + 1;
						
						if (sArc.isLabel(P_ARGN))
							pred.addDependentRightNextToSelf(tmp);
						else
							pred.addDependent(tmp);
					}
					
					break;
				}
			}
		}
	}
}
