#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from apkutils import *

try:
    url = "http://apk.aihuo360.com/index.php/package_config/"
    idx = sys.argv[1]
    url = "%s%s" % (url, idx)
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python download_rehole.py 601"
    print "==============================="
    print
    sys.exit()

print ApkUtils.download_file(url, "xml", "rebuild")
