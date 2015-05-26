#!/data/data/marzec/app_bin/sh
#
# see attacks-common for arguments, the args are converted to vars there.  The
# first arg is the "app payload" directory where the included scripts are kept

# many phones don't even include 'test', so set the path to our
# busybox tools first, where we provide all the UNIX tools needed by
# this script
export PATH=$1:$PATH

test -e $1/marzecpro_co || exit
. $1/marzecpro_co

# create the mount dir on the read-only rootfs
if [ ! -e $mnt ]; then
    mount -o remount,rw rootfs /
    echo mkdir $mnt
    mkdir $mnt
    mount -o remount,ro rootfs /
fi
