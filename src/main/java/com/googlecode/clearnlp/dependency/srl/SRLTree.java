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
package com.googlecode.clearnlp.dependency.srl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.googlecode.clearnlp.dependency.DEPLib;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.morphology.MPLibEn;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class SRLTree
{
	private DEPNode      d_predicate;
	private List<SRLArc> l_arguments;
	
	public SRLTree(DEPNode predicate)
	{
		d_predicate = predicate;
		l_arguments = new ArrayList<SRLArc>();
	}
	
	public boolean containsLabel(String label)
	{
		label = SRLLib.getBaseLabel(label);
		
		for (SRLArc arc : l_arguments)
		{
			if (label.equals(SRLLib.getBaseLabel(arc.getLabel())))
				return true;
		}
		
		return false;
	}
	
	public Set<String> getLabels()
	{
		Set<String> labels = new HashSet<String>();
		String label;
		
		for (SRLArc arc : l_arguments)
		{
			label = arc.getLabel();
			
			if (label.startsWith(SRLLib.S_PREFIX_CONCATENATION))
				label = label.substring(SRLLib.S_PREFIX_CONCATENATION.length());
			else if (label.startsWith(SRLLib.S_PREFIX_REFERENT))
				label = label.substring(SRLLib.S_PREFIX_REFERENT.length());
			
			labels.add(label);
		}
		
		return labels;
	}

	public void addArgument(DEPNode argument, String label)
	{
		l_arguments.add(new SRLArc(argument, label));
	}
	
	public DEPNode getPredicate()
	{
		return d_predicate;
	}
	
	public String getRolesetID()
	{
		return d_predicate.getFeat(DEPLib.FEAT_PB);
	}
	
	public SRLArc getFirstArgument(String label)
	{
		for (SRLArc arc : l_arguments)
		{
			if (arc.isLabel(label))
				return arc;
		}
		
		return null;
	}
	
	public List<SRLArc> getArguments(Pattern regex)
	{
		List<SRLArc> args = new ArrayList<SRLArc>();
		
		for (SRLArc arc : l_arguments)
		{
			if (arc.isLabel(regex))
				args.add(arc);
		}
		
		return args;
	}
	
	public List<SRLArc> getArguments()
	{
		return l_arguments;
	}
	
	public String getKey()
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			build.append(";");
			build.append(arg.label);
			build.append(":");
			build.append(arg.getNode().lemma);
		}
		
		return build.toString();
	}
	
	public String getKey(Set<String> ignore)
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			if (!ignore.contains(arg.label))
			{
				build.append(";");
				build.append(arg.label);
				build.append(":");
				build.append(arg.getNode().lemma);				
			}
		}
		
		return build.toString();
	}
	
	public String getKey(Pattern ignore)
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			if (!ignore.matcher(arg.label).find())
			{
				build.append(";");
				build.append(arg.label);
				build.append(":");
				build.append(arg.getNode().lemma);				
			}
		}
		
		return build.toString();
	}
	
	public String getRichKeyEn(Pattern ignore, String space)
	{
		StringBuilder build = new StringBuilder();
		Collections.sort(l_arguments);
		DEPNode node;
		String value;
		
		build.append("V:");
		build.append(getRolesetID());
		
		for (SRLArc arg : l_arguments)
		{
			if (!ignore.matcher(arg.label).find())
			{
				node = arg.getNode();
				
				build.append(";");
				build.append(arg.label);
				build.append(":");
				
				if (MPLibEn.isNoun(node.pos))
					value = node.getSubLemmasEnNoun(space);
				else
					value = node.lemma;
				
				build.append(value);
			}
		}
		
		return build.toString();
	}
}
