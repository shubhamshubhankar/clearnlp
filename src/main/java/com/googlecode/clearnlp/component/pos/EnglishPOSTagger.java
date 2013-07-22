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
package com.googlecode.clearnlp.component.pos;

import java.util.Set;
import java.util.zip.ZipInputStream;

import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.classification.train.StringTrainSpace;
import com.googlecode.clearnlp.constant.english.ENAux;
import com.googlecode.clearnlp.constituent.CTLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;

/**
 * Part-of-speech tagger using document frequency cutoffs.
 * @since 1.3.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class EnglishPOSTagger extends AbstractPOSTagger
{
//	====================================== CONSTRUCTORS ======================================

	public EnglishPOSTagger() {}
	
	/** Constructs a part-of-speech tagger for collecting lexica. */
	public EnglishPOSTagger(JointFtrXml[] xmls, Set<String> sLsfs)
	{
		super(xmls, sLsfs);
	}
	
	/** Constructs a part-of-speech tagger for training. */
	public EnglishPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica)
	{
		super(xmls, spaces, lexica);
	}
	
	/** Constructs a part-of-speech tagger for developing. */
	public EnglishPOSTagger(JointFtrXml[] xmls, StringModel[] models, Object[] lexica)
	{
		super(xmls, models, lexica);
	}
	
	/** Constructs a part-of-speech tagger for bootsrapping. */
	public EnglishPOSTagger(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica)
	{
		super(xmls, spaces, models, lexica);
	}
	
	/** Constructs a part-of-speech tagger for decoding. */
	public EnglishPOSTagger(ZipInputStream in)
	{
		super(in);
	}
	
//	================================ APPLY RULES ================================
	
	@Override
	protected boolean applyRules()
	{
		if (s_lsfs.contains(d_tree.get(i_input).lowerSimplifiedForm)) return false;
		if (applyBe()) return true;
		
		return false;
	}
	
	private boolean applyBe()
	{
		DEPNode curr = d_tree.get(i_input);
		DEPNode p2 = d_tree.get(i_input-2);
		DEPNode p1 = d_tree.get(i_input-1);
		
		if (p2 != null)
		{
			if (p2.lowerSimplifiedForm.endsWith("name") && p1.lowerSimplifiedForm.equals(ENAux.IS))
			{
				curr.pos = CTLibEn.POS_NNP;
				return true;
			}
		}
		
		return false;
	}
}
