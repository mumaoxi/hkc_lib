#!/usr/bin/env python
# -*- coding: UTF-8 -*-
from packageutils import *

try:
    dest_dir = sys.argv[1]
    if len(sys.argv) == 3:
        new_package = sys.argv[2]
    else:
        new_package = ""
except Exception, e:
    print e
    print "==============================="
    print "Prompt>>>python repackage.py config/dyda com.blsm.tu"
    print "==============================="
    print
    sys.exit()

packageutils = PackageUtils(dest_dir, new_package)
packageutils.modify_all_package()
