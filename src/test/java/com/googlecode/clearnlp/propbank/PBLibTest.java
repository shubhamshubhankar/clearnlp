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
package com.googlecode.clearnlp.propbank;

import static org.junit.Assert.assertEquals;

import java.util.regex.Matcher;

import org.junit.Test;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class PBLibTest
{
	@Test
	public void testPatterns()
	{
		assertEquals(true, PBLib.P_ARGN.matcher("A0").find());
		assertEquals(true, PBLib.P_ARGN.matcher("A1-DSP").find());
		assertEquals(true, PBLib.P_ARGN.matcher("C-A0").find());
		assertEquals(true, PBLib.P_ARGN.matcher("R-A0").find());
		assertEquals(true, PBLib.P_ARGN.matcher("ARG0").find());
		assertEquals(true, PBLib.P_ARGN.matcher("ARG1-DSP").find());
		
		assertEquals(false, PBLib.P_ARGN.matcher("AM-TMP").find());
		assertEquals(false, PBLib.P_ARGN.matcher("ARGM-TMP").find());
		assertEquals(false, PBLib.P_ARGN.matcher("LINK-SLC").find());
		
		Matcher m = PBLib.P_ARGN.matcher("A0");
		if (m.find()) assertEquals("0", m.group(3));
		
		m = PBLib.P_ARGN.matcher("C-A0");
		if (m.find()) assertEquals("0", m.group(3));
		
		m = PBLib.P_ARGN.matcher("R-A0");
		if (m.find()) assertEquals("0", m.group(3));
		
		m = PBLib.P_ARGN.matcher("A1-DSP");
		if (m.find()) assertEquals("1", m.group(3));
	}
}
