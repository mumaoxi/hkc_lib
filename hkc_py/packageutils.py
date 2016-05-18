#!/usr/bin/env python
# -*- coding: UTF-8 -*-
import os
import sys
import yaml
try:
    import xml.etree.cElementTree as ET
except ImportError:
    import xml.etree.ElementTree as ET
import shutil
import logging

logging.basicConfig(level=logging.DEBUG,
                    format='%(levelname)s %(asctime)s %(filename)s %(funcName)s [line:%(lineno)d] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')


class PackageUtils:
    decoded_file_path = None
    old_package = None
    new_package = None
    old_label = None
    new_label = None
    replace_smali_const_package = True
    replace_smali_const_label = True

    def __init__(self, decoded_file_path="", new_package="", replace_smali_const_package=True,
                 replace_smali_const_label=True):
        self.decoded_file_path = decoded_file_path
        self.new_package = new_package
        self.replace_smali_const_package = replace_smali_const_package
        self.replace_smali_const_label = replace_smali_const_label

    def replace(self, filename, oldstr, newstr):
        if not os.path.exists(filename):
            print ">>>not exists %s" % filename
            return
        try:
            with open(filename, "r") as f:
                d = f.read()
                d = d.replace(oldstr, newstr)
                f.close()
            with open(filename, "w") as fw:
                fw.write(d)
                fw.close()
        except Exception, e:
            logging.error(filename)
            logging.error(e)

    # 修改AndroidManifest包名
    def modify_minifest_package(self):
        if self.new_package == None or len(self.new_package) == 0:
            return
        # AndroidManifest.xml
        mani_xml_path = "%s/AndroidManifest.xml" % self.decoded_file_path
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        k_android_authorities = "{%s}authorities" % schemas
        k_android_name = "{%s}name" % schemas
        tree = ET.parse(mani_xml_path)
        root = tree.getroot()
        self.old_package = root.get("package")
        print ">>>old package: %s" % self.old_package
        # 修改manifest包名
        if self.new_package and cmp(self.new_package, self.old_package) != 0:
            root.set("package", self.new_package)
        # 修改application中android:name中的包名
        application = root.find("application")
        old_app_name = application.get(k_android_name)
        if old_app_name is not None and "." not in old_app_name:
            new_app_name = self.old_package + "." + old_app_name
            logging.info("old_app_name: %s" % old_app_name)
            logging.info("new_app_name: %s" % new_app_name)
            application.set(k_android_name, new_app_name)
            tree.write(mani_xml_path, "utf-8", True)
        # 修改provider中android:authorities中的包名
        providers = application.findall("provider")
        for provider in providers:
            old_authorities = provider.get(k_android_authorities)
            if old_authorities != None and self.old_package in old_authorities:
                new_authorities = old_authorities.replace(self.old_package,
                                                          self.new_package)
                provider.set(k_android_authorities, new_authorities)
                print ">>>old authorities: %s" % old_authorities
                print ">>>new authorities: %s" % new_authorities
        tree.write(mani_xml_path, "utf-8", True)
        s_new_package = root.get("package")
        print ">>>new package: %s" % s_new_package
        path = os.path.abspath(mani_xml_path)
        # 修改application、activity、service、broadcast、provider中的.省略方式的包名
        old_package_name = "android:name=\"."
        new_package_name = "android:name=\"%s." % self.old_package
        self.replace(path, old_package_name, new_package_name)
        print "Update Androidmanifest.xml package %s => OK" % mani_xml_path
        print

    # 修改*.smali|*.xml|*.yml|*.java中包名
    def modify_sxyj_package(self):
        print
        print "================================================"
        print "+++Modify *.smali|*.xml|*.yml|*.java package++++"
        print "================================================"
        if self.new_package == None or len(self.new_package) == 0:
            return

        r_old_str_1 = "%s.R" % self.old_package
        r_new_str_1 = "%s.R" % self.new_package
        r_old_str_2 = "L%s/R;" % self.old_package.replace(".", "/")
        r_new_str_2 = "L%s/R;" % self.new_package.replace(".", "/")
        r_old_str_3 = "L%s/R$" % self.old_package.replace(".", "/")
        r_new_str_3 = "L%s/R$" % self.new_package.replace(".", "/")
        xml_old_str = "http://schemas.android.com/apk/res/%s" % self.old_package
        xml_new_str = "http://schemas.android.com/apk/res/%s" % self.new_package
        yml_old_str = "cur_package: %s" % self.old_package
        yml_new_str = "cur_package: %s" % self.new_package
        java_old_str = "%s.BuildConfig" % self.old_package
        java_new_str = "%s.BuildConfig" % self.new_package
        data_data_old_package = "/data/data/%s" % self.old_package
        data_data_new_package = "/data/data/%s" % self.new_package
        # 三个参数：分别返回1.父目录 2.所有文件夹名字（不含路径） 3.所有文件名字
        for parent, dirs, files in os.walk(self.decoded_file_path):
            for filename in files:
                path = os.path.join(parent, filename)
                if filename.endswith(".smali"):
                    self.replace(path, r_old_str_1, r_new_str_1)
                    self.replace(path, r_old_str_2, r_new_str_2)
                    self.replace(path, r_old_str_3, r_new_str_3)
                    self.replace(path, data_data_old_package, data_data_new_package)
                    # 更新*.smali中的包名常量
                    if self.replace_smali_const_package:
                        self.replace(path, self.old_package, self.new_package)
                elif filename.endswith(".xml"):
                    self.replace(path, xml_old_str, xml_new_str)
                elif filename.endswith(".yml"):
                    self.replace(path, yml_old_str, yml_new_str)
                elif filename.endswith(".java"):
                    self.replace(path, r_old_str_1, r_new_str_1)
                    self.replace(path, java_old_str, java_new_str)
        print "Update *.smali|*.xml|*.yml|*.java package %s => OK" % \
              self.decoded_file_path
        print

    # 修改*.smali|*.xml|*.java|*.html中的应用名称
    def modify_sxjh_label(self, old_label, new_label):
        logging.info("")
        logging.info(">>>old_label: %s" % old_label)
        logging.info(">>>new_label: %s" % new_label)
        logging.info("")
        if self.new_label == None or len(new_label) == 0:
            return

        label_old_str = old_label.encode("unicode_escape")
        label_new_str = new_label.encode("unicode_escape")
        logging.info(">>>label_old_str: %s" % label_old_str)
        logging.info(">>>label_new_str: %s" % label_new_str)
        logging.info("")

        # 三个参数：分别返回1.父目录 2.所有文件夹名字（不含路径） 3.所有文件名字
        for parent, dirs, files in os.walk(self.decoded_file_path):
            for filename in files:
                path = os.path.join(parent, filename)
                if filename.endswith(".smali"):
                    if self.replace_smali_const_label:
                        self.replace(path, label_old_str, label_new_str)
                elif filename.endswith(".xml"):
                    self.replace(path, old_label, new_label)
                elif filename.endswith(".java"):
                    self.replace(path, old_label, new_label)
                elif filename.endswith(".html"):
                    self.replace(path, old_label, new_label)
        print "Update *.smali|*.xml|*.java|*.html app label %s => OK" % self.decoded_file_path
        print

    # 移动smali中的R文件
    def move_smali_r(self):
        if self.new_package == None or len(self.new_package) == 0:
            return

        old_r_dir_1 = "%s/smali/%s/R.smali" % (self.decoded_file_path,
                                               self.old_package.replace(".", "/"))
        old_r_dir_2 = "%s/smali/%s/R$*.smali" % (self.decoded_file_path,
                                                 self.old_package.replace(".", "/"))
        new_r_dir = "%s/smali/%s/" % (self.decoded_file_path,
                                      self.new_package.replace(".", "/"))
        if not os.path.exists(new_r_dir):
            os.makedirs(new_r_dir)
        cmd = "mv -f %s %s" % (old_r_dir_1, new_r_dir)
        print cmd
        os.system(cmd)
        cmd = "mv -f %s %s" % (old_r_dir_2, new_r_dir)
        print cmd
        os.system(cmd)
        print "Move smali R %s to %s => OK" % (old_r_dir_1, new_r_dir)
        print

    # 修改所有中包名
    def modify_all_package(self, is_source=False):
        print
        print "========================="
        print "+++Modify all package++++"
        print "========================="
        print ">>>self.new_package: %s" % self.new_package
        print ">>>self.decoded_file_path: %s" % self.decoded_file_path
        print ">>>self.replace_smali_const_package: %s" % self.replace_smali_const_package
        print

        if self.new_package == None or len(self.new_package) == 0:
            return
        self.modify_minifest_package()
        self.modify_sxyj_package()
        # 不是项目源码,则移动smali中R文件
        if not is_source:
            self.move_smali_r()
        print "Update all package form %s to %s in %s  => OK" % (self.old_package,
                                                                 self.new_package, self.decoded_file_path)
        print

    # 修改应用名称
    def modify_app_label(self, decoded_file_path, new_app_label):
        print
        print "====================="
        print "++++++App lable++++++"
        print "====================="
        print ">>>new_app_label: %s" % new_app_label
        print ">>>replace_smali_const_label: %s" % self.replace_smali_const_label

        self.new_label = new_app_label
        mani_xml_path = "%s/AndroidManifest.xml" % decoded_file_path
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        tree = ET.parse(mani_xml_path)
        app = tree.find("application")
        k_label = "{%s}label" % schemas
        s_old_label = app.get(k_label)
        print ">>>old app name: %s" % s_old_label
        print
        # 情况一：应用名称来自于配置文件strings.xml
        if s_old_label.startswith("@string"):
            print "*****************************"
            print ">>>strings.xml"
            print "*****************************"
            label_array = s_old_label.split("/")
            if len(label_array) == 2:
                key = label_array[1]
                self.old_label = self.get_old_app_label(decoded_file_path, key)
        # 情况二：应用名称直接在AndroidManifest中修改的
        else:
            print "*****************************"
            print ">>>AndroidManifest.xml"
            print "*****************************"
            if new_app_label and cmp(new_app_label, s_old_label) != 0:
                self.old_label = s_old_label
                app.set(k_label, new_app_label)
                tree.write(mani_xml_path, "utf-8", True)
        s_new_label = app.get(k_label)
        # 修改*.smali|*.xml|*.java|*.html中的应用名称
        self.modify_sxjh_label(self.old_label, self.new_label)
        print ">>>new app name: %s" % s_new_label
        print "Update app label %s => OK" % self.decoded_file_path
        print

    # 修改strings.xml中的值
    def get_old_app_label(self, decoded_file_path, key):
        print
        print "=============================================="
        print "++++++Get old app label from strings.xml++++++"
        print "=============================================="
        print ">>>key: %s" % key
        res_path = "%s/res" % decoded_file_path
        res_array = os.listdir(res_path)
        old_app_label = None
        for item in res_array:
            if item.startswith("values"):
                strings_xml_path = "%s/res/%s/strings.xml" % (
                    decoded_file_path, item)
                # 判断文件是否存在
                if not os.path.exists(strings_xml_path):
                    print ">>>no exists: %s" % strings_xml_path
                    continue
                else:
                    print ">>>exists %s" % strings_xml_path
                # 查找数据
                tree = ET.parse(strings_xml_path)
                items = tree.findall("string")
                for item in items:
                    attrs = item.attrib
                    name = attrs["name"]
                    if name == key:
                        old_app_label = item.text
                        print "Get old app label %s => OK" % decoded_file_path
                        print "Old app label: %s" % old_app_label
                        print
                        return old_app_label
        else:
            print "Get old app label %s => Failure" % decoded_file_path

    # 更新编码版本
    @staticmethod
    def update_version_info(decode_dir, version_code="", version_name="", tool_version_java={}):
        logging.info("")
        if not version_code.isdigit():
            logging.error("VersionCode is not digit")
            sys.exit()
        # Apktool版本为2.0.0时的处理方式
        tool_version = "2.0.0"
        if tool_version_java:
            tool_version = tool_version_java.keys()[0]
        if tool_version >= '2.0.0':
            apktool_yml_path = "%s/apktool.yml" % decode_dir
            stream = file(apktool_yml_path, 'r')
            doc = yaml.load(stream)
            old_version_code = doc["versionInfo"]["versionCode"]
            old_version_name = doc["versionInfo"]["versionName"]
            logging.info("Old =>VersionCode: %s, VersionName: %s" % (old_version_code, old_version_name))
            doc["versionInfo"]["versionCode"] = version_code
            doc["versionInfo"]["versionName"] = version_name
            stream = file(apktool_yml_path, 'w')
            yaml.dump(doc, stream)
            yaml.dump(doc)
            logging.info("New =>VersionCode: %s, VersionName: %s" % (version_code, version_name))
        # Apktool版本为1.5.2时的处理方式
        else:
            mani_xml_path = "%s/AndroidManifest.xml" % decode_dir
            schemas = "http://schemas.android.com/apk/res/android"
            ET.register_namespace("android", schemas)
            tree = ET.parse(mani_xml_path)
            root = tree.getroot()
            k_version_code = "{%s}versionCode" % schemas
            k_version_name = "{%s}versionName" % schemas
            old_version_code = root.get(k_version_code)
            old_version_name = root.get(k_version_name)
            logging.info("Old =>VersionCode: %s, VersionName: %s" % (old_version_code, old_version_name))
            if version_code:
                root.set(k_version_code, str(version_code))
            if version_name:
                root.set(k_version_name, str(version_name))
            tree.write(mani_xml_path, "utf-8", True)
            new_version_code = root.get(k_version_code)
            new_version_name = root.get(k_version_name)
            logging.info("New =>VersionCode: %s, VersionName: %s" % (new_version_code, new_version_name))
        logging.info("Update %s version_code => OK" % decode_dir)

    # 修改应用图标
    def modify_app_lanucher(self, decoded_file_path, ldpi, mdpi, hdpi,
                            xhdpi, xxhdpi):
        print
        print "====================="
        print "+++Update app icon+++"
        print "====================="
        print ">>>decoded_file_path: %s" % decoded_file_path
        print ">>>ldpi: %s" % ldpi
        print ">>>mdpi: %s" % mdpi
        print ">>>hdpi: %s" % hdpi
        print ">>>xhdpi: %s" % xhdpi
        print ">>>xxhdpi: %s" % xxhdpi
        mani_xml_path = "%s/AndroidManifest.xml" % decoded_file_path
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        tree = ET.parse(mani_xml_path)
        app = tree.find("application")
        k_icon = "{%s}icon" % schemas
        s_icon = app.get(k_icon)
        icon_array = s_icon.split("/")
        new_icon_name = "%s.png" % icon_array[1]
        print ">>>New icon name: %s" % new_icon_name
        if hdpi:
            res_path = "%s/res" % decoded_file_path
            res_array = os.listdir(res_path)
            res_array = os.listdir(res_path)
            for item in res_array:
                if item.startswith("drawable"):
                    self.modify_drawable_icon(decoded_file_path, item, ldpi,
                                              mdpi, hdpi, xhdpi, xxhdpi, new_icon_name)
        else:
            print ">>>You must give me hdpi image"

    # 修改某一drawable-文件夹下的应用图标
    def modify_drawable_icon(self, decoded_file_path, drawable_file_name, ldpi,
                             mdpi, hdpi, xhdpi, xxhdpi, new_icon_name):
        icon_path = "%s/res/%s/%s" \
                    % (decoded_file_path, drawable_file_name, new_icon_name)
        if not os.path.exists(icon_path):
            print ">>>no exists: %s" % icon_path
            return
        else:
            print ">>>exists %s" % icon_path

        # ldpi
        if "ldpi" in drawable_file_name:
            if ldpi != None:
                shutil.move(ldpi, icon_path)
            else:
                shutil.copyfile(hdpi, icon_path)
        # mdpi
        elif "mdpi" in drawable_file_name:
            if mdpi != None:
                shutil.move(mdpi, icon_path)
            else:
                shutil.copyfile(hdpi, icon_path)
        # hdpi
        elif "hdpi" in drawable_file_name:
            shutil.copyfile(hdpi, icon_path)
        # xhdpi
        elif "xhdpi" in drawable_file_name:
            if xhdpi != None:
                shutil.move(xhdpi, icon_path)
            else:
                shutil.copyfile(hdpi, icon_path)
        # xxhdpi
        elif "xxhdpi" in drawable_file_name:
            if xxhdpi != None:
                shutil.move(xxhdpi, icon_path)
            else:
                shutil.copyfile(hdpi, icon_path)
        # others
        else:
            shutil.copyfile(hdpi, icon_path)
        print "Replace %s => OK" % icon_path
        print

    # 修改meta data
    def modify_meta_data(self, extra_dir, key, value):
        print
        print "====================="
        print "+++Update meta_data++"
        print "====================="
        print ">>>extra_dir: %s" % extra_dir
        print ">>>key: %s" % key
        print ">>>value: %s" % value
        mani_xml_path = "%s/AndroidManifest.xml" % extra_dir
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        tree = ET.parse(mani_xml_path)
        k_android_name = "{%s}name" % schemas
        k_android_value = "{%s}value" % schemas
        application = tree.find("application")
        items = application.findall("meta-data")
        meta_data = None
        for item in items:
            android_name = item.get(k_android_name)
            if android_name == key:
                meta_data = item
                break
        if meta_data != None:
            old_android_value = meta_data.get(k_android_value)
            print ">>>old meta-data: %s" % old_android_value
            if value:
                meta_data.set(k_android_value, str(value))
                tree.write(mani_xml_path, "utf-8", True)
            new_android_value = meta_data.get(k_android_value)
            print ">>>new meta-data: %s" % new_android_value
            print "Update meta-data %s to %s => OK" % (key, value)
            print
        else:
            print ">>>not exists meta-data %s" % key

    # 修改AndroidManifest.xml activity name
    def modify_mani_activity_name(self, extra_dir, old_activity, new_activity):
        print
        print "====================="
        print "+Update Mani activity"
        print "====================="
        mani_xml_path = "%s/AndroidManifest.xml" % extra_dir
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        tree = ET.parse(mani_xml_path)
        k_android_name = "{%s}name" % schemas
        application = tree.find("application")
        items = application.findall("activity")
        activity = None
        for item in items:
            android_name = item.get(k_android_name)
            if android_name == old_activity:
                activity = item
                break

        if activity != None:
            old_android_name = activity.get(k_android_name)
            print ">>>old activity name: %s" % old_android_name
            if new_activity:
                activity.set(k_android_name, str(new_activity))
                tree.write(mani_xml_path, "utf-8", True)
            new_android_name = activity.get(k_android_name)
            print ">>>new activity name: %s" % new_android_name
            print "Update AndroidManifest.xml activity name %s to %s => OK" % (old_activity, new_activity)
            print
        else:
            print ">>>not exists activity name %s" % old_activity

            # 修改src activity name

    def modify_src_activity_name(self, extra_dir, old_activity, new_activity):
        print
        print "====================="
        print "+Update Src activity"
        print "====================="
        # 移动文件并重命名
        old_name = "src/%s.java" % old_activity.replace(".", "/")
        new_name = "src/%s.java" % new_activity.replace(".", "/")
        old_activity_dir = os.path.join(extra_dir, old_name)
        new_activity_dir = os.path.join(extra_dir, new_name)
        dirname = os.path.dirname(new_activity_dir)
        if not os.path.exists(dirname):
            os.makedirs(dirname)
        # 移动文件并重命名
        shutil.move(old_activity_dir, new_activity_dir)
        # 重命名java文件中的import和activity class名称
        java_old_import_name = old_activity
        java_new_import_name = new_activity
        old_array = old_activity.split(".")
        java_old_class_name = old_array.pop(len(old_array) - 1)
        java_old_package_name = ".".join(str(i) for i in old_array)
        new_array = new_activity.split(".")
        java_new_class_name = new_array.pop(len(new_array) - 1)
        java_new_package_name = ".".join(str(i) for i in new_array)
        # 三个参数：分别返回1.父目录 2.所有文件夹名字（不含路径） 3.所有文件名字
        for parent, dirs, files in os.walk(self.decoded_file_path):
            for filename in files:
                path = os.path.join(parent, filename)
                if filename.endswith(".java"):
                    self.replace(
                        path, java_old_import_name, java_new_import_name)
                    self.replace(
                        path, java_old_class_name, java_new_class_name)
                    self.replace(
                        path, java_old_package_name, java_new_package_name)
        print "Update *.java activity name %s to %s => OK" % (old_activity, new_activity)
        print

    def modify_activity_name(self, extra_dir, old_activity, new_activity):
        print
        print "=========================="
        print "+Update Mani/Src activity+"
        print "=========================="
        print ">>>extra_dir: %s" % extra_dir
        print ">>>old_activity: %s" % old_activity
        print ">>>new_activity: %s" % new_activity
        print ">>>updateing"
        self.modify_mani_activity_name(extra_dir, old_activity, new_activity)
        self.modify_src_activity_name(extra_dir, old_activity, new_activity)

    # 修复style中No resource found that matches the given name
    # 'Widget.AppCompat.Base'
    def fix_styles_xml(self, styles_xml_dir):
        print
        print "==========================="
        print "++++++Fix styles.xml+++++++"
        print "==========================="
        print ">>>styles_xml_dir: %s" % styles_xml_dir
        try:
            tree = ET.parse(styles_xml_dir)
            root = tree.getroot()
            style_array = root.findall("style")
            for item in style_array:
                if not item.attrib.has_key("parent"):
                    item.attrib["parent"] = ""
            tree.write(styles_xml_dir, "utf-8", True)
            print "Fix %s => OK" % styles_xml_dir
            print
        except Exception, e:
            print "Fix %s => Failure" % styles_xml_dir
            print
            logging.error(e)

    # 获取strings.xml中的内容
    def get_strings_item_value(self, app_path, key):
        print
        print "======================================="
        print "++++++Get strings.xml item value+++++++"
        print "======================================="
        strings_xml_dir = "%s/res/values/strings.xml" % app_path
        tree = ET.parse(strings_xml_dir)
        items = tree.findall("string")
        for item in items:
            if item.attrib["name"] == key:
                value = item.text
                print "Get %s value in %s => OK" % (key, strings_xml_dir)
                print
                return value
        else:
            print "Get %s value in %s => Failure" % (key, strings_xml_dir)
            print

    # 修复AndroidManifest.xml中meta-data中name为@string/格式问题
    def fix_mani_meta_bug(self, app_path):
        print
        print "=============================================="
        print "++++++Fix AndroidManifest meta-data bug+++++++"
        print "=============================================="
        try:
            mani_xml_path = "%s/AndroidManifest.xml" % app_path
            schemas = "http://schemas.android.com/apk/res/android"
            ET.register_namespace("android", schemas)
            tree = ET.parse(mani_xml_path)
            k_android_name = "{%s}name" % schemas
            application = tree.find("application")
            meta_datas = application.findall("meta-data")
            for meta_data in meta_datas:
                android_name = meta_data.get(k_android_name)
                if android_name.startswith("@string"):
                    label_array = android_name.split("/")
                    if len(label_array) == 2:
                        key = label_array[1]
                        value_in_strings = self.get_strings_item_value(
                            app_path, key)
                        meta_data.set(k_android_name, value_in_strings)
            tree.write(mani_xml_path, "utf-8", True)
            print "Fix mani meta-data bug in %s => OK" % app_path
            print
        except Exception, e:
            print "Fix mani meta-data bug in %s => Failure" % app_path
            logging.error(e)

    # 去除AndroidManifest.xml中的activity-alias
    def del_mani_activity_alias(self, app_path):
        print
        print "===================================="
        print "++++++Del mani activity-alias+++++++"
        print "===================================="

        try:
            mani_xml_path = "%s/AndroidManifest.xml" % app_path
            schemas = "http://schemas.android.com/apk/res/android"
            ET.register_namespace("android", schemas)
            tree = ET.parse(mani_xml_path)
            application = tree.find("application")
            activity_alias_array = application.findall("activity-alias")
            for activity_alias in activity_alias_array:
                application.remove(activity_alias)
            tree.write(mani_xml_path, "utf-8", True)
            print "Del mani activity-alias in %s => OK" % app_path
            print
        except Exception, e:
            print "Del mani activity-alias in %s => Failure" % app_path
            logging.error(e)

    # 编译前的修复工作
    def pre_build(self, app_path):
        print
        print "======================"
        print "++++++Pre build+++++++"
        print "======================"

        # 修复styles.xml中的问题
        res_dir = "%s/res" % app_path
        res_array = os.listdir(res_dir)
        for res_item in res_array:
            if res_item == "values" or res_item == "values-v14":
                styles_xml_dir = "%s/%s/styles.xml" % (res_dir, res_item)
                if os.path.exists(styles_xml_dir):
                    self.fix_styles_xml(styles_xml_dir)

        # 修复AndroidManifest.xml中meta-data中name为@string/格式问题
        self.fix_mani_meta_bug(app_path)

        # 去除AndroidManifest.xml中的activity-alias
        self.del_mani_activity_alias(app_path)
