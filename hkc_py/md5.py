#!/usr/bin/env python
# -*- coding: UTF-8 -*-
import sys
import hashlib

str = sys.argv[1]
m = hashlib.md5()
m.update(str)
print m.hexdigest()
