#!/usr/bin/env pyhton
# -*- coding: UTF-8 -*-
import os
import sys
import httplib
import urllib
import json
import re
import logging

logging.basicConfig(level=logging.DEBUG,
                    format='%(levelname)s %(asctime)s %(filename)s %(funcName)s [line:%(lineno)d] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')

''' 新的抓取地址：http://a.app.qq.com/o/ajax/micro/AppDetail?pkgname=com.blsm.sft.fresh（直接返回json数据）
 应用宝抓取数据的url '''
myapp_api_url = {
    'host': 'android.myapp.com', 'app_detail': '/myapp/detail.htm'}

default_encoding = 'utf-8'
if sys.getdefaultencoding() != default_encoding:
    reload(sys)
    sys.setdefaultencoding(default_encoding)


class NetUtils(object):
    # 获取应用的详情
    @staticmethod
    def get_appinfo_from_myapp_by_package(package):
        logging.info("")
        url = "%s?%s" % (myapp_api_url['app_detail'], urllib.urlencode({'apkName': package}))
        conn = httplib.HTTPConnection(myapp_api_url['host'])
        conn.request("GET", url)
        res = conn.getresponse()

        # 如果数据返回出错，那么return 空
        if res.status != 200:
            logging.error("Get app info from myapp by package error!")
            conn.close()
            return None

        data = res.read()
        conn.close()
        return NetUtils.parser_data(data)

    @staticmethod
    def get_appinfo_by_apk_url(apk_url):
        logging.info("")
        logging.info("apk_url: %s" % apk_url)
        response = urllib.urlopen(apk_url)
        if response.getcode() == 200:
            data = response.read()
            response.close()
            return NetUtils.parser_data(data)
        else:
            return None

    # 解析返回的数据
    @staticmethod
    def parser_data(data):
        logging.info("")
        info = {}
        try:
            match = re.search(r"appDetailData = (.*?(\n.*){11})", data)
            if match:
                match_data = match.groups()[0]
                match_data = match_data.replace("orgame", "\"orgame\"").replace("apkName", "\"apkName\"").replace(
                    "apkCode", "\"apkCode\"").replace("appId", "\"appId\"").replace("appName", "\"appName\"").replace(
                    "iconUrl", "\"iconUrl\"").replace("appScore", "\"appScore\"").replace("downTimes",
                                                                                          "\"downTimes\"").replace(
                    "downUrl", "\"downUrl\"").replace("tipsUpDown", "\"tipsUpDown\"")
                json_data = json.loads(match_data)
                info["package"] = str(json_data['apkName'])
                info["versionCode"] = str(json_data['apkCode'])
                info["appId"] = str(json_data['appId'])
                info["label"] = str(json_data['appName'])
                info["icon"] = str(json_data['iconUrl'])

            match_vname = re.search(
                r"<div class=\"det-othinfo-data\">V(.[^<>]*)</div>", data)
            if match_vname:
                info["versionName"] = match_vname.groups()[0]

            match_cate = re.search(r"id=\"J_DetCate\">(.[^<>]*)</a>", data)
            if match_cate:
                info["appCate"] = match_cate.groups()[0]
            logging.info("----------------Net app Info---------------------")
            for k, v in info.items():
                logging.info("%s:%s" % (k, v))
            logging.info("----------------Net app Info---------------------")
            return info
        except Exception, ex:
            print(ex)
            sys.exit()

    # 下载进度
    @staticmethod
    def schedule(count, block_size, total_size):
        pass
        # percent = int(count * block_size * 100 / total_size)
        # print(">>>\r%d%%" % percent + ' complete')

    # 下载文件到指定目录
    @staticmethod
    def download(url, local, file_name, file_ext):
        logging.info("")
        if not file_name.endswith(file_ext):
            file_name = "%s.%s" % (file_name, file_ext)
        path = os.path.join(local, file_name)
        urllib.urlretrieve(url, path, NetUtils.schedule)
        logging.info("Download %s => OK" % url)
        return path

    # 判断是否为url
    @staticmethod
    def is_url(url):
        return re.match(r'^http?:/{2}\w.+$', url)


if __name__ == '__main__':
    try:
        NetUtils.get_appinfo_from_myapp_by_package("com.blsm.sft")
        NetUtils.get_appinfo_by_apk_url(
            "http://sj.qq.com/myapp/detail.htm?apkName=com.mel0t.meshow")
        print NetUtils.download("http://pp.myapp.com/ma_icon/0/icon_264935_19185291_1409895255/72", "config", "tu.png",
                                "png")
    except Exception, e:
        print e
        print "==============================="
        print "Prompt>>>python netutils.py"
        print "==============================="
        print
        sys.exit()