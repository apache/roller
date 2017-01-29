package org.apache.roller.weblogger.ui.restapi;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(path = "/tb-ui/app")
public class UIController {

    @RequestMapping(value = "/mytest")
    public ModelAndView globalConfig() {
        ActionItem myItem = new ActionItem();
        myItem.name = "Sam";
        Map<String, Object> myMap = new HashMap<>();
        myMap.put("name2", "bob");
        myMap.put("action", myItem);
        return new ModelAndView(".GlobalConfig", myMap);
    }

    public class ActionItem {
        String name;
        public String getName() {
            return name;
        }
    }
}
