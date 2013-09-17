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
package com.googlecode.clearnlp.dependency;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.googlecode.clearnlp.dependency.factory.IArgInfoDatum;
import com.googlecode.clearnlp.dependency.srl.ArgInfo;

/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class ArgInfoTest
{
	@Test
	public void testArgInfo()
	{
		ArgInfo oldInfo = new ArgInfo();
		
		oldInfo.setPredicateId(7);
		oldInfo.setSemanticInfo("A1");
		oldInfo.pushSyntacticInfo("dobj", "take");
		oldInfo.pushSyntacticInfo("prt", "outr");
		
		testArgInfoDatum(oldInfo);
	}
	
	private void testArgInfoDatum(ArgInfo oldInfo)
	{
		IArgInfoDatum datum = oldInfo.getArgInfoDatum();
		ArgInfo newInfo = ArgInfo.buildFrom(datum);
		
		assertEquals(oldInfo.getPredicateId(), newInfo.getPredicateId());
		assertEquals(oldInfo.getSemanticInfo(), newInfo.getSemanticInfo());
		assertEquals(oldInfo.getSyntacticInfo().toString(), newInfo.getSyntacticInfo().toString());
	}
}
