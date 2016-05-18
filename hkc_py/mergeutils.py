#!/usr/bin/env python
# -*- coding: UTF-8 -*-

from apkutils import *


try:
    import xml.etree.cElementTree as ET
except ImportError:
    import xml.etree.ElementTree as ET

logging.basicConfig(level=logging.DEBUG,
                    format='%(levelname)s %(asctime)s %(filename)s %(funcName)s [line:%(lineno)d] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')


class MergeUtils():
    extra_apk_dir = None
    decode_dir = None
    launchable_activity = None
    new_package = None
    old_package = None
    extra_decode_dir = None
    extra_third_sdk = None
    extra_res_prefix = None
    tool_version_java = None

    def __init__(self, extra_apk_dir, decode_dir, launchable_activity, old_package, apk_config):
        print
        print "============================"
        print "+++++++MergeUtils Init++++++"
        print "============================"
        self.extra_apk_dir = extra_apk_dir
        self.decode_dir = decode_dir
        self.launchable_activity = launchable_activity
        self.new_package = apk_config.new_package
        self.old_package = old_package
        self.extra_third_sdk = apk_config.extra_third_sdk
        self.extra_res_prefix = apk_config.extra_res_prefix
        self.tool_version_java = apk_config.tool_version_java

        print ">>>extra_apk_dir: %s" % self.extra_apk_dir
        print ">>>dest_dir: %s" % self.decode_dir
        print ">>>launchable_activity: %s" % self.launchable_activity
        print ">>>new_package: %s" % self.new_package
        print ">>>old_package: %s" % self.old_package
        print ">>>extra_third_sdk: %s" % self.extra_third_sdk
        print ">>>extra_res_prefix: %s" % self.extra_res_prefix
        print ">>>tool_version_java: %s" % self.tool_version_java
        print

    def delete_dest_laucher_action_category(self):
        dest_mani = "%s/AndroidManifest.xml" % self.decode_dir
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        k_android_name = "{%s}name" % schemas
        tree = ET.parse(dest_mani)
        application = tree.find("application")
        activitys = application.findall("activity")
        for activity in activitys:
            intent_filters = activity.findall("intent-filter")
            for intent_filter in intent_filters:
                action_main = None
                actions = intent_filter.findall("action")
                for action in actions:
                    android_name = action.get(k_android_name)
                    if android_name == "android.intent.action.MAIN":
                        action_main = action
                category_launcher = None
                categories = intent_filter.findall("category")
                for category in categories:
                    android_name = category.get(k_android_name)
                    if android_name == "android.intent.category.LAUNCHER":
                        category_launcher = category
                if action_main != None and category_launcher != None:
                    intent_filter.remove(action_main)
                    intent_filter.remove(category_launcher)
        tree.write(dest_mani, "utf-8", True)
        print "Delete dest launcher action category %s => OK" % dest_mani
        print

    def delete_extra_launch_activity(self):
        extra_mani = "%s/AndroidManifest.xml" % self.extra_decode_dir
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        k_android_name = "{%s}name" % schemas
        tree = ET.parse(extra_mani)
        application = tree.find("application")
        activitys = application.findall("activity")
        delete_activity = None
        for activity in activitys:
            android_name = activity.get(k_android_name)
            if android_name == self.launchable_activity:
                delete_activity = activity
                break
        if delete_activity != None:
            application.remove(delete_activity)
        tree.write(extra_mani, "utf-8", True)
        print "Delete extra launcher activity %s => OK" % extra_mani
        print

    def update_dest_mani_permission(self, new_permissions):
        print
        print "================================="
        print "++++Merge mani permission+++++++"
        print "================================="
        dest_mani = "%s/AndroidManifest.xml" % self.decode_dir
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        k_android_name = "{%s}name" % schemas
        tree = ET.parse(dest_mani)
        root = tree.getroot()
        old_permissions = root.findall("uses-permission")
        # 添加新的
        for new_permission in new_permissions:
            # 判断是否存在相同的
            new_permission_android_name = new_permission.get(k_android_name)
            exist_permission = False
            for old_permission in old_permissions:
                old_permission_android_name = old_permission.get(
                    k_android_name)
                if new_permission_android_name == old_permission_android_name:
                    exist_permission = True
                    break
            # 不存在则添加
            if not exist_permission:
                root.append(new_permission)
        tree.write(dest_mani, "utf-8", True)
        print "Update mani permissions %s => OK" % dest_mani
        print

    def update_dest_mani_application(self, new_items, tag):
        print
        print "================================="
        print "++++Merge mani application+++++++"
        print "================================="
        print ">>>tag: %s" % tag
        dest_mani = "%s/AndroidManifest.xml" % self.decode_dir
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        k_android_name = "{%s}name" % schemas
        tree = ET.parse(dest_mani)
        application = tree.find("application")
        old_items = application.findall(tag)
        delete_items = []
        # 检索出android:name值相同的
        for old_item in old_items:
            old_item_android_name = old_item.get(k_android_name)
            for new_item in new_items:
                new_item_android_name = new_item.get(k_android_name)
                if old_item_android_name == new_item_android_name:
                    delete_items.append(old_item)
        # 去除android:name值相同的
        for delete_item in delete_items:
            application.remove(delete_item)
        tree.write(dest_mani, "utf-8", True)
        # 添加新的
        for new_item in new_items:
            application.append(new_item)
        tree.write(dest_mani, "utf-8", True)
        print "Update mani application tag %s %s => OK" % (tag, dest_mani)
        print

    def merge_all_mani(self):
        print
        print "========================="
        print "++++Merge all mani+++++++"
        print "========================="
        extra_mani = "%s/AndroidManifest.xml" % self.extra_decode_dir
        schemas = "http://schemas.android.com/apk/res/android"
        ET.register_namespace("android", schemas)
        tree = ET.parse(extra_mani)
        root = tree.getroot()
        permissions = root.findall("uses-permission")
        self.update_dest_mani_permission(permissions)
        application = tree.find("application")
        tags = set()
        for child in application:
            tags.add(child.tag)
        print ">>>Tags: %s" % tags
        for tag in tags:
            new_items = application.findall(tag)
            self.update_dest_mani_application(new_items, tag)

    def merge_mani(self):
        print
        print "====================="
        print "++++Merge Mani+++++++"
        print "====================="
        old_mani = "%s/AndroidManifest.xml" % self.extra_decode_dir
        new_mani = "%s/AndroidManifest.xml" % self.decode_dir
        # 删除原项目AndroidManifest.xml启动标识
        self.delete_dest_laucher_action_category()
        # 删除Extra项目中的多余的Launch Activity
        self.delete_extra_launch_activity()
        # 整合AndroidManifest.xml
        self.merge_all_mani()
        print "Merge AndroidManifest.xml %s to %s => OK" % (old_mani, new_mani)
        print

    def merge_assets(self):
        print
        print "====================="
        print "++++Merge assets+++++"
        print "====================="
        old_assets = "%s/assets/%s*" % (self.extra_decode_dir, self.extra_res_prefix)
        new_assets = "%s/assets/" % self.decode_dir
        if not os.path.exists(new_assets):
            os.makedirs(new_assets)
        cmd = "cp -f %s %s" % (old_assets, new_assets)
        print cmd
        os.system(cmd)
        print "Merge assets %s to %s => OK" % (old_assets, new_assets)
        print

    def merge_lib(self):
        print
        print "====================="
        print "++++Merge lib++++++++"
        print "====================="
        old_lib = "%s/lib" % self.extra_decode_dir
        new_lib = "%s/lib" % self.decode_dir
        if not os.path.exists(old_lib):
            print ">>>No found %s" % old_lib
            return
        abilist = os.listdir(old_lib)
        print ">>>%s" % abilist
        for abi in abilist:
            abidir = os.path.join(old_lib, abi)
            if os.path.exists(abidir) and os.path.isdir(abidir):
                solist = os.listdir(abidir)
                print ">>>%s %s" % (abi, solist)
                for so in solist:
                    old_so = "%s/%s/*" % (old_lib, abi)
                    new_so = "%s/%s/" % (new_lib, abi)
                    if not os.path.exists(new_so):
                        os.makedirs(new_so)
                    cmd = "cp -f %s %s" % (old_so, new_so)
                    print cmd
                    os.system(cmd)
        print "Merge lib %s to %s => OK" % (old_lib, new_lib)
        print

    # 合并第三方jar包，前提是原包中没有相同的
    def merge_third_smali(self, third_package, force=False):
        print
        print "==========================="
        print "++++Merge third smali++++++"
        print "==========================="
        old_smali_dir = "%s/smali/%s/" % (self.extra_decode_dir,
                                          third_package.replace(".", "/"))
        new_smali_dir = "%s/smali/%s/" % (self.decode_dir,
                                          third_package.replace(".", "/"))
        # 判断Dest中是否有第三方SDK
        if os.path.exists(new_smali_dir):
            print ">>>Dest smali exists third sdk %s" % third_package
            # 是否强制覆盖
            if force:
                print ">>>Dest smali can forced to replace %s" % third_package
            else:
                print ">>>Dest smali cann't forced to replace %s" % third_package
                return
        # 判断Extra中是否有第三方SDK, 不存在则不操作
        if not os.path.exists(old_smali_dir):
            print ">>>Extra smali no exists third sdk %s" % third_package
            return
        old_smali = "%s/smali/%s/*" % (self.extra_decode_dir,
                                       third_package.replace(".", "/"))
        new_smali = "%s/smali/%s/" % (self.decode_dir,
                                      third_package.replace(".", "/"))
        if not os.path.exists(new_smali):
            os.makedirs(new_smali)
        cmd = "cp -Rf %s %s" % (old_smali, new_smali)
        print cmd
        os.system(cmd)

    def merge_smali(self):
        print
        print "====================="
        print "++++Merge smali++++++"
        print "====================="
        # 合并
        old_smali = "%s/smali/%s/*" % (self.extra_decode_dir,
                                       self.old_package.replace(".", "/"))
        new_smali = "%s/smali/%s/" % (self.decode_dir,
                                      self.old_package.replace(".", "/"))
        if not os.path.exists(new_smali):
            os.makedirs(new_smali)
        cmd = "cp -Rf %s %s" % (old_smali, new_smali)
        print cmd
        os.system(cmd)
        # 合并第三方jar包
        if self.extra_third_sdk != None:
            for key, value in self.extra_third_sdk.items():
                force = False
                if value == "true":
                    force = True
                self.merge_third_smali(key, force)
        print "Merge smali %s to %s => OK" % (old_smali, new_smali)
        print

    def merge_res_start_hkc(self):
        print
        print "=============================="
        print "++++Merge Res start hkc+++++++"
        print "=============================="
        extra_res_dir = "%s/res" % self.extra_decode_dir
        dest_res_dir = "%s/res" % self.decode_dir
        res_array = os.listdir(extra_res_dir)
        for res in res_array:
            # 查询target项目对应的文件目录下有无需要移动的文件
            sub_res_dir = os.path.join(extra_res_dir, res)
            if os.path.isdir(sub_res_dir):
                xml_array = os.listdir(sub_res_dir)
                for filename in xml_array:
                    if filename.startswith(self.extra_res_prefix):
                        extra_cp_dir = "%s/%s/%s" % (extra_res_dir,
                                                     res, filename)
                        dest_cp_dir = "%s/%s/" % (dest_res_dir, res)
                        if not os.path.exists(dest_cp_dir):
                            os.makedirs(dest_cp_dir)
                        cmd = "cp -f %s %s" % (extra_cp_dir, dest_cp_dir)
                        print cmd
                        os.system(cmd)
        print "Merge res startwith %s %s to %s => OK" % (self.extra_res_prefix, extra_res_dir, dest_res_dir)
        print

    def update_asset_hkc_res(self, new_type, key, new_value):
        print
        print "===================================="
        print "++++Update assets hkc_res.xml+++++++"
        print "===================================="
        hkc_res_name = "%sres.xml" % self.extra_res_prefix
        hkc_res_xml = "%s/assets/%s" % (self.extra_decode_dir, hkc_res_name)
        tree = ET.parse(hkc_res_xml)
        root = tree.getroot()
        items = root.findall("public")
        key_item = None
        for item in items:
            attrs = item.attrib
            name = attrs["name"]
            ttype = attrs["type"]
            if name == key and ttype == new_type:
                key_item = item
                break

        if key_item != None:
            old_value = key_item.attrib["id"]
            print ">>>old value: %s" % old_value
            if new_value and cmp(new_value, old_value):
                key_item.attrib["id"] = new_value
                tree.write(hkc_res_xml, "utf-8", True)
                new_value = key_item.text
                print ">>>new value: %s" % new_value
                print "Update value %s => OK" % hkc_res_xml
                print
        else:
            print ">>>no exists key %s in %s" % (key, hkc_res_xml)

    def get_hex_id(self, max_dict, ttype):
        # other
        if ttype not in max_dict:
            max_id = max_dict["other"]
            max_id = int("0x0000ffff", 16) | max_id
            logging.info(ttype + " => " + hex(max_id))
            max_dict[ttype] = max_id
            other_max = max_id + int("0x00010000", 16)
            max_dict["other"] = other_max
            logging.info("other => " + hex(other_max))
        # normal
        max_id = max_dict[ttype]
        max_id = max_id + 1
        max_dict[ttype] = max_id
        hex_id = hex(max_id)
        return hex_id

    def ids_append_res(self, name, dest_one_value):
        print
        print "================================"
        print "++++Ids append res+++++++"
        print "================================"
        ids_xml = "%s/ids.xml" % os.path.dirname(dest_one_value)
        tree = ET.parse(ids_xml)
        root = tree.getroot()
        items = root.findall("item")
        has_name = False
        for item in items:
            attrs = item.attrib
            old_name = attrs["name"]
            if old_name == name:
                has_name = True
                break
        if not has_name:
            id_item = ET.Element("item", {"type": "id", "name": name})
            comment = ET.Comment("\r\n")
            id_item.text = "false"
            root.append(id_item)
            root.append(comment)
            tree.write(ids_xml, "utf-8", True)

    # 获取各类别最大的值
    def get_type_max_id(self, tree):
        print
        print "================================"
        print "++++++++Get type max id+++++++++"
        print "================================"
        items = tree.findall("public")
        publics_dict = {}
        for item in items:
            ttype = item.attrib["type"]
            tid = int(item.attrib["id"], 16)
            if publics_dict.has_key(ttype):
                publics_dict[ttype].append(tid)
            else:
                ids_array = []
                ids_array.append(tid)
                publics_dict[ttype] = ids_array
        # Max
        max_dict = {}
        for key in publics_dict.keys():
            values = publics_dict.get(key)
            max_value = 0
            for value in values:
                max_value = max(max_value, value)
            max_dict[key] = max_value
        # Min
        min_array = []
        for key in publics_dict.keys():
            values = publics_dict.get(key)
            min_value = int("0xffffffff", 16)
            for value in values:
                min_value = min(min_value, value)
            min_array.append(min_value)
        print ">>>min_array: %s" % min_array
        # Other
        other_max = 0
        for value in min_array:
            other_max = max(other_max, value)
        other_max = int("0x0000ffff", 16) + other_max
        print ">>>other_max: %s" % other_max
        max_dict["other"] = other_max
        print "-------------------Max hex-------------------"
        for (k, v) in max_dict.items():
            print ">>>%s => %s" % (k, hex(v))
        print "-------------------Max hex-------------------"
        return max_dict

    def merge_public_xml_item(self, extra_items, dest_one_value):
        print
        print "================================"
        print "++++Merge public xml item+++++++"
        print "================================"
        print ">>>dest_one_value: %s" % dest_one_value
        tree = ET.parse(dest_one_value)
        root = tree.getroot()
        dest_items = list(root)
        # 检索要删除的item
        extra_delete_items = []
        for dest_item in dest_items:
            dest_name = dest_item.get("name")
            for extra_item in extra_items:
                extra_name = extra_item.get("name")
                if dest_name == extra_name:
                    extra_delete_items.append(extra_item)
                    break
        # 删除extra_items中与dest中相同的item
        for extra_delete_item in extra_delete_items:
            extra_items.remove(extra_delete_item)
        # 获取各类别最大的值
        max_dict = self.get_type_max_id(tree)
        # 整合
        app_ids = []
        for extra_item in extra_items:
            ttype = extra_item.attrib["type"]
            name = extra_item.attrib["name"]
            tid = extra_item.attrib["id"]
            # 过滤数据
            if ttype == "drawable" and not name.startswith(self.extra_res_prefix):
                continue
            elif ttype == "id" and not name.startswith(self.extra_res_prefix):
                continue
            elif ttype == "layout" and not name.startswith(self.extra_res_prefix):
                continue
            elif ttype == "string" and not name.startswith(self.extra_res_prefix):
                continue
            hex_id = self.get_hex_id(max_dict, ttype)
            print ">>>type=%s name=%s old id= %s" % (ttype, name, tid)
            print ">>>type=%s name=%s new id= %s" % (ttype, name, hex_id)
            self.update_asset_hkc_res(ttype, name, hex_id)
            extra_item.attrib["id"] = hex_id
            root.append(extra_item)
            # 以ID的形式，向public.xml中追加资源
            if ttype == "drawable" or ttype == "layout":
                app_ids.append(extra_item)
        tree.write(dest_one_value, "utf-8", True)

        # 追加资源
        # for app_id in app_ids:
        # 以ID的形式，向public.xml中追加资源
        # name = app_id.attrib["name"]
        # hex_id = self.get_hex_id(max_dict, "id")
        # id_item = ET.Element(
        # "public", {"type": "id", "name": name, "id": hex_id})
        # comment = ET.Comment("\r\n")
        # root.append(id_item)
        # root.append(comment)
        # 以ID的形式，向ids.xml中追加资源
        #     self.ids_append_res(name, dest_one_value)
        # tree.write(dest_one_value, "utf-8", True)

    def merge_public_xml(self, extra_one_value, dest_one_value):
        print
        print "==========================="
        print "++++Merge Public.xml+++++++"
        print "==========================="
        print ">>>extra_one_value: %s" % extra_one_value
        print ">>>dest_one_value: %s" % dest_one_value
        tree = ET.parse(extra_one_value)
        root = tree.getroot()
        extra_items = list(root)
        self.merge_public_xml_item(extra_items, dest_one_value)
        print "Merge public.xml  %s to %s => OK" % (extra_one_value, dest_one_value)
        print

    def merge_value_xml_item(self, extra_items, dest_one_value):
        print
        print "==================================="
        print "+++++++Merge value xml item +++++++"
        print "==================================="
        print ">>>dest_one_value: %s" % dest_one_value
        tree = ET.parse(dest_one_value)
        root = tree.getroot()
        dest_items = list(root)
        # 添加新的item
        for extra_item in extra_items:
            extra_name = extra_item.get("name")
            extra_tag = extra_item.tag
            exist_name = False
            # 监测dest中是否存在
            for dest_item in dest_items:
                dest_name = dest_item.get("name")
                dest_tag = dest_item.tag
                if extra_name == dest_name and extra_tag == dest_tag:
                    exist_name = True
                    break
            # 不存在，则添加
            if not exist_name:
                root.append(extra_item)
        tree.write(dest_one_value, "utf-8", True)

    def merge_value_xml(self, extra_one_value, dest_one_value):
        print
        print "=============================="
        print "+++++++Merge value xml +++++++"
        print "=============================="
        print ">>>extra_one_value: %s" % extra_one_value
        print ">>>dest_one_value: %s" % dest_one_value
        tree = ET.parse(extra_one_value)
        root = tree.getroot()
        extra_items = list(root)
        self.merge_value_xml_item(extra_items, dest_one_value)
        print "Merge values  %s to %s => OK" % (extra_one_value, dest_one_value)
        print

    def merge_res_one_value(self, old_values, new_values):
        print
        print "=============================="
        print "++++Merge res one value+++++++"
        print "=============================="
        values = os.listdir(old_values)
        for value in values:
            old_one_value = "%s/%s" % (old_values, value)
            new_one_value = "%s/%s" % (new_values, value)
            # 目标文件夹不存在相同的资源，直接拷贝
            if not os.path.exists(new_one_value):
                dirname = os.path.dirname(new_one_value)
                if not os.path.exists(dirname):
                    os.makedirs(dirname)
                cmd = "cp -f %s %s" % (old_one_value, new_one_value)
                print cmd
                os.system(cmd)
            # 目标文件是public.xml，特殊处理
            elif value == "public.xml":
                self.merge_public_xml(old_one_value, new_one_value)
            # 直接合并
            elif value.endswith(".xml"):
                self.merge_value_xml(old_one_value, new_one_value)

    def merge_res_values(self):
        print
        print "==========================="
        print "++++Merge res values+++++++"
        print "==========================="
        old_res = "%s/res" % self.extra_decode_dir
        new_res = "%s/res" % self.decode_dir
        res_array = os.listdir(old_res)
        for res in res_array:
            if res.startswith("values"):
                old_values = "%s/%s" % (old_res, res)
                new_values = "%s/%s" % (new_res, res)
                self.merge_res_one_value(old_values, new_values)
        print "Merge values  %s to %s => OK" % (old_res, new_res)
        print

    def clean(self):
        print
        print "========================"
        print "++++++Merge Clean+++++++"
        print "========================"
        # 清理extra APK
        cmd = "rm %s" % self.extra_apk_dir
        print cmd
        os.system(cmd)
        # 清理extra APK反编译后文件夹
        cmd = "rm -rf %s" % self.extra_decode_dir
        print cmd
        os.system(cmd)
        print "Clean %s => OK" % self.extra_decode_dir
        print

    def merge_extra(self):
        try:
            # 获取extra apk信息
            ApkUtils.info(self.extra_apk_dir)
            # 反编译extra apk
            self.extra_decode_dir = ApkUtils.decode(self.extra_apk_dir,
                                                    os.path.dirname(self.extra_apk_dir), self.tool_version_java)
            # 整合AndroidManifest.xml
            self.merge_mani()
            # 整合lib
            self.merge_lib()
            # 整合已hkc_开头的资源文件
            self.merge_res_start_hkc()
            # 整合res/values*
            self.merge_res_values()
            # 整合smali
            self.merge_smali()
            # 整合assets
            self.merge_assets()
        except Exception, e:
            logging.error(e)
            sys.exit()
        finally:
            # 清理资源
            self.clean()
