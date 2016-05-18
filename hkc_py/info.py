#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    src_apk = sys.argv[1]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python info.py config/dyda.apk"
    print "==============================="
    print
    sys.exit()

ApkUtils.info(src_apk)
