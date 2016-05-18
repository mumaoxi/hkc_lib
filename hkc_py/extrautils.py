#!/usr/bin/env pyhton
# -*- coding: UTF-8 -*-
from mergeutils import *
from apkconfig import *

logging.basicConfig(level=logging.DEBUG,
                    format='%(levelname)s %(asctime)s %(filename)s %(funcName)s [line:%(lineno)d] %(message)s',
                    datefmt='%Y-%m-%d %H:%M:%S')


class ExtraUtils(object):
    hole_file_path = None
    launchable_activity = None
    decode_dir = None
    apk_config = None

    def __init__(self, hole_file_path, decode_dir, launchable_activity, apk_config):

        self.hole_file_path = hole_file_path
        self.decode_dir = decode_dir
        self.launchable_activity = launchable_activity
        self.apk_config = apk_config

        logging.info("hole_file_path: %s" % self.hole_file_path)
        logging.info("decode_dir: %s" % self.decode_dir)
        logging.info("launchable_activity: %s" % self.launchable_activity)
        self.apk_config.desc()

    # 解压第三方项目压缩包
    def unzip_extra(self):
        logging.info("")
        try:
            source_zip = self.apk_config.extra_zip
            target_dir = "%s/%s_extra" % (
                os.path.dirname(self.decode_dir), self.apk_config.new_package.replace(".", "_"))
            logging.info("target_dir: %s" % target_dir)
            zip_file = zipfile.ZipFile(source_zip, mode="r")
            for filename in zip_file.namelist():
                if not filename.endswith('/'):
                    file_path = os.path.join(target_dir, filename)
                    file_dir = os.path.dirname(file_path)
                    if not os.path.exists(file_dir):
                        os.makedirs(file_dir)
                    file(file_path, 'wb').write(zip_file.read(filename))
            zip_file.close()
            extra_zip = os.path.basename(source_zip)
            extra_name = os.path.splitext(extra_zip)[0]
            extra_unzip_dir = "%s/%s" % (target_dir, extra_name)
            logging.info("Unzip %s to %s => OK" % (source_zip, target_dir))
            return extra_unzip_dir
        except Exception, ex:
            logging.error(Exception)
            logging.error(ex)
            sys.exit(0)
        finally:
            # 清理资源
            os.remove(source_zip)

    # Debug模式打包应用, 并移动到指定目录
    @staticmethod
    def ant_debug(project_dir, dest_apk):
        logging.info("")
        # 更新本地打包环境
        cwd = os.getcwd()
        os.chdir(project_dir)
        cmd = "android update project -p ."
        logging.info(cmd)
        os.system(cmd)

        # 打包
        cmd = "ant clean"
        logging.info(cmd)
        os.system(cmd)

        cmd = "ant debug"
        logging.info(cmd)
        os.system(cmd)

        # 移动apk
        cmd = "mv -f %s/bin/*-debug.apk %s" % (project_dir, dest_apk)
        logging.info(cmd)
        os.system(cmd)

        # 恢复目录环境
        os.chdir(cwd)

        logging.info("Ant debug %s to %s => OK" % (project_dir, dest_apk))
        return dest_apk

    # Release模式打包应用, 并移动到指定目录
    @staticmethod
    def ant_release(project_dir, dest_apk):
        logging.info("")
        # 更新本地打包环境
        cwd = os.getcwd()
        os.chdir(project_dir)
        cmd = "android update project -p ."
        logging.info(cmd)
        os.system(cmd)

        # 打包
        cmd = "ant clean"
        logging.info(cmd)
        os.system(cmd)

        cmd = "ant release"
        logging.info(cmd)
        os.system(cmd)

        # 移动apk
        cmd = "mv -f %s/bin/*-release.apk %s" % (project_dir, dest_apk)
        logging.info(cmd)
        os.system(cmd)

        # 恢复目录环境
        os.chdir(cwd)

        logging.info("Ant release %s to %s => OK" % (project_dir, dest_apk))
        return dest_apk

    def merge(self):
        logging.info("")
        try:
            # 解压extra.zip
            extra_unzip_dir = self.unzip_extra()
            # 修改extra包名
            package_utils = PackageUtils(extra_unzip_dir, self.apk_config.new_package)
            # 修改包名
            package_utils.modify_all_package(True)
            # 替换应用图标
            package_utils.modify_app_lanucher(extra_unzip_dir, None, None, self.apk_config.hdpi, self.apk_config.xhdpi,
                                              None)
            # 修改应用名称
            package_utils.modify_app_label(extra_unzip_dir, self.apk_config.new_app_label)
            # 修改meta-data信息
            for key, value in self.apk_config.extra_meta_data.items():
                package_utils.modify_meta_data(extra_unzip_dir, key, value)
            # 修改launcher activity
            package_utils.modify_activity_name(
                extra_unzip_dir, self.apk_config.extra_replace_activity, self.launchable_activity)
            # Debug模式打包应用, 并移动到指定目录
            extra_apk_name = "%s_extra.apk" % self.apk_config.new_package.replace(".", "_")
            extra_apk_dir = os.path.join(os.path.dirname(self.decode_dir), extra_apk_name)
            logging.info("Extra build mode: %s", self.apk_config.extra_build_mode)
            if self.apk_config.extra_build_mode == "release":
                self.ant_release(os.path.abspath(extra_unzip_dir), os.path.abspath(extra_apk_dir))
            else:
                self.ant_debug(os.path.abspath(extra_unzip_dir), os.path.abspath(extra_apk_dir))
        except Exception, ex:
            logging.error(Exception)
            logging.error(ex)
            sys.exit(0)
        finally:
            # 清理资源
            target_dir = os.path.dirname(extra_unzip_dir)
            shutil.rmtree(target_dir)

        # 整合extra apk到目标项目
        merge_utils = MergeUtils(extra_apk_dir, self.decode_dir, self.launchable_activity, package_utils.old_package,
                                 self.apk_config)
        merge_utils.merge_extra()
