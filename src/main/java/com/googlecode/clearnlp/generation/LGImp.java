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
package com.googlecode.clearnlp.generation;

import com.googlecode.clearnlp.constant.universal.STConstant;
import com.googlecode.clearnlp.constant.universal.STPunct;
import com.googlecode.clearnlp.constituent.CTLibEn;
import com.googlecode.clearnlp.dependency.DEPNode;
import com.googlecode.clearnlp.dependency.DEPTree;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class LGImp
{
	static public String generateSentenceFromImperative(String prefix, DEPTree tree, boolean switchUnI) 
	{
		tree = tree.clone();
		tree.setDependents();
		
		StringBuilder build = new StringBuilder();
		
		build.append(prefix);
		build.append(STConstant.SPACE);
		build.append(LGLibEn.getForms(tree, false, STConstant.SPACE));
		
		DEPNode last = tree.get(tree.size()-1);
		if (last.isPos(CTLibEn.POS_PERIOD))
			build.append(STPunct.PERIOD);
		
		return build.toString();
	}
}
