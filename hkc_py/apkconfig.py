#!/usr/bin/env python
# -*- coding: UTF-8 -*-
__author__ = 'Tu'
from apkutils import *
from netutils import *


class ApkConfig(object):
    # 广告渠道APK,可为网络apk地址,必须
    original_apk = None
    # Apktool.jar版本和java路径, 必须
    tool_version_java = {}
    # 第三方项目源码编译成APK模式, 默认为release,可选(release, debug)
    extra_build_mode = "debug"
    # 第三方项目源码压缩包,可为网络项目zip包,必须
    extra_zip = None
    # 第三方项目源码压缩包, 需要合并的资源前缀, 必须
    extra_res_prefix = None
    # 第三方项目源码压缩包, 需要替换的activity名称, 必须
    extra_replace_activity = None
    # 第三方项目中需要替换的meta-data信息, 可选
    extra_meta_data = {}
    # 合并extra中的第三方SDK
    extra_third_sdk = {}
    # 重新编译好后的apk名称, 不填则自动生成, 可选
    build_apk = None
    # 是否替换  *.smali中的包名, 可选
    replace_smali_const_package = True
    # 是否替换 *.smali中的应用名
    replace_smali_const_label = True
    # 签名信息
    keystore = {}
    # 坑应用APK在应用宝中的信息，可选
    hole_url = None
    # 坑渠道APK,可为网络apk地址, 可选
    hole_apk = None
    # 新的应用名称
    new_app_label = None
    # 各分辨率下的新应用图标，默认用坑应用图标,该图标给了则用新图标,可为本地路径和网路路径,可选
    xxhdpi = None
    xhdpi = None
    hdpi = None
    mdpi = None
    ldpi = None
    # 编码版本操作,或者具体编码版本
    version_code = None
    # 版本号操作
    version_name = None
    # 新的包名,不能出现java中的关键字
    new_package = None

    def parser_rehole_xml(self, apk):
        # 广告渠道APK
        self.original_apk = apk.findtext("original_apk")

        # Apktool.jar版本和java路径, 必须
        tool_version = apk.find("tool_version_java")
        key = tool_version.get("name")
        value = tool_version.text
        self.tool_version_java[key] = value

        # 第三方项目源码编译成APK模式, 默认为release,可选(release, debug)
        self.extra_build_mode = apk.findtext("extra_build_mode")

        # 第三方项目压缩包
        self.extra_zip = apk.findtext("extra_zip")

        # 第三方项目压缩包,需要合并的资源前缀
        self.extra_res_prefix = apk.findtext("extra_res_prefix")

        # extra_replace_activity
        self.extra_replace_activity = apk.findtext("extra_replace_activity")

        # extra_meta_data
        tmp_meta_data_array = apk.findall("extra_meta_data")
        for extra_meta_data in tmp_meta_data_array:
            key = extra_meta_data.get("name")
            value = extra_meta_data.text
            self.extra_meta_data[key] = value

        # 第三方SDK的包名
        tmp_third_sdk_array = apk.find("extra_third_sdk")
        if tmp_third_sdk_array is not None:
            sdk_packages = tmp_third_sdk_array.findall("sdk_package")
            for sdk_package in sdk_packages:
                key = sdk_package.get("name")
                value = sdk_package.text
                self.extra_third_sdk[key] = value

        # 重新编译好后的apk名称
        self.build_apk = apk.findtext("build_apk")

        # 是否替换*.smali中包名常量
        tmp = apk.findtext("replace_smali_const_package")
        if tmp is not None and tmp == "false":
            self.replace_smali_const_package = False

        # 是否替换*.smali中应用名常量
        tmp = apk.findtext("replace_smali_const_label")
        if tmp is not None and tmp == "false":
            self.replace_smali_const_label = False

        # 签名信息
        new_keystore = apk.find("keystore")
        if new_keystore:
            self.keystore["name"] = new_keystore.findtext("name")
            self.keystore["pwd"] = new_keystore.findtext("pwd")

        hole_info = {}
        # 坑应用APK在应用宝中的信息
        hole_url = apk.findtext("hole_url")
        if hole_url:
            logging.info("---------------------")
            logging.info(">>>Use hole url info ")
            logging.info("---------------------")
            # 获取坑信息
            hole_info = NetUtils.get_appinfo_by_apk_url(hole_url)
            # 从网络下载坑icon图标
            filename = "%s_%s_xxhdpi_144" % (
                hole_info["package"], hole_info["versionCode"])
            hdpi = hole_info["icon"]
            hdpi = ApkUtils.get_original(hdpi, "png", filename)
            hole_info["icon"] = hdpi
            # 编码版本+1
            versionCode = hole_info["versionCode"]
            i_versionCode = int(versionCode)
            i_versionCode += 1
            hole_info["versionCode"] = str(i_versionCode)

        # 坑渠道APK
        hole_apk = apk.findtext("hole_apk")
        if hole_apk:
            logging.info("---------------------")
            logging.info(">>>Use hole apk info ")
            logging.info("---------------------")
            try:
                # 清除坑之前的icon图标
                hdpi = "icon" in hole_info and hole_info["icon"] or None
                if hdpi:
                    os.remove(hdpi)
                # 下载坑APK
                hole_apk = ApkUtils.get_original(hole_apk, "apk")
                # 获取坑信息
                hole_info = ApkUtils.info(hole_apk)
                hdpi = hole_info["icon"]
                # 从坑APK中获取新的坑icon图标
                filename = "%s_%s_xxhdpi_144" % (
                    hole_info["package"], hole_info["versionCode"])
                hdpi = ApkUtils.get_icon_from_apk(
                    hole_apk, hole_info["icon"], filename)
                hole_info["icon"] = hdpi
                # 编码版本+1
                versionCode = hole_info["versionCode"]
                i_versionCode = int(versionCode)
                i_versionCode += 1
                hole_info["versionCode"] = str(i_versionCode)
            except Exception, ex:
                raise ex
            finally:
                # 清理坑APK
                os.remove(hole_apk)

        # new_hole_info
        new_hole_info = apk.find("new_hole_info")
        if new_hole_info:
            logging.info("--------------------")
            logging.info(">>>Use new apk info ")
            logging.info("--------------------")
            # 清除坑之前的icon图标
            hdpi = "icon" in hole_info and hole_info["icon"] or None
            if hdpi:
                os.remove(hdpi)
            # 新的应用名称
            new_app_label = new_hole_info.findtext("new_app_label")
            if new_app_label:
                hole_info["label"] = new_app_label
            # hdpi
            hdpi = new_hole_info.findtext("hdpi")
            if hdpi:
                hole_info["icon"] = hdpi
            # xhdpi
            xhdpi = new_hole_info.findtext("xhdpi")
            if xhdpi:
                hole_info["xhdpi"] = xhdpi
            # 编码版本操作
            version_code = new_hole_info.findtext("version_code")
            if version_code:
                hole_info["versionCode"] = version_code
            # 版本号操作
            version_name = new_hole_info.findtext("version_name")
            if version_name:
                hole_info["versionName"] = version_name
            # 新的包名
            new_package = new_hole_info.findtext("new_package")
            if new_package:
                hole_info["package"] = new_package
            # 获取新的坑icon图标
            # hdpi
            hdpi = None
            if "icon" in hole_info:
                filename = "%s_%s_hdpi_72" % (
                    hole_info["package"], hole_info["versionCode"])
                hdpi = hole_info["icon"]
                hdpi = ApkUtils.get_original(
                    hdpi, "png", filename)
                hole_info["icon"] = hdpi
            # xhdpi
            xhdpi = None
            if "xhdpi" in hole_info:
                filename = "%s_%s_xhdpi_96" % (
                    hole_info["package"], hole_info["versionCode"])
                xhdpi = hole_info["xhdpi"]
                xhdpi = ApkUtils.get_original(
                    xhdpi, "png", filename)
                hole_info["xhdpi"] = xhdpi

        if "label" in hole_info:
            self.new_app_label = hole_info["label"]
        # 各分辨率下的新应用图标，默认用坑应用图标,该图标给了则用新图标,可为本地路径和网路路径,可选
        if "icon" in hole_info:
            self.hdpi = hole_info["icon"]
        # 编码版本操作,或者具体编码版本
        if "versionCode" in hole_info:
            self.version_code = hole_info["versionCode"]
        # 版本号操作
        if "versionName" in hole_info:
            self.version_name = hole_info["versionName"]
        # 新的包名,不能出现java中的关键字
        if "package" in hole_info:
            self.new_package = hole_info["package"]
        self.__process_res()

    def parser_rebuild_xml(self, apk):
        # 广告渠道APK
        self.original_apk = apk.findtext("original_apk")
        # 签名信息
        new_keystore = apk.find("keystore")
        if new_keystore:
            self.keystore["name"] = new_keystore.findtext("name")
            self.keystore["pwd"] = new_keystore.findtext("pwd")
        # 重新编译好后的apk名称
        self.build_apk = apk.findtext("build_apk")
        # Apktool.jar版本和java路径, 必须
        tool_version = apk.find("tool_version_java")
        key = tool_version.get("name")
        value = tool_version.text
        self.tool_version_java[key] = value
        # 新的应用名称
        self.new_app_label = apk.findtext("new_app_label")
        # ldpi
        self.ldpi = apk.findtext("ldpi")
        # mdpi
        self.mdpi = apk.findtext("mdpi")
        # hdpi
        self.hdpi = apk.findtext("hdpi")
        # xhdpi
        self.xhdpi = apk.findtext("xhdpi")
        # xxhdpi
        self.xxhdpi = apk.findtext("xxhdpi")
        # 编码版本操作
        self.version_code = apk.findtext("version_code")
        # 版本号操作
        self.version_name = apk.findtext("version_name")
        # 包名
        self.new_package = apk.findtext("new_package")
        # meta-data
        extra_meta_data_array = apk.findall("meta_data")
        for extra_meta_data in extra_meta_data_array:
            key = extra_meta_data.get("name")
            value = extra_meta_data.text
            self.extra_meta_data[key] = value
        self.__process_res();

    # 处理移动资源到打包目录
    def __process_res(self):
        if not self.extra_build_mode or self.extra_build_mode not in ["debug", "release"]:
            self.extra_build_mode = "debug"
        if self.original_apk:
            self.original_apk = ApkUtils.get_original(self.original_apk, "apk")
        if self.extra_zip:
            self.original_apk = ApkUtils.get_original(self.original_apk, "zip")
        if self.ldpi:
            self.ldpi = ApkUtils.get_original(self.ldpi, "png")
        if self.mdpi:
            self.mdpi = ApkUtils.get_original(self.mdpi, "png")
        if self.hdpi:
            self.hdpi = ApkUtils.get_original(self.hdpi, "png")
        if self.xhdpi:
            self.xhdpi = ApkUtils.get_original(self.xhdpi, "png")
        if self.xhdpi:
            self.xxhdpi = ApkUtils.get_original(self.xxhdpi, "png")

    def desc(self):
        # 广告渠道APK,可为网络apk地址,必须
        logging.info("original_apk => %s" % self.original_apk)
        # Apktool.jar版本,可选
        logging.info("tool_version_java => %s" % self.tool_version_java)
        for k, v in self.tool_version_java.items():
            logging.info("tool_version_java %s => %s" % (k, v))
        # 第三方项目源码压缩包,可为网络项目zip包,必须
        logging.info("extra_zip => %s" % self.extra_zip)
        # 第三方项目源码压缩包, 需要合并的资源前缀, 必须
        logging.info("extra_res_prefix => %s" % self.extra_res_prefix)
        # 第三方项目源码压缩包, 需要替换的activity名称, 必须
        logging.info("extra_replace_activity => %s" % self.extra_replace_activity)
        # 第三方项目中需要替换的meta-data信息, 可选
        logging.info("extra_meta_data")
        for k, v in self.extra_meta_data.items():
            logging.info("extra_meta_data %s => %s" % (k, v))
        # 合并extra中的第三方SDK
        logging.info("extra_third_sdk")
        for k, v in self.extra_third_sdk.items():
            logging.info("extra_third_sdk %s => %s" % (k, v))
        # 重新编译好后的apk名称, 不填则自动生成, 可选
        logging.info("build_apk => %s" % self.build_apk)
        # 是否替换  *.smali中的包名, 可选
        logging.info("replace_smali_const_package => %s" % self.replace_smali_const_package)
        # 是否替换 *.smali中的应用名
        logging.info("replace_smali_const_label => %s" % self.replace_smali_const_label)
        # 签名信息
        logging.info("keystore")
        for k, v in self.keystore.items():
            logging.info("keystore %s => %s" % (k, v))
        # 坑应用APK在应用宝中的信息，可选
        logging.info("hole_url => %s" % self.hole_url)
        # 坑渠道APK,可为网络apk地址, 可选
        logging.info("hole_apk => %s" % self.hole_apk)
        # 新的应用名称
        logging.info("new_app_label => %s" % self.new_app_label)
        # 各分辨率下的新应用图标，默认用坑应用图标,该图标给了则用新图标,可为本地路径和网路路径,可选
        logging.info("xxhdpi => %s" % self.xxhdpi)
        logging.info("xhdpi => %s" % self.xhdpi)
        logging.info("hdpi => %s" % self.hdpi)
        logging.info("hdpi => %s" % self.mdpi)
        logging.info("ldpi => %s" % self.ldpi)
        # 编码版本操作,或者具体编码版本
        logging.info("version_code => %s" % self.version_code)
        # 版本号操作
        logging.info("version_name => %s" % self.version_name)
        # 新的包名,不能出现java中的关键字
        logging.info("new_package => %s" % self.new_package)
