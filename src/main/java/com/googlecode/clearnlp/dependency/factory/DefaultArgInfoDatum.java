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
public class DefaultArgInfoDatum implements IArgInfoDatum, Serializable
{
	private static final long serialVersionUID = -5544870341444382479L;
	
	int    predicateID;
	String semanticInfo;
	String syntacticInfo;
	
	@Override
	public int getPredicateID()
	{
		return predicateID;
	}

	@Override
	public String getSemanticInfo()
	{
		return semanticInfo;
	}

	@Override
	public String getSyntacticInfo()
	{
		return syntacticInfo;
	}

	@Override
	public void setPredicateID(int predicateID)
	{
		this.predicateID = predicateID;
	}

	@Override
	public void setSemanticInfo(String semanticInfo)
	{
		this.semanticInfo = semanticInfo;
	}

	@Override
	public void setSyntacticInfo(String syntacticInfo)
	{
		this.syntacticInfo = syntacticInfo;
	}
}
