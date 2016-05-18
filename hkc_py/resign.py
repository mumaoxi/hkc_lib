#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

"""
python  ~/Documents/iProject/Coding/rebuild/resign.py ~/Downloads/fresh.apk
python  ~/Documents/iProject/Coding/rebuild/resign.py http://aihuo360.cn/download/android/sqsx_qudao.apk
"""
try:
    src_apk = sys.argv[1]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python resign.py test.apk"
    print "Prompt>>>python resign.py http://aihuo360.cn/download/android/sqsx_qudao.apk"
    print "==============================="
    print
    sys.exit()

print ApkUtils.resign(src_apk)
