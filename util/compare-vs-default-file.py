# Licensed to the Apache Software Foundation (ASF) under one or more
#  contributor license agreements.  The ASF licenses this file to You
# under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.  For additional information regarding
# copyright in this work, please see the NOTICE file in the top level
# directory of this distribution.
#
# This script compares a given resource file against the default (English) file,
# signalling unused strings that can be removed from the given file.
#
# To use: 
# 1.) first run util/check-default-resource-file.py to remove
#     unused strings from the default (English) resource file as well as (via IntelliJ)
#     the resource files of the other languages -- see that .py file for instructions.
#     This step should quickly get rid of most extra strings in all the language files.
# 2.) Edit the open() command below for the language resource file you wish to check.
#     then run from a command prompt: python compare-vs-default-file.py | grep UNUSED > results.txt
# 3.) results.txt will list unused strings that can probably be removed. Good to do a
#     a full IDE search on each one listed to confirm.
#
#!/usr/bin/python

import re
import os
from contextlib import closing
import sys

def prop_names():
    rfile = open("../app/src/main/resources/ApplicationResources_de.properties")
    prop_pattern = re.compile('^([a-zA-Z]+(\.[a-zA-Z]+)*)=.*')
    for line in rfile:
        m = prop_pattern.match(line)
        if (m):
            propname = m.group(1)
            yield propname


for propname in prop_names():
    cmd = 'find ../app -type f -name "ApplicationResources.properties" | xargs grep -n %s /dev/null | wc -l' % propname
    with closing(os.popen(cmd,'r')) as pipe:
        occurrences = int(pipe.readline())
    if (occurrences == 0):
        print "Property %s is UNUSED" % propname
    else:
        print "Property %s occurs %d times" % (propname, occurrences)
    sys.stdout.flush()





        
    
