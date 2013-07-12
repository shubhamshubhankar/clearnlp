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
package com.googlecode.clearnlp.run;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import com.googlecode.clearnlp.propbank.frameset.PBFrameset;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
public class FramesetSave
{
	public static void main(String[] args) throws Exception
	{
		PBFrameset p = new PBFrameset(args[0], args[1]);
		ObjectOutputStream oout = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(args[2])));
		
		oout.writeObject(p);
		oout.close();
		
	/*	ObjectInputStream oin = new ObjectInputStream(new BufferedInputStream(new FileInputStream(args[2])));
		p = (PBFrameset)oin.readObject();
		oin.close();
		
		System.out.println(p.isVerb("jinho"));
		System.out.println(p.isVerb("arrive"));
		System.out.println(p.isVerbParticleConstruction("jinho", "choi"));
		System.out.println(p.isVerbParticleConstruction("take", "down"));
		System.out.println(p.isVerbParticleConstruction("take", "apart"));*/
	}
}
