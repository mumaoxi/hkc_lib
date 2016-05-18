#!/usr/bin/env python
# -*- coding: UTF-8 -*-

try:
    import xml.etree.cElementTree as ET
except ImportError:
    import xml.etree.ElementTree as ET
from apkconfig import *

logging.basicConfig(level=logging.DEBUG,
                    format='%(asctime)s %(filename)s %(funcName)s [line:%(lineno)d] %(levelname)s %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')

"""
python ~/Documents/iProject/Coding/rebuild/rebuild.py ~/Documents/iProject/Coding/rebuild/config
"""


class Rebuild(object):
    rebuild_file_path = None
    rebuild_xml_path = None

    def __init__(self, rebuild_file_path=None):
        if rebuild_file_path:
            self.rebuild_file_path = os.path.abspath(rebuild_file_path)
            self.rebuild_xml_path = "%s/rebuild.xml" % rebuild_file_path
            # 进入下一步动之前验证
            if not os.path.exists(self.rebuild_xml_path):
                logging.info("No found %s, Please check your config" % self.rebuild_xml_path)
                sys.exit()

    def rebuild(self):
        begin_time = time.time()
        tree = ET.parse(self.rebuild_xml_path)
        apks = tree.findall("apk")
        try:
            for apk in apks:
                apk_config = ApkConfig()
                apk_config.parser_rebuild_xml(apk)
                # 修改APK
                msg = self.modify_apk(apk_config)
                end_time = time.time()
                logging.info("Modify apk %s Time: %ss" % (apk_config.original_apk, (end_time - begin_time)))
                logging.info(msg)
        except Exception, ex:
            logging.error(Exception)
            logging.error(ex)
        finally:
            # 清理配置文件
            os.remove(self.rebuild_xml_path)

    def modify_apk(self, apk_config):
        logging.info("")
        apk_config.desc()
        try:
            # 获取老APK信息
            ApkUtils.info(apk_config.original_apk)
            # 获取存储路径
            rebuild_dir = ApkUtils.get_out_dir(apk_config.original_apk, "rebuild")
            # 反编译
            decode_dir = ApkUtils.decode(apk_config.original_apk, rebuild_dir, apk_config.tool_version_java)
            package_utils = PackageUtils(decode_dir, apk_config.new_package)
            # 修改编码版本和版本号
            if apk_config.version_code or apk_config.version_name:
                PackageUtils.update_version_info(decode_dir, apk_config.version_code, apk_config.version_name,
                                                 apk_config.tool_version_java)
            # 修改包名
            if apk_config.new_package:
                package_utils.modify_all_package()
            # 修改应用名称
            if apk_config.new_app_label:
                package_utils.modify_app_label(decode_dir, apk_config.new_app_label)
            # 修改应用图标
            if apk_config.ldpi or apk_config.mdpi or apk_config.hdpi or apk_config.xhdpi or apk_config.xxhdpi:
                package_utils.modify_app_lanucher(decode_dir, apk_config.ldpi, apk_config.mdpi,
                                                  apk_config.hdpi, apk_config.xhdpi, apk_config.xxhdpi)
            # 修改meta-data信息
            for key, value in apk_config.extra_meta_data.items():
                package_utils.modify_meta_data(decode_dir, key, value)
            # 编译
            build_apk = ApkUtils.build(decode_dir, rebuild_dir, apk_config.tool_version_java)
            # 签名
            signed_apk = ApkUtils.sign(build_apk, rebuild_dir, apk_config.keystore)
            # 对齐
            zipaligned_apk = ApkUtils.zipalign(signed_apk, rebuild_dir, apk_config.build_apk)
            return zipaligned_apk
        except Exception, ex:
            logging.error(Exception)
            logging.error(ex)
        finally:
            # 删除ldpi临时图标文件
            if apk_config.ldpi and os.path.exists(apk_config.ldpi):
                os.remove(apk_config.ldpi)
            # 删除mdpi临时图标文件
            if apk_config.mdpi and os.path.exists(apk_config.mdpi):
                os.remove(apk_config.mdpi)
            # 删除hdpi临时图标文件
            if apk_config.hdpi and os.path.exists(apk_config.hdpi):
                os.remove(apk_config.hdpi)
            # 删除xhdpi临时图标文件
            if apk_config.xhdpi and os.path.exists(apk_config.xhdpi):
                os.remove(apk_config.xhdpi)
            # 删除xxhdpi临时图标文件
            if apk_config.xxhdpi and os.path.exists(apk_config.xxhdpi):
                os.remove(apk_config.xxhdpi)
            # 删除临时签名signed_apk
            if signed_apk and os.path.exists(signed_apk):
                os.remove(signed_apk)
            # 删除临时编译文件build_apk
            if build_apk and os.path.exists(build_apk):
                os.remove(build_apk)
            # 删除临时反编译文件decode_dir
            if decode_dir and os.path.exists(decode_dir):
                shutil.rmtree(decode_dir)
            # 删除临时原包文件
            if apk_config.original_apk and os.path.exists(apk_config.original_apk):
                os.remove(apk_config.original_apk)

# 配置文件路径
if __name__ == '__main__':
    try:
        rebuild_file_path = sys.argv[1]
        hole = Rebuild(rebuild_file_path)
        hole.rebuild()
    except Exception, ex:
        logging.info(Exception)
        logging.info(ex)
        logging.info("===============================")
        logging.info("Prompt>>>python rebuild.py config")
        logging.info("===============================")
        sys.exit()
