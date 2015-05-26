#!/data/data/marzec/app_bin/sh
#
# see marzecpro_co for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

echo "--------------------------------------------------"
echo "./test.sh"

echo $1/MarzecPro_co
test -e $1/MarzecPro_co || exit
. $1/MarzecPro_co

. $app_bin/install.conf

echo "app_bin: $app_bin"
echo "sdcard: $sdcard"
echo "install_path: $install_path"
echo "mnt: $mnt"
echo "release: $release"
echo "mirror: $mirror"
echo "arch: $arch"
echo "imagesize: $imagesize"
