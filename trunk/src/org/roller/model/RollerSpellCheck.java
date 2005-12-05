package org.roller.model;

import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Since this class cannot be thread-safe due to the
 * SpellCheckListener interface, first a "canonical"
 * SpellDictionary must be instantiated with the path
 * to the dictionary.  Subsequently, call getInstance()
 * which will return a new instance of RollerSpellCheck
 * using the "precompiled" SpellDictionary, or will throw
 * a NullPointerException.  A better way can
 * probably be found.
 **/
public class RollerSpellCheck implements SpellCheckListener
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerSpellCheck.class);
        
    private static SpellDictionary dictionary = null;

    private SpellChecker spellCheck = null;
    private ArrayList spellCheckEvents = new ArrayList();

    /**
     * Initializer which takes an InputStream for
     * /WEB-INF/english.0 to load the SpellDictionary.
     * Building the SpellDictionary, and thus the SpellChecker
     * is an expensive operation.
     * You can get this InputStream with ServletContext.getResource(
     * "/WEB-INF/english.0").  Throws a RollerException if
     * SpellDictionary cannot be instantiated.
     **/
    public static void init(InputStream in) throws RollerException
    {
        try {
            InputStreamReader inR = new InputStreamReader( in );
            RollerSpellCheck.dictionary = new SpellDictionary( inR );

        } catch (IOException ioe) {
            mLogger.error("RollerSpellCheck unable to load SpellDictionary",
                ioe);
            throw new RollerException(ioe);
        }
    }

    /**
     * Private constructor taking a prebuilt SpellChecker
     * as an argument.
     **/
    private RollerSpellCheck()
    {
        spellCheck = new SpellChecker(dictionary);
        spellCheck.addSpellCheckListener(this);
    }

    /**
     * Returns a new instance of RollerSpellCheck, using
     * the (hopefully) prebuilt SpellChecker.
     **/
    public static RollerSpellCheck getInstance() throws RollerException
    {
        if (RollerSpellCheck.dictionary == null)
        {
            throw new RollerException(
                "RollerSpellCheck.SpellDictionary has not been defined");
        }

        return new RollerSpellCheck();
    }

    /**
     * Fulfills interface SpellCheckListener.
     * SpellCheckEvent is placed into the ArrayList
     * spellCheckEvents held by this RollerSpellCheck.
     **/
    public void spellingError(SpellCheckEvent event)
    {
        spellCheckEvents.add( event );
    }

    /**
     * This is the method to check spelling.
     * The submitted String is "parsed" by SpellChecker,
     * SpellCheckEvents are placed into an ArrayList, which
     * is returned to the caller.  A SpellCheckEvent contains
     * the "suspect" word, and a LinkedList of suggested replacements.
     */
    public ArrayList checkSpelling(String str) throws RollerException
    {
        spellCheck.checkSpelling( new StringWordTokenizer(str) );
        return spellCheckEvents;
    }

    /**
     * Convenience method. Creates a RollerSpellCheck object
     * and calls checkSpelling(str) on it, returning the ArrayList.
     */
    public static ArrayList getSpellingErrors(String str) throws RollerException
    {
        RollerSpellCheck rCheck = RollerSpellCheck.getInstance();
        return rCheck.checkSpelling(str);
    }
}