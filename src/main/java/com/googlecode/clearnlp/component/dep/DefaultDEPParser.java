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
package com.googlecode.clearnlp.component.dep;

import java.util.List;
import java.util.zip.ZipInputStream;

import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.classification.prediction.StringPrediction;
import com.googlecode.clearnlp.classification.train.StringTrainSpace;
import com.googlecode.clearnlp.dependency.DEPLabel;
import com.googlecode.clearnlp.dependency.DEPNode;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class DefaultDEPParser extends AbstractDEPParser
{
	/** Constructs a dependency parsing for training. */
	public DefaultDEPParser(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, lexica, margin, beams);
	}
	
	/** Constructs a dependency parsing for developing. */
	public DefaultDEPParser(JointFtrXml[] xmls, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, models, lexica, margin, beams);
	}
	
	/** Constructs a dependency parser for bootsrapping. */
	public DefaultDEPParser(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, models, lexica, margin, beams);
	}
	
	/** Constructs a dependency parser for decoding. */
	public DefaultDEPParser(ZipInputStream in)
	{
		super(in);
	}
	
	@Override
	protected void rerankPredictions(List<StringPrediction> ps) {}

	@Override
	protected void resetPost(DEPNode lambda, DEPNode beta, DEPLabel label) {}

	@Override
	protected void postParse() {}
	
	@Override
	protected boolean isNotHead(DEPNode node) {return false;}

	@Override
	protected boolean resetPre(DEPNode lambda, DEPNode beta) {return false;}
}
