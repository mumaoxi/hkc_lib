#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *
"""
python  ~/Documents/iProject/Coding/rebuild/repackage.py ~/Downloads/fresh.apk com.blsm.tu
python  ~/Documents/iProject/Coding/rebuild/repackage.py ~/Downloads/fresh.apk com.blsm.tu
python  ~/Documents/iProject/Coding/rebuild/repackage.py http://aihuo360.cn/download/android/sqsx_qudao.apk com.blsm.tu
python  ~/Documents/iProject/Coding/rebuild/repackage.py http://aihuo360.cn/download/android/sqsx_qudao.apk com.blsm.tu
"""
try:
    src_apk = sys.argv[1]
    new_package = sys.argv[2]
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python repackage.py config/fresh.apk com.blsm.tu"
    print "Prompt>>>python repackage.py config/fresh.apk com.blsm.tu"
    print "Prompt>>>python repackage.py http://aihuo360.cn/download/android/sqsx_qudao.apk com.blsm.tu"
    print "Prompt>>>python repackage.py http://aihuo360.cn/download/android/sqsx_qudao.apk com.blsm.tu"
    print "==============================="
    print
    sys.exit()

print ApkUtils.repackage(src_apk, new_package)
