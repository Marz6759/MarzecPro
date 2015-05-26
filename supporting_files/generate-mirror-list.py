#!/usr/bin/python

import re
import sys
import urllib2

# mirrors = ("ftp.uk.debian.org")


keep = []
mirrors = urllib2.urlopen('http://www.debian.org/mirror/list')
for line in mirrors.readlines():
    m = re.match('.*<td valign="top"><a rel="nofollow" href="http://([^/]*).*">.*', line)
    if m:
        mirror = m.group(1)
        debianurl = 'http://' + mirror + '/debian/dists/jessie'
        ubuntuurl = 'http://' + mirror + '/ubuntu/dists/raring'
        print 'trying: ',
        print mirror,
        print '...',
        sys.stdout.flush()
        try:
            response=urllib2.urlopen(debianurl, timeout=5)
            response=urllib2.urlopen(ubuntuurl, timeout=5)
            keep.append(mirror)
            print 'success!'
        except:
            print 'fail!'

print keep

i = 0
for m in sorted(keep):
    print '"' + m + '", ',
    i += 1
    if i % 3 == 0:
        print ""
