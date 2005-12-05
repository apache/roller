/*
 * Created on Jul 20, 2003
 *
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.business.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.roller.business.IndexManagerImpl;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author aim4min
 *
 * Class containing helper methods. 
 */
public class IndexUtil {

	/**
	 * Create a lucene term from the first token of the input string. 
	 * 
	 * @param field The lucene document field to create a term with
	 * @param input The input you wish to convert into a term
	 * @return Lucene search term
	 */
	public static final Term getTerm(String field, String input) {
		if (input==null || field==null) return null;
		Analyzer analyer = IndexManagerImpl.getAnalyzer();
		TokenStream tokens = analyer.tokenStream(field,
			new StringReader(input));
		
		Token token = null;
		Term term = null;
		try {
			token = tokens.next();
		} catch (IOException e) {}
		if (token!=null) {
			String termt = token.termText();
			term = new Term(field,termt);
		}
		return term;
	}

}
