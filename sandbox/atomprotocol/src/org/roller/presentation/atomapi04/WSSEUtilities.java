/*
 * Copyright 2005, Dave Johnson
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.roller.presentation.atomapi04;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;

/**
 * Utilties to support WSSE authentication.
 * @author Dave Johnson
 */
public class WSSEUtilities {
    public static synchronized String generateDigest(
            byte[] nonce, byte[] created, byte[] password) {
        String result = null;
        try {
            MessageDigest digester = MessageDigest.getInstance("SHA");
            digester.reset();
            digester.update(nonce);
            digester.update(created);
            digester.update(password);
            byte[] digest = digester.digest();
            result = new String(base64Encode(digest));
        }
        catch (NoSuchAlgorithmException e) {
            result = null;
        }
        return result;
    }
    public static byte[] base64Decode(String value) throws IOException {
        return Base64.decodeBase64(value.getBytes("UTF-8"));
    }
    public static String base64Encode(byte[] value) {
        return new String(Base64.encodeBase64(value));
    }
    public static String generateWSSEHeader(String userName, String password) 
    throws UnsupportedEncodingException {  
       
        byte[] nonceBytes = Long.toString(new Date().getTime()).getBytes();
        String nonce = new String(WSSEUtilities.base64Encode(nonceBytes));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String created = sdf.format(new Date());
        
        String digest = WSSEUtilities.generateDigest(
                nonceBytes, created.getBytes("UTF-8"), password.getBytes("UTF-8"));
        
        StringBuffer header = new StringBuffer("UsernameToken Username=\"");
        header.append(userName);
        header.append("\", ");
        header.append("PasswordDigest=\"");
        header.append(digest);
        header.append("\", ");
        header.append("Nonce=\"");
        header.append(nonce);
        header.append("\", ");
        header.append("Created=\"");
        header.append(created);
        header.append("\"");
        return header.toString();
    }
}
