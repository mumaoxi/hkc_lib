#!/bin/bash
mkdir .temp_for_certificate
cd .temp_for_certificate
count=0
while [ -n "$1" ]
do
count=$[$count+1]
echo "(#$count) "`basename "$1"`":"
echo ""
path=`jar tf "$1" | grep RSA` #查找apk中RSA文件
jar xf $1 $path #把RSA文件解压出来
keytool -printcert -file $path #查看指纹证书
rm -r $path #删除之前解压的文件
echo "--------------------------------------------"
shift
done
cd ..
rm -r .temp_for_certificate 
