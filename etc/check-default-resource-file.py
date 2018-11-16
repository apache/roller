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
# This script checks the default (English) resource file for unused strings that can be removed.
# 
# To use: 
# 1.) run from a command prompt: python check-default-resource-file.py | grep UNUSED > results.txt
# 2.) results.txt will list unused strings that can be removed.  Good to confirm each by doing a
#     full search in your IDE on the string.
#     Caveat #1: due to substring searching, if "xxx.yyy" has 0 usages but "xxx.yyy.zzz"
#     has one or more, "xxx.yyy" will not be marked as unused, so some unused strings may
#     end up remaining in the resource file.
#
#     Caveat #2: certain strings are dynamically generated in the code (weblogEntriesPager.*.*
#     for example) and hence will show up in the list as false positives -- don't delete them.
#
# 3.) Removal is best from the IntelliJ Resource Bundle view, where you can delete the key and it
#     will automatically delete the string from all of the language resource files.
#
#!/usr/bin/python

import re
import os
from contextlib import closing
import sys

def prop_names():
    rfile = open("../src/main/resources/messages.properties")
    prop_pattern = re.compile('^([a-zA-Z]+(\.[a-zA-Z]+)*)=.*')
    for line in rfile:
        m = prop_pattern.match(line)
        if (m):
            propname = m.group(1)
            yield propname


for propname in prop_names():
    cmd = 'find ../app -type f \! -name "messages*" | xargs grep -n %s /dev/null | wc -l' % propname
    with closing(os.popen(cmd,'r')) as pipe:
        occurrences = int(pipe.readline())
    if (occurrences == 0):
        print "Property %s is UNUSED" % propname
    else:
        print "Property %s occurs %d times" % (propname, occurrences)
    sys.stdout.flush()

