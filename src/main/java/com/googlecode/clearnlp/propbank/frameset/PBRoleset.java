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
package com.googlecode.clearnlp.propbank.frameset;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.googlecode.clearnlp.constant.universal.STConstant;
import com.googlecode.clearnlp.constant.universal.STPunct;
import com.googlecode.clearnlp.morphology.MPLib;
import com.googlecode.clearnlp.propbank.PBLib;
import com.googlecode.clearnlp.util.UTXml;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PBRoleset implements Serializable, Comparable<PBRoleset>
{
	static Logger LOG = Logger.getLogger(PBRoleset.class.getName());
	
	private static final long serialVersionUID = 1632699410581892419L;
	private Map<String,PBRole> m_roles;
	private Set<String> s_vncls;
	private String s_name;
	private String s_id;
	
	public PBRoleset(Element eRoleset)
	{
		init();
		
		setID(UTXml.getTrimmedAttribute(eRoleset, PBFLib.A_ID));
		setName(UTXml.getTrimmedAttribute(eRoleset, PBFLib.A_NAME));
		addVerbNetClasses(UTXml.getTrimmedAttribute(eRoleset, PBFLib.A_VNCLS));
		addRoles(eRoleset.getElementsByTagName(PBFLib.E_ROLE));
	}
	
	public void init()
	{
		s_vncls = Sets.newHashSet();
		m_roles = Maps.newHashMap();
	}
	
	private void addVerbNetClasses(String classes)
	{
		if (!classes.equals(STConstant.EMPTY) && !classes.equals(STPunct.HYPHEN))
		{
			for (String vncls : classes.split(STConstant.SPACE))
				addVerbNetClass(vncls);
		}
	}
	
	public void addVerbNetClass(String vncls)
	{
		s_vncls.add(vncls);
	}
	
	public void addRoles(NodeList list)
	{
		int i, size = list.getLength();
		
		for (i=0; i<size; i++)
			addRole((Element)list.item(i));
	}
	
	public void addRole(Element eRole)
	{
		addRole(new PBRole(eRole));
	}
	
	public void addRole(PBRole role)
	{
		if (!isValidAnnotation(role))
		{
			if (!s_id.endsWith(PBLib.LIGHT_VERB))
				LOG.debug("Invalid argument: "+s_id+" - "+role.getArgKey()+"\n");
		}
		else
		{
			m_roles.put(role.getArgNumber(), role);
			
			for (String vncls : role.getVNClasses())
			{
				if (!s_vncls.contains(vncls))
					System.err.printf("VerbNet class mismatch: %s - %s\n", s_id, role.getArgKey());
			}
		}
	}
	
	private boolean isValidAnnotation(PBRole role)
	{
		String n = role.getArgNumber();
		if (n.length() != 1) return false;
		
		if (MPLib.containsOnlyDigits(n))	return true;
		if (n.equals("A"))					return true;
		if (n.equals("M") && !role.getFunctionTag().equals(STConstant.EMPTY))	return true;
		
		return false;
	}
	
	public Set<String> getVerbNetClasses()
	{
		return s_vncls;
	}
	
	public List<PBRole> getRoleSortedList()
	{
		List<PBRole> list = Lists.newArrayList(m_roles.values());
		
		Collections.sort(list);
		return list;
	}
	
	/** @param argNumber e.g., {@code "0"}, {@code "2"}. */
	public PBRole getRole(String argNumber)
	{
		return m_roles.get(argNumber);
	}

	public String getID()
	{
		return s_id;
	}
	
	public String getName()
	{
		return s_name;
	}
	
	public void setID(String id)
	{
		s_id = id;
	}
	
	public void setName(String name)
	{
		s_name = name;
	}
	
	public boolean isValidArgument(String label)
	{
		Matcher m = PBLib.P_ARGN.matcher(label);
		
		if (m.find())
			return m_roles.containsKey(m.group(3));
		
		return true;
	}
	
	@Override
	public String toString()
	{
		StringBuilder build = new StringBuilder();
		
		build.append(s_id);
		build.append(": ");
		build.append(s_name);
		
		for (String vncls : s_vncls)
		{
			build.append(", ");
			build.append(vncls);
		}
		
		build.append(STConstant.NEW_LINE);
		
		for (PBRole role : getRoleSortedList())
		{
			build.append(role.toString());
			build.append(STConstant.NEW_LINE);
		}
		
		return build.toString();
	}

	@Override
	public int compareTo(PBRoleset roleset)
	{
		return s_id.compareTo(roleset.s_id);
	}
}
