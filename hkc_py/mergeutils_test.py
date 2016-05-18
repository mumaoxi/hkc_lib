#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from mergeutils import *

default_encoding = 'utf-8'
if sys.getdefaultencoding() != default_encoding:
    reload(sys)
    sys.setdefaultencoding(default_encoding)

try:
    extra_apk_dir = "config/remerge/com_mel0t_meshow_extra.apk"
    dest_dir = "config/remerge/fresh"
    launchable_activity = "com.blsm.sft.fresh.SplashActivity"
    new_package = "com.mel0t.meshow"
    mergeutils = MergeUtils(
        extra_apk_dir, dest_dir, launchable_activity, new_package)
    mergeutils.merge_extra()
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python mergeutils_test.py"
    print "==============================="
    print
    sys.exit()
