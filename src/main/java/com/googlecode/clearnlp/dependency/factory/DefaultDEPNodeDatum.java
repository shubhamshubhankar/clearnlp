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
package com.googlecode.clearnlp.dependency.factory;

import java.io.Serializable;


/**
 * @since 1.5.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DefaultDEPNodeDatum implements IDEPNodeDatum, Serializable
{
	private static final long serialVersionUID = 669975643426329724L;
	
	int    id;
	String form;
	String lemma;
	String pos;
	String feats;
	String namedEntity;
	String syntacticHead;
	String semanticHeads;

	@Override
	public int getID()
	{
		return id;
	}

	@Override
	public String getForm()
	{
		return form;
	}

	@Override
	public String getLemma()
	{
		return lemma;
	}

	@Override
	public String getPOS()
	{
		return pos;
	}

	@Override
	public String getFeats()
	{
		return feats;
	}

	@Override
	public String getNamedEntity()
	{
		return namedEntity;
	}

	@Override
	public String getSyntacticHead()
	{
		return syntacticHead;
	}
	
	@Override
	public String getSemanticHeads()
	{
		return semanticHeads;
	}

	@Override
	public void setID(int id)
	{
		this.id = id;
	}

	@Override
	public void setForm(String form)
	{
		this.form = form;
	}

	@Override
	public void setLemma(String lemma)
	{
		this.lemma = lemma;
	}

	@Override
	public void setPOS(String pos)
	{
		this.pos = pos;
	}

	@Override
	public void setFeats(String feats)
	{
		this.feats = feats;
	}

	@Override
	public void setNamedEntity(String namedEntity)
	{
		this.namedEntity = namedEntity;
	}

	@Override
	public void setSyntacticHead(String syntacticHead)
	{
		this.syntacticHead = syntacticHead;
	}

	@Override
	public void setSemanticHeads(String semanticHeads)
	{
		this.semanticHeads = semanticHeads;
	}
}
