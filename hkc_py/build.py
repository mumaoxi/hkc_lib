#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    project_dir = sys.argv[1]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python build.py project_dir"
    print "==============================="
    print
    sys.exit()

print ApkUtils.build(project_dir)
