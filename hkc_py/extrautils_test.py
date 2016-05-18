#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from extrautils import *

default_encoding = 'utf-8'
if sys.getdefaultencoding() != default_encoding:
    reload(sys)
    sys.setdefaultencoding(default_encoding)

try:
    config_file_path = "config"
    dest_dir = "config/remerge/fresh"
    new_package = "com.mel0t.meshow"
    new_app_label = "老野猫"
    launchable_activity = "com.blsm.sft.fresh.SplashActivity"
    hkc_ah_app_key = "tu123ah"
    hkc_um_app_key = "tu456um"
    hdpi = "config/com.mel0t.meshow_4081636_hdpi_72.png"
    extrautils = ExtraUtils(config_file_path, dest_dir, new_package,
                            new_app_label, launchable_activity, hkc_ah_app_key, hkc_um_app_key, hdpi)
    extrautils.merge()
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python extrautils.py"
    print "==============================="
    print
    sys.exit()
