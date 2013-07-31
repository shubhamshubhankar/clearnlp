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
package com.googlecode.clearnlp.pattern;

import java.util.regex.Pattern;

import com.googlecode.clearnlp.constant.universal.STConstant;
import com.googlecode.clearnlp.constant.universal.STPunct;

/**
 * @since 1.4.2
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class PTLib
{
	static public Pattern SPACE = Pattern.compile(STConstant.SPACE);
	static public Pattern UNDERSCORE = Pattern.compile(STPunct.UNDERSCORE);
	
	static public String[] split(String s, Pattern p)
	{
		return p.split(s);
	}
	
	static public String[] splitSpace(String s)
	{
		return split(s, SPACE);
	}
	
	static public String[] splitUnderscore(String s)
	{
		return split(s, UNDERSCORE);
	}
}
