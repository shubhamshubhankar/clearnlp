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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class ENPunct
{
	static final private Set<String> VALUE_SET = new HashSet<String>(getValueList());

	static public final String PIPE				= "|";
	static public final String UNDERSCORE		= "_";
	static public final String COMMA			= ",";
	static public final String QUESTION_MARK	= "?";
	static public final String PERIOD			= ".";
	
	/**
	 * @param lemma a lower-case string.
	 * @return {@code true} if this class contains the specific lemma; otherwise, {@code false}.
	 */
	static public boolean contains(String lemma)
	{
		return VALUE_SET.contains(lemma);
	}
	
	/** @return a list containing all field values of this class. */
	static public List<String> getValueList()
	{
		List<String> list = new ArrayList<String>();
		Class<ENPunct> cs = ENPunct.class;
		
		try
		{
			for (Field f : cs.getFields())
			{
				list.add(f.get(cs).toString());
			}
		}
		catch (IllegalArgumentException e) {e.printStackTrace();}
		catch (IllegalAccessException e)   {e.printStackTrace();}
		
		return list;
	}
}
