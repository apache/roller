package org.roller.presentation.atomadminapi.sdk;

import java.util.Locale;

class LocaleString {
    private Locale locale;
    
    public LocaleString(String localeString) {
        if (localeString == null) {
            locale = null;
            return;
        }
        
        String[] components = localeString.split("_");
        
        if (components == null) {
            locale = null;
            return;
        }
                
        if (components.length == 1) {
            locale = new Locale(components[0]);
        } else if (components.length == 2) {
            locale = new Locale(components[0], components[1]);
        } else if (components.length == 3) {
            locale = new Locale(components[0], components[1], components[2]);
        } else {
            throw new IllegalArgumentException("invalid locale string: " + localeString);
        }
    }
    
    public Locale getLocale() {
        return locale;
    }
    
}
