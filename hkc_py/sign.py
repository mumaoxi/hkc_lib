#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    src_apk = sys.argv[1]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python sign.py unsigned.apk"
    print "==============================="
    print
    sys.exit()

print ApkUtils.sign(src_apk)
