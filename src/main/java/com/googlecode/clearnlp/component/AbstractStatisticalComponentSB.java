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
package com.googlecode.clearnlp.component;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.googlecode.clearnlp.classification.feature.JointFtrXml;
import com.googlecode.clearnlp.classification.model.StringModel;
import com.googlecode.clearnlp.classification.train.StringTrainSpace;
import com.googlecode.clearnlp.util.UTInput;
import com.googlecode.clearnlp.util.UTOutput;

/**
 * @since 1.4.0
 * @author Jinho D. Choi ({@code jdchoi77@gmail.com})
 */
abstract public class AbstractStatisticalComponentSB extends AbstractStatisticalComponent
{
	protected int     n_beams;		// beam size
	protected double  d_margin;		// margin threshold
	protected double  d_score;		// total score of the current sequence
	protected boolean b_first;		// true if the current sequence is the first one
	
	public AbstractStatisticalComponentSB() {}
	
	/** Constructs a component for collecting lexica. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls)
	{
		super(xmls);
	}
	
	/** Constructs a component for training. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls, StringTrainSpace[] spaces, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, lexica);
		d_margin = margin;
		n_beams  = beams;
	}
	
	/** Constructs a component for developing. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, models, lexica);
		d_margin = margin;
		n_beams  = beams;
	}
	
	/** Constructs a component for bootstrapping. */
	public AbstractStatisticalComponentSB(JointFtrXml[] xmls, StringTrainSpace[] spaces, StringModel[] models, Object[] lexica, double margin, int beams)
	{
		super(xmls, spaces, models, lexica);
		d_margin = margin;
		n_beams  = beams;
	}
	
	/** Constructs a component for decoding. */
	public AbstractStatisticalComponentSB(ZipInputStream zin)
	{
		super(zin);
	}
	
	public void setMargin(double margin)
	{
		d_margin = margin;
	}
	
	public void setBeams(int beams)
	{
		n_beams = beams;
	}
	
	protected void loadSBConfiguration(ZipInputStream zin) throws Exception
	{
		BufferedReader fin = UTInput.createBufferedReader(zin);
		LOG.info("Loading configuration.\n");
		
		s_models = new StringModel[Integer.parseInt(fin.readLine())];
		n_beams  = Integer.parseInt(fin.readLine());
		d_margin = Double.parseDouble(fin.readLine());
	}
	
	protected void saveSBConfiguration(ZipOutputStream zout, String entryName) throws Exception
	{
		zout.putNextEntry(new ZipEntry(entryName));
		PrintStream fout = UTOutput.createPrintBufferedStream(zout);
		LOG.info("Saving configuration.\n");
		
		fout.println(s_models.length);
		fout.println(n_beams);
		fout.println(d_margin);
		
		fout.flush();
		zout.closeEntry();
	}
	
}
