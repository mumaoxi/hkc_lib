#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from extrautils import *

try:
    app_path = sys.argv[1]
    if len(sys.argv) == 3:
        unsigned_foler = sys.argv[2]
    else:
        unsigned_foler = ""
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python build.py config/dyda config"
    print "==============================="
    print
    sys.exit()

extrautils = ExtraUtils()
print extrautils.merge()
