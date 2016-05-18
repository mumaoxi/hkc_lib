#!/usr/bin/env python
# -*- coding: UTF-8 -*-

try:
    import xml.etree.cElementTree as ET
except ImportError:
    import xml.etree.ElementTree as ET
from extrautils import *
from apkconfig import *

logging.basicConfig(level=logging.DEBUG,
                    format='%(levelname)s %(asctime)s %(filename)s %(funcName)s [line:%(lineno)d] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')

# 应用合包
class Hole:
    rehole_file_path = None
    rehole_xml_path = None

    def __init__(self, rehole_file_path=None):
        if rehole_file_path:
            self.rehole_file_path = os.path.abspath(rehole_file_path)
            self.rehole_xml_path = "%s/rehole.xml" % rehole_file_path
            # 进入下一步动之前验证
            if not os.path.exists(self.rehole_xml_path):
                logging.error(">>>No found %s, Please check your config" % self.rehole_xml_path)
                sys.exit()
            logging.info(">>>Config xml path: %s" % self.rehole_xml_path)
        else:
            logging.info(">>>Please validate your config")
            sys.exit()

    def merger(self):
        begin_time = time.time()
        tree = ET.parse(self.rehole_xml_path)
        try:
            apks = tree.findall("apk")
            for apk in apks:
                apk_config = ApkConfig()
                apk_config.parser_rehole_xml(apk)
                # 整合开始
                msg = self.merge_apk(apk_config)
                end_time = time.time()
                logging.info("Modify apk %s Time: %ss" % (apk_config.original_apk, (end_time - begin_time)))
                logging.info(msg)
        except Exception, ex:
            raise ex
        finally:
            # 清理配置文件
            os.remove(self.rehole_xml_path)

    def merge_apk(self, apk_config):

        logging.info("")
        apk_config.desc()

        decode_dir = None
        try:
            # 获取渠道APK
            filename = "%s_%s_original" % (apk_config.new_package, apk_config.version_code)
            original_apk = ApkUtils.get_original(apk_config.original_apk, "apk", filename)

            # 获取老APK信息
            original_info = ApkUtils.info(original_apk)

            # 第三方项目压缩包
            filename = "%s_%s_extra" % (apk_config.new_package, apk_config.version_code)
            extra_zip = os.path.join(sys.path[0], apk_config.extra_zip)
            apk_config.extra_zip = ApkUtils.get_original(extra_zip, "zip", filename)

            # 反编译老APK
            rehole_dir = ApkUtils.get_out_dir(apk_config.original_apk, "rehole")
            decode_dir = ApkUtils.decode(apk_config.original_apk, rehole_dir, apk_config.tool_version_java)

            package_utils = PackageUtils(decode_dir, apk_config.new_package, apk_config.replace_smali_const_package,
                                         apk_config.replace_smali_const_label)

            # 修改编码版本和版本号
            if apk_config.version_code or apk_config.version_name:
                PackageUtils.update_version_info(decode_dir, apk_config.version_code, apk_config.version_name,
                                                 apk_config.tool_version_java)

            # 修改包名
            if apk_config.new_package:
                package_utils.modify_all_package()

            # 替换应用图标
            if apk_config.hdpi:
                package_utils.modify_app_lanucher(decode_dir, apk_config.ldpi, apk_config.mdpi, apk_config.hdpi,
                                                  apk_config.xhdpi, apk_config.xxhdpi)

            # 修改应用名称
            if apk_config.new_app_label:
                package_utils.modify_app_label(decode_dir, apk_config.new_app_label)

            # 整合应用
            if "launchable-activity" in original_info:
                launchable_activity = original_info["launchable-activity"]
                extra_utils = ExtraUtils(self.rehole_file_path, decode_dir, launchable_activity, apk_config)
                extra_utils.merge()
            else:
                logging.error("No found launchable-activity")
                sys.exit()

            # 编译
            build_apk = ApkUtils.build(decode_dir, rehole_dir, apk_config.tool_version_java)
            # 签名
            signed_apk = ApkUtils.sign(build_apk, rehole_dir, apk_config.keystore)
            # 对齐
            zipaligned_apk = ApkUtils.zipalign(signed_apk, rehole_dir, apk_config.build_apk)
            return zipaligned_apk
        except Exception, ex:
            raise ex
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
        rehole_file_path = sys.argv[1]
        hole = Hole(rehole_file_path)
        hole.merger()
    except Exception, ex:
        logging.info(Exception)
        logging.info(ex)
        logging.info("===============================")
        logging.info("Prompt>>>python rehole.py config")
        logging.info("===============================")
        sys.exit()
