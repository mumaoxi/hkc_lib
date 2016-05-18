#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    src_apk = sys.argv[1]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python decode.py test.apk"
    print "==============================="
    print
    sys.exit()

print ApkUtils.decode(src_apk)
