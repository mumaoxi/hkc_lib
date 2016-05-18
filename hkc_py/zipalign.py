#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    src_apk = sys.argv[1]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python zipalign.py unzipaligned_apk"
    print "==============================="
    print
    sys.exit()

print ApkUtils.zipalign(src_apk)
