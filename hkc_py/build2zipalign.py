#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    project_dir = sys.argv[1]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python build2zipalign.py project_dir"
    print "==============================="
    print
    sys.exit()

apkutils = ApkUtils()
print apkutils.build2zipalign(project_dir)
