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
package com.googlecode.clearnlp.generation;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.googlecode.clearnlp.constituent.CTLibEn;
import com.googlecode.clearnlp.dependency.DEPFeat;
import com.googlecode.clearnlp.dependency.DEPLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;


/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class LGLibEnTest
{
	@Test
	public void testConvertUnI()
	{
		DEPTree tree = new DEPTree();
		DEPNode sbj1 = new DEPNode(1, "I"   , "i"   , CTLibEn.POS_PRP , new DEPFeat());
		DEPNode verb = new DEPNode(2, "am"  , "be"  , CTLibEn.POS_VBP , new DEPFeat());
		DEPNode attr = new DEPNode(3, "me"  , "me"  , CTLibEn.POS_PRP , new DEPFeat());
		DEPNode poss = new DEPNode(4, "my"  , "my"  , CTLibEn.POS_POS , new DEPFeat());
		DEPNode appo = new DEPNode(5, "mine", "mine", CTLibEn.POS_PRPS, new DEPFeat());
		
		sbj1.setHead(verb, DEPLibEn.DEP_NSUBJ);
		verb.setHead(tree.get(0), DEPLibEn.DEP_ROOT);
		attr.setHead(verb, DEPLibEn.DEP_ATTR);
		poss.setHead(appo, DEPLibEn.DEP_POSS);
		appo.setHead(attr, DEPLibEn.DEP_APPOS);
		
		tree.add(sbj1);
		tree.add(verb);
		tree.add(attr);
		tree.add(poss);
		tree.add(appo);
		tree.setDependents();
		
		String si = "I am me my mine";
		String su = "you are you your yours";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
		
		si = "I was me my mine";
		su = "you were you your yours";
		verb.form = "was";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
		
		DEPNode sbj2 = new DEPNode(1, "He", "he", CTLibEn.POS_PRP, new DEPFeat());
		sbj2.setHead(sbj1, DEPLibEn.DEP_CONJ);
		tree.add(2, sbj2);
		tree.resetIDs();
		tree.resetDependents();
		
		si = "I He are me my mine";
		su = "you He are you your yours";
		verb.form = "are";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
		
		sbj2.setHead(verb, DEPLibEn.DEP_NSUBJ);
		sbj1.setHead(sbj2, DEPLibEn.DEP_CONJ);
		tree.remove(sbj2);
		tree.add(1, sbj2);
		
		si = "He I are me my mine";
		su = "He you are you your yours";
		
		assertEquals(tree.toStringRaw(), si);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), su);
		LGLibEn.convertUnI(tree);
		assertEquals(tree.toStringRaw(), si);
	}
}
