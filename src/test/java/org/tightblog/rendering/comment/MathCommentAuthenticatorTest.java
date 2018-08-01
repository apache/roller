/*
   Copyright 2017 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.comment;

import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.support.ResourceBundleMessageSource;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MathCommentAuthenticatorTest {

    private static MathCommentAuthenticator mathCommentAuthenticator;

    @BeforeClass
    public static void initialize() {
        mathCommentAuthenticator = new MathCommentAuthenticator();
        ResourceBundleMessageSource messages = new ResourceBundleMessageSource();
        messages.setBasename("ApplicationResources");
        mathCommentAuthenticator.setMessages(messages);
    }

    private HttpServletRequest createMockRequest(HttpSession mockSession) {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockRequest.getSession(anyBoolean())).thenReturn(mockSession);
        when(mockRequest.getLocale()).thenReturn(Locale.ENGLISH);
        return mockRequest;
    }

    @Test
    public void getHtmlNewTest() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        HttpServletRequest mockRequest = createMockRequest(mockSession);

        ArgumentCaptor<Integer> firstValCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> secondValCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> sumCaptor = ArgumentCaptor.forClass(Integer.class);

        when(mockSession.getAttribute("mathValue1")).thenReturn(5);
        when(mockSession.getAttribute("mathValue2")).thenReturn(57);
        String actual = mathCommentAuthenticator.getHtml(mockRequest);

        // check that for new test, sum is equal to two random values generated
        verify(mockSession).setAttribute(eq("mathValue1"), firstValCaptor.capture());
        verify(mockSession).setAttribute(eq("mathValue2"), secondValCaptor.capture());
        verify(mockSession).setAttribute(eq("mathAnswer"), sumCaptor.capture());
        assertEquals((long) (firstValCaptor.getValue() + secondValCaptor.getValue()), (long) sumCaptor.getValue());

        // mocked session ignores setAttribute calls, so relying on "when" calls above for getAttribute calls
        String expected = "<p>Please answer this simple math question</p><p>5 + 57 = <input name='answer' value='' required></p>";
        assertEquals(expected, actual);
    }

    @Test
    public void getHtmlAlreadyAnsweredTest() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        HttpServletRequest mockRequest = createMockRequest(mockSession);

        when(mockSession.getAttribute("mathAnswer")).thenReturn(45);
        when(mockSession.getAttribute("mathValue1")).thenReturn(8);
        when(mockSession.getAttribute("mathValue2")).thenReturn(37);
        when(mockRequest.getParameter("answer")).thenReturn("42");
        String actual = mathCommentAuthenticator.getHtml(mockRequest);
        String expected =
                "<p>Please answer this simple math question</p><p>8 + 37 = <input name='answer' value='42' required></p>";
        assertEquals(expected, actual);
    }

    @Test
    public void authenticatePass() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        HttpServletRequest mockRequest = createMockRequest(mockSession);
        when(mockRequest.getParameter("answer")).thenReturn("82");
        when(mockSession.getAttribute("mathAnswer")).thenReturn(82);
        boolean actual = mathCommentAuthenticator.authenticate(mockRequest);
        assertTrue("Authenticate didn't pass with correct answer", actual);
    }

    @Test
    public void authenticateFailNoAnswer() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        HttpServletRequest mockRequest = createMockRequest(mockSession);
        boolean actual = mathCommentAuthenticator.authenticate(mockRequest);
        assertFalse(actual);
    }

    @Test
    public void authenticateFailAnswerIncorrect() throws Exception {
        HttpSession mockSession = mock(HttpSession.class);
        HttpServletRequest mockRequest = createMockRequest(mockSession);
        when(mockRequest.getParameter("answer")).thenReturn("82");
        when(mockSession.getAttribute("mathAnswer")).thenReturn(84);
        boolean actual = mathCommentAuthenticator.authenticate(mockRequest);
        assertFalse("Authenticate didn't fail with incorrect answer", actual);
    }
}
