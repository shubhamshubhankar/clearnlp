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

import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.zip.ZipInputStream;

import org.junit.Ignore;
import org.junit.Test;

import com.googlecode.clearnlp.dependency.DEPTree;
import com.googlecode.clearnlp.reader.SRLReader;
import com.googlecode.clearnlp.util.UTInput;

/** @author Jinho D. Choi ({@code jdchoi77@gmail.com}) */
public class LGAskTest
{
	@Test
	public void testGenarateQuestionFromAsk()
	{
		SRLReader reader = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		reader.open(UTInput.createBufferedFileReader("src/test/resources/generation/ask.txt"));
		DEPTree tree;
		int i;
		
	/*	Ask whether the user wants to reset the user's password.
		Ask whether I should place an order for the user.
		Ask whether you want me to place an order for you.
		Ask if the user's account is locked.
		Ask if the user's account is being locked.
		Ask if the user's account was locked yesterday.
		Ask if the user is an existing customer.
		Ask if the user has registered the user's account.
		Ask where the user was yesterday.
		Ask what the user does for a living.
		Ask what the user wants to buy.
		Ask how long the user has been waiting for.
		Ask how soon the user wants the product to be shipped.
		Ask what kind of books the user likes to buy.
		Ask when the user's account got locked.
		Ask what you can do for the user.
		Ask who helped the user last time.
		Ask which of the user's accounts is locked.
		Ask when to reset the user's password.
		Ask to enter the user's password.
		Ask entering the user's password.
		Ask to be patient.*/

		String[] questions = {
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Do you want to reset your password?",
				"Should I place an order for you?",
				"Do you want me to place an order for you?",
				"Is your account locked?",
				"Is your account being locked?",
				"Was your account locked yesterday?",
				"Are you an existing customer?",
				"Have you registered your account?",
				"Where were you yesterday?",
				"What do you do for a living?",
				"What do you want to buy?",
				"How long have you been waiting for?",
				"How soon do you want the product to be shipped?",
				"What kind of books do you like to buy?",
				"When did your account get locked?",
				"What can I do for you?",
				"Who helped you last time?",
				"Which of your accounts is locked?",
				"When should I reset your password?",
				"Please enter your password.",
				"Please enter your password.",
				"Please be patient."};
		
		for (i=0; (tree = reader.next()) != null; i++)
			assertEquals(questions[i], LGAsk.genarateQuestionFromAsk(tree, " "));
	}

	@Ignore
	@Test
	public void testGenarateAskFromQuestion() throws Exception
	{
		SRLReader reader = new SRLReader(0, 1, 2, 3, 4, 5, 6, 7);
		reader.open(UTInput.createBufferedFileReader("src/test/resources/generation/ask2.txt"));
		LGAsk ask = new LGAsk(new ZipInputStream(new BufferedInputStream(new FileInputStream("/Users/jdchoi/Documents/Workspace/ClearNLP2/clearnlp-models/dictionary/dictionary-1.4.0.zip"))));
		DEPTree tree;
		int i;
		
		String[] asks = {
				"Ask whether the user wants to reset the user's password.",
				"Ask whether I should place an order for the user.",
				"Ask whether the user wanted me to place an order for the user.",
				"Ask whether the user's account is locked.",
				"Ask whether the user's account is being locked.",
				"Ask whether the user's account was locked yesterday.",
				"Ask whether the user was playing basketball.",
				"Ask whether the user is an existing customer.",
				"Ask whether the user has registered the user's account.",
				"Ask where the user was yesterday.",
				"Ask what the user does for a living.",
				"Ask what the user wants to buy.",
				"Ask how long the user has been waiting for.",
				"Ask how soon the user wants the product to be shipped.",
				"Ask what kind of books the user likes to buy.",
				"Ask when the user's account got locked.",
				"Ask what I can do for the user.",
				"Ask who helped the user last time.",
				"Ask which of the user's accounts is locked.",
				"Ask when I should reset the user's password.",
				"Ask what the user's username is."};
		
		for (i=0; (tree = reader.next()) != null; i++)
			assertEquals(asks[i], ask.genarateAskFromQuestion(tree, " "));
	}
}
