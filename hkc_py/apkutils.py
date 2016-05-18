#!/usr/bin/env python
# -*- coding: UTF-8 -*-
import time
import zipfile
import commands

import pexpect

from packageutils import *
from netutils import *


logging.basicConfig(level=logging.DEBUG,
                    format='%(levelname)s %(asctime)s %(filename)s %(funcName)s [line:%(lineno)d] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')


class ApkUtils(object):
    # 查看应用信息
    @staticmethod
    def info(src_apk):
        logging.info("")
        if not os.path.exists(src_apk):
            logging.info("Src apk not exist")
            sys.exit()
        cmd = "aapt d badging %s" % src_apk
        logging.info(cmd)
        mani_info = os.popen(cmd).readlines()
        info = {}
        for line in mani_info:
            if "package" in line and "versionCode=" in line:
                manifest = ApkUtils.__parser_line(line)
                info["package"] = manifest["name"]
                info["versionCode"] = manifest["versionCode"]
                info["versionName"] = manifest["versionName"]
            elif "application" in line and "label=" in line and "icon=" in line:
                application = ApkUtils.__parser_line(line)
                info.update(application)
            elif "launchable-activity" in line and "name=" in line:
                launcher = ApkUtils.__parser_line(line)
                info["launchable-activity"] = launcher["name"]
            else:
                continue
        logging.info("-------------Apk Info----------------")
        for k, v in info.items():
            logging.info(">>>%s:%s" % (k, v))
        logging.info("-------------Apk Info----------------")
        logging.info("Info %s => OK" % src_apk)
        return info

    # 反编译APK
    @staticmethod
    def decode(src_apk, decode_dir="", tool_version_java={}):
        logging.info("")
        if not os.path.exists(src_apk):
            logging.info("Src apk not exist")
            sys.exit()
        # 获取编译存放目录
        apk_name = os.path.basename(src_apk).replace(".apk", "")
        decode_dir = decode_dir and decode_dir or os.path.dirname(src_apk)
        out_dir = decode_dir and "%s/%s" % (decode_dir, apk_name) or apk_name
        # 获取Apktool版本和Java路径
        tool_version = "2.0.0"
        java_path = "/Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/Home/bin/java"
        if tool_version_java:
            tool_version = tool_version_java.keys()[0]
            java_path = tool_version_java[tool_version]
        # 获取apktool不同版本的参数配置
        out_key = tool_version >= "2.0.0" and "-o" or ""
        cmd = "%s -jar %s/apktool-%s.jar d -f %s %s %s" % (
            java_path, sys.path[0], tool_version, src_apk, out_key, out_dir)
        logging.info(cmd)
        os.system(cmd)
        if os.path.exists(out_dir):
            logging.info("Decode %s to %s => OK" % (src_apk, out_dir))
        else:
            logging.info("Decode %s to %s => Failure" % (src_apk, out_dir))
        logging.info(out_dir)
        return out_dir

    # 编译成APK
    @staticmethod
    def build(project_dir, build_dir="", tool_version_java={}):
        logging.info("")
        if not os.path.exists(project_dir):
            logging.info("Project dir not exist")
            sys.exit()
        # 编译前的修复工作
        package_utils = PackageUtils("", "")
        package_utils.pre_build(project_dir)
        # 获取编译后APK存放路径
        build_apk = "%s_%s_build.apk" % (os.path.basename(project_dir), ApkUtils.__timestamp())
        build_dir = build_dir and build_dir or os.path.dirname(project_dir)
        out_apk = build_dir and "%s/%s" % (build_dir, build_apk) or build_apk
        # 获取Apktool版本和Java路径
        tool_version = "2.0.0"
        java_path = "/Library/Java/JavaVirtualMachines/jdk1.7.0_71.jdk/Contents/Home/bin/java"
        if tool_version_java:
            tool_version = tool_version_java.keys()[0]
            java_path = tool_version_java[tool_version]
        # 获取apktool不同版本的参数配置
        out_key = tool_version >= "2.0.0" and "-o" or ""
        cmd = "%s -jar %s/apktool-%s.jar b -f %s %s %s" % (
        java_path, sys.path[0], tool_version, project_dir, out_key, out_apk)
        logging.info(cmd)
        os.system(cmd)
        # 进入下一步动作前验证
        if os.path.exists(out_apk):
            logging.info("Build %s to %s => OK" % (project_dir, out_apk))
        else:
            logging.info("Build %s to %s => Failure" % (project_dir, out_apk))
        logging.info(out_apk)
        return out_apk

    # 签名APK
    @staticmethod
    def sign(src_apk, signed_dir="", keystore={}):
        logging.info("")
        if not os.path.exists(src_apk):
            logging.info("Src apk not exist")
            sys.exit()
        keystore_name = "hellokittycat.keystore"
        keystore_pwd = "hellokittycat"
        if keystore:
            keystore_name = keystore["name"]
            keystore_pwd = keystore["pwd"]
        keystore_name = "%s/%s" % (sys.path[0], keystore_name)
        # 获取签名后APK存放路径
        signed_apk = "%s_%s_signed.apk" % (os.path.basename(src_apk).replace(".apk", ""), ApkUtils.__timestamp())
        signed_dir = signed_dir and signed_dir or os.path.dirname(src_apk)
        out_apk = signed_dir and "%s/%s" % (signed_dir, signed_apk) or signed_apk
        cmd = "jarsigner -verbose -tsa http://timestamp.digicert.com -sigalg SHA1withRSA -digestalg SHA1 -keystore %s -signedjar %s %s %s" % (
            keystore_name, out_apk, src_apk, keystore_pwd)
        logging.info(cmd)
        jarsigner = pexpect.spawn(cmd)
        jarsigner.expect(".*:*")
        jarsigner.sendline(keystore_pwd)
        jarsigner.expect(pexpect.EOF)
        # 进入下一步之前验证
        if os.path.exists(out_apk):
            logging.info("Sign %s to %s => OK" % (src_apk, out_apk))
        else:
            logging.info("Sign %s to %s => Failure" % (src_apk, out_apk))
        logging.info(out_apk)
        return out_apk

    # 压缩APK
    @staticmethod
    def zipalign(src_apk, zipalign_dir="", new_apk_name=""):
        logging.info("")
        if not os.path.exists(src_apk):
            logging.info("Src apk not exist")
            sys.exit()
        # 获取压缩后的APK存放路径
        info = ApkUtils.info(src_apk)
        zipaligned_apk = new_apk_name
        zipalign_dir = zipalign_dir and zipalign_dir or os.path.dirname(src_apk)
        if not new_apk_name:
            zipaligned_apk = "%s_V%s_C%s_%s_%s_zipaligned.apk" % (
                info["label"], info["versionName"], info["versionCode"], info["package"], ApkUtils.__timestamp())
        out_apk = zipalign_dir and "%s/%s" % (zipalign_dir, zipaligned_apk) or zipaligned_apk
        cmd = "zipalign -f -v 4 %s %s" % (src_apk, out_apk)
        logging.info(cmd)
        os.system(cmd)
        # 进入一步动之前验证
        if os.path.exists(out_apk):
            logging.info("Zipalign %s to %s => OK" % (src_apk, out_apk))
        else:
            logging.info("Zipalign %s to %s => OK" % (src_apk, out_apk))
        logging.info(out_apk)
        return out_apk

    # decode->build->sign->zipalign
    @staticmethod
    def resign(src_apk, tool_version={}, new_apk_name=""):
        logging.info("")
        begin_time = time.time()
        decode_dir = ""
        try:
            src_apk = ApkUtils.get_original(src_apk, "apk")
            ApkUtils.info(src_apk)
            resign_dir = ApkUtils.get_out_dir(src_apk, "resign")
            decode_dir = ApkUtils.decode(src_apk, resign_dir, tool_version)
            zipaligned_apk = ApkUtils.__b2z(decode_dir, tool_version, new_apk_name, resign_dir)
            return zipaligned_apk
        except Exception, ex:
            print(Exception)
            print(ex)
        finally:
            # 删除临时资源
            if os.path.exists(src_apk):
                os.remove(src_apk)
            if os.path.exists(decode_dir):
                shutil.rmtree(decode_dir)
        end_time = time.time()
        logging.info("-------------------------------")
        logging.info("Resign %s Time: %ss" % (src_apk, (end_time - begin_time)))
        logging.info("-------------------------------")

    # decode->modify_package->build->sign->zipalign
    @staticmethod
    def repackage(src_apk, new_package, tool_version={}, new_apk_name=""):
        logging.info("")
        begin_time = time.time()
        try:
            src_apk = ApkUtils.get_original(src_apk, "apk")
            ApkUtils.info(src_apk)
            repackage_dir = ApkUtils.get_out_dir(src_apk, "repackage")
            decode_dir = ApkUtils.decode(src_apk, repackage_dir, tool_version)
            # 换包名
            package_utils = PackageUtils(decode_dir, new_package)
            package_utils.modify_all_package()
            zipaligned_apk = ApkUtils.__b2z(decode_dir, tool_version, new_apk_name, repackage_dir)
            return zipaligned_apk
        except Exception, ex:
            logging.error(Exception)
            logging.error(ex)
        finally:
            # 删除临时资源
            if os.path.exists(src_apk):
                os.remove(src_apk)
            if os.path.exists(decode_dir):
                shutil.rmtree(decode_dir)
        end_time = time.time()
        logging.info("-------------------------------")
        logging.info("Repackage %s Time: %ss" % (src_apk, (end_time - begin_time)))
        logging.info("-------------------------------")

    # build->sign->zipalign
    @staticmethod
    def build2zipalign(project_dir, tool_version={}, new_apk_name="", b2z_dir=""):
        logging.info("")
        if not os.path.exists(project_dir):
            logging.info("Project dir not exist")
            sys.exit()
        begin_time = time.time()
        if not b2z_dir:
            b2z_dir = ApkUtils.get_out_dir(project_dir, "build2zipalign")
        zipaligned_apk = ApkUtils.__b2z(project_dir, tool_version, new_apk_name, b2z_dir)
        end_time = time.time()
        logging.info("-------------------------------")
        logging.info("Build2Zipalign %s Time: %ss" % (project_dir, (end_time - begin_time)))
        logging.info("-------------------------------")
        return zipaligned_apk

    # package->build->sign->zipalign
    @staticmethod
    def package2zipalign(project_dir, new_package, tool_version={}, new_apk_name=""):
        logging.info("")
        if not os.path.exists(project_dir):
            logging.info("Project dir not exist")
            sys.exit()
        begin_time = time.time()
        p2z_dir = ApkUtils.get_out_dir(project_dir, "package2zipalign")
        # 换包名
        package_utils = PackageUtils(project_dir, new_package)
        package_utils.modify_all_package()
        zipaligned_apk = ApkUtils.__b2z(project_dir, tool_version, new_apk_name, p2z_dir)
        end_time = time.time()
        logging.info("-------------------------------")
        logging.info("Package2Zipalign %s Time: %ss" % (project_dir, (end_time - begin_time)))
        logging.info("-------------------------------")
        return zipaligned_apk

    # build->sign->zipalign
    @staticmethod
    def __b2z(project_dir, tool_version="1.5.2", new_apk_name="", b2z_dir=""):
        logging.info("")
        if not os.path.exists(project_dir):
            logging.info("Project dir not exist")
            sys.exit()
        try:
            build_apk = ApkUtils.build(project_dir, b2z_dir, tool_version)
            signed_apk = ApkUtils.sign(build_apk, b2z_dir)
            zipaligned_apk = ApkUtils.zipalign(signed_apk, b2z_dir, new_apk_name)
            logging.info(zipaligned_apk)
            return zipaligned_apk
        except Exception, ex:
            logging.error(Exception)
            logging.error(ex)
        finally:
            # 删除临时资源
            if os.path.exists(build_apk):
                os.remove(build_apk)
            if os.path.exists(signed_apk):
                os.remove(signed_apk)

    # 获取APK
    @staticmethod
    def get_original(url, ext, filename=""):
        logging.info("")
        logging.info("url => %s" % url)
        logging.info("ext => %s" % ext)
        logging.info("filename => %s" % filename)
        if NetUtils.is_url(url):
            return ApkUtils.download_file(url, ext, filename)
        else:
            return ApkUtils.copy_local_file(url)

    # 获取当前时间
    @staticmethod
    def __timestamp():
        return time.strftime("%Y%m%d%H%M%S", time.localtime())

    # 解析存在键值对的字符串
    @staticmethod
    def __parser_line(line):
        result = {}
        array = line.split(' ')
        for item in array:
            key_value = item.split('=')
            if len(key_value) != 2:
                continue
            result[key_value[0]] = key_value[
                1].replace('\'', '').replace('\n', '')
        return result

    # 获取输出目录
    @staticmethod
    def get_out_dir(src_apk, action):
        dir_name = os.path.dirname(src_apk)
        return os.path.join(dir_name, action)

    # 更换Java环境
    @staticmethod
    def __change_jdk(version):
        cmd = "/usr/libexec/java_home -v %s" % version
        logging.info(cmd)
        java_home = commands.getstatusoutput(cmd)
        logging.info(java_home)
        cmd = "export JAVA_HOME=%s" % str(java_home[1])
        logging.info(cmd)
        os.system(cmd)

    # 移动本地文件
    @staticmethod
    def copy_local_file(src_path):
        logging.info("")
        logging.info("src_path => %s" % src_path)
        if not os.path.exists(src_path):
            logging.info("Src path dir not exist")
            sys.exit()
        current_path = sys.path[0]
        config_dir = os.path.join(current_path, "config")
        if not os.path.exists(config_dir):
            os.makedirs(config_dir)
        # 判断是否同一目录，不是则拷贝
        dir_name = os.path.dirname(src_path)
        if dir_name != config_dir:
            shutil.copy(src_path, config_dir)
        filename = os.path.basename(src_path)
        original_apk = os.path.join(config_dir, filename)
        # 进入一步动之前验证
        if os.path.exists(original_apk):
            logging.info("Copy file %s to %s => OK" % (src_path, config_dir))
        else:
            logging.info("Copy file %s to %s => Failure" % (src_path, config_dir))
            sys.exit()
        logging.info(original_apk)
        return original_apk

    @staticmethod
    def download_file(url, ext, filename=""):
        logging.info("")
        local = sys.path[0]
        config_dir = os.path.join(local, "config/")
        if not os.path.exists(config_dir):
            os.makedirs(config_dir)
        if not filename:
            url_array = url.split("/")
            tmp_filename = url_array[len(url_array) - 1]
            old_str = ".%s" % ext
            if tmp_filename.endswith(old_str):
                filename = tmp_filename.replace(old_str, "")
            else:
                filename = tmp_filename
        original_apk = NetUtils.download(url, config_dir, filename, ext)
        # 进入一步动之前验证
        if os.path.exists(original_apk):
            logging.info("Download file %s => OK" % url)
        else:
            logging.info("Download file %s => Failure" % url)
            sys.exit()
        logging.info(original_apk)
        return original_apk

    @staticmethod
    def get_icon_from_apk(src_apk, icon, new_name=""):
        logging.info("")
        logging.info("src_apk => %s" % src_apk)
        logging.info("icon => %s" % icon)
        logging.info("new_name => %s" % new_name)
        if not os.path.exists(src_apk):
            logging.info("Src apk not exist")
            sys.exit()
        try:
            target_dir = os.path.abspath(os.path.dirname(src_apk))
            icon_name = None
            if "/" in icon:
                icon_array = icon.split("/")
                logging.info(icon_array)
                icon = icon_array[len(icon_array) - 1].replace(".png", "")
            zfile = zipfile.ZipFile(src_apk, mode="r")
            for filename in zfile.namelist():
                if icon in filename:
                    if not filename.endswith('/'):
                        f = os.path.join(target_dir, filename)
                        icon_name = f
                        dirname = os.path.dirname(f)
                        if not os.path.exists(dirname):
                            os.makedirs(dirname)
                        file(f, 'wb').write(zfile.read(filename))
            zfile.close()
            # 移动资源
            if new_name == "":
                basename = os.path.basename(icon_name)
                new_name = basename.replace(".png", "")

            old_dir = os.path.abspath(icon_name)
            new_dir = "%s/%s.png" % (target_dir, new_name)
            shutil.move(old_dir, new_dir)
        except Exception, ex:
            logging.error(ex)
        finally:
            # 清理资源
            res_dir = "%s/res" % target_dir
            shutil.rmtree(res_dir)
        # 进入下一步动作前验证
        if os.path.exists(new_dir):
            logging.info("Get icon from apk %s => OK" % icon)
        else:
            logging.info("Get icon %s from apk => Failure" % icon)
            sys.exit()
        logging.info(new_dir)
        return new_dir
