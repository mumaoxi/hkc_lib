#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    project_dir = sys.argv[1]
    new_package = sys.argv[2]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python package2zipalign.py config com.tuan.gou"
    print "==============================="
    print
    sys.exit()

print ApkUtils.package2zipalign(project_dir, new_package)
