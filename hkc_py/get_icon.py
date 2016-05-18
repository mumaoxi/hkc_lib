#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

# try:
# app_path = sys.argv[1]
# icon =  sys.argv[2]
# except Exception, e:
#   print e
#   print "==============================="
#   print "Prompt>>>python get_icon.py config/demo.apk ic_launcher"
#   print "==============================="
#   print
#   sys.exit()

apkutils = ApkUtils()
src_apk = "/Users/Tu/Documents/iProject/Coding/rebuild/config/zgjm_20140327034059_V1.31_c31.apk"
icon = "res/drawable-mdpi/ic_launcher.png"
new_name = "com.blsm.dream_31_hdpi_72"
print apkutils.get_icon_from_apk(src_apk, icon, new_name)
