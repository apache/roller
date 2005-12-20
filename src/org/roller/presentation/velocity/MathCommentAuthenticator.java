package org.roller.presentation.velocity;

import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.pojos.CommentData;


/**
 * Asks the commenter to answer a simple math question.
 *
 * @author David M Johnson
 */
public class MathCommentAuthenticator implements CommentAuthenticator {
    
    private transient ResourceBundle bundle =
            ResourceBundle.getBundle("ApplicationResources");
    
    private static Log mLogger = LogFactory.getLog(MathCommentAuthenticator.class);
    
    
    public String getHtml(Context context, 
                    HttpServletRequest request, 
                    HttpServletResponse response) {

        String answer = "";
        
        HttpSession session = request.getSession(true);
        if (session.getAttribute("mathAnswer") == null) {
            // starting a new test
            int value1 = (int)(Math.random()*10.0);
            int value2 = (int)(Math.random()*100.0);
            int sum = value1 + value2;
            session.setAttribute("mathValue1", new Integer(value1));
            session.setAttribute("mathValue2", new Integer(value2));
            session.setAttribute("mathAnswer", new Integer(sum));
        } else {
            // preserve user's answer
            answer = request.getParameter("answer");
            answer = (answer == null) ? "" : answer;
        }
        
        // pull existing values out of session
        Integer value1o = (Integer)request.getSession().getAttribute("mathValue1");
        Integer value2o = (Integer)request.getSession().getAttribute("mathValue2");
        
        StringBuffer sb = new StringBuffer();
        
        sb.append("<p>");
        sb.append(bundle.getString("comments.mathAuthenticatorQuestion"));
        sb.append("</p><p>");
        sb.append(value1o);
        sb.append(" + ");
        sb.append(value2o);
        sb.append(" = ");
        sb.append("<input name=\"answer\" value=\"");
        sb.append(answer);
        sb.append("\" /></p>");
        
        return sb.toString();
    }
    
    
    public boolean authenticate(CommentData comment, HttpServletRequest request) {
        
        boolean authentic = false;
        
        HttpSession session = request.getSession(false);
        String answerString = request.getParameter("answer");
        
        if (answerString != null && session != null) {
            try {
                int answer = Integer.parseInt(answerString);
                Integer sum = (Integer) session.getAttribute("mathAnswer");
                
                if (answer == sum.intValue()) {
                    authentic = true;
                    session.removeAttribute("mathAnswer");
                    session.removeAttribute("mathValue1");
                    session.removeAttribute("mathValue2");
                }
            } catch (NumberFormatException ignored) {
                // ignored ... someone is just really bad at math
            } catch (Exception e) {
                // unexpected
                mLogger.error(e);
            }
        }
        
        return authentic;
    }
    
}

