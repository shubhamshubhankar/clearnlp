/**
 * Copyright (c) 2009/09-2012/08, Regents of the University of Colorado
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Copyright 2012/09-2013/04, University of Massachusetts Amherst
 * Copyright 2013/05-Present, IPSoft Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package com.googlecode.clearnlp.morphology;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class MPLibTest
{
	@Test
	public void normalizePunctuationTest()
	{
		String form = "...!!!???---***===~~~,,,";
		assertEquals("..!!??--**==~~,,", MPLib.normalizePunctuation(form));
		
		form = ". .. ... !!! ??? ---- ***** === ~~~ , ,, ,,,";
		assertEquals(". .. .. !! ?? -- ** == ~~ , ,, ,,", MPLib.normalizePunctuation(form));
	}
	
	@Test
	public void containsPunctuationTest()
	{
		String form = "a@b.com";
		assertEquals(true , MPLib.containsAnyPunctuation(form));
		assertEquals(false, MPLib.containsOnlyPunctuation(form));
		assertEquals(true , MPLib.containsAnySpecificPunctuation(form, '@', '?'));	
		assertEquals(false, MPLib.containsAnySpecificPunctuation(form, '!', '?'));
		
		form = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";
		assertEquals(true, MPLib.containsAnyPunctuation(form));
		assertEquals(true, MPLib.containsOnlyPunctuation(form));
		assertEquals(true, MPLib.containsAnySpecificPunctuation(form, '@', '?'));
		
		form = "abcde";
		assertEquals(false, MPLib.containsAnyPunctuation(form));
	}
	
	@Test
	public void normalizeDigitsTest()
	{
		String form = "10%";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "$10";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "A.01";
		assertEquals("A.0", MPLib.normalizeDigits(form));
		form = "A:01";
		assertEquals("A:0", MPLib.normalizeDigits(form));
		form = "A/01";
		assertEquals("A/0", MPLib.normalizeDigits(form));
		form = ".01";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "12.34";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "12,34,56";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "12:34:56";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "12-34-56";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "12/34/56";
		assertEquals("0", MPLib.normalizeDigits(form));
		form = "$10.23,45:67-89/10%";
		assertEquals("0", MPLib.normalizeDigits(form));
	}
	
	@Test
	public void revertBracketTest()
	{
		assertEquals("(", MPLib.revertBracket("-LRB-"));
		assertEquals(")", MPLib.revertBracket("-RRB-"));
		assertEquals("[", MPLib.revertBracket("-LSB-"));
		assertEquals("]", MPLib.revertBracket("-RSB-"));
		assertEquals("{", MPLib.revertBracket("-LCB-"));
		assertEquals("}", MPLib.revertBracket("-RCB-"));
		
		assertEquals("(0)", MPLib.revertBracket("-LRB-0-RRB-"));
		assertEquals(":-)", MPLib.revertBracket(":--RRB-"));
	}

	@Test
	public void isPeriodLikeTest()
	{
		String form = ".";
		assertEquals(true, MPLib.isPeriodLike(form));
		form = "?";
		assertEquals(true, MPLib.isPeriodLike(form));
		form = "!";
		assertEquals(true, MPLib.isPeriodLike(form));
		form = ".?!";
		assertEquals(true, MPLib.isPeriodLike(form));
		form = ".?!.?!";
		assertEquals(true, MPLib.isPeriodLike(form));
		form = "/.?!.?!";
		assertEquals(true, MPLib.isPeriodLike(form));
		form = "?-";
		assertEquals(false, MPLib.isPeriodLike(form));
		form = "@?";
		assertEquals(false, MPLib.isPeriodLike(form));
	}
	
	public void splitWhiteSpaces()
	{
		String s = "A B\nC\tD\rE\fF  G\n";
		assertEquals("[A, B, C, D, E, F, G]", Arrays.toString(MPLib.splitWhiteSpaces(s)));
	}
}
