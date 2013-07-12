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
package com.googlecode.clearnlp.constant.english;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class ENModalTest
{
	@Test
	public void testGetForms() throws IllegalArgumentException, IllegalAccessException
	{
		assertEquals(true , ENModal.contains(ENModal.CAN));
		assertEquals(false, ENModal.contains("need"));
	}
}
