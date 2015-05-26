/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */
package org.torproject.android.service;

public interface TorServiceConstants {

    String TAG = "ORBOT";

    String TOR_APP_USERNAME = "org.torproject.android";

    String ASSETS_BASE = "assets/";

    //home directory of Android application

    //name of the tor C binary
    String TOR_BINARY_ASSET_KEY = "tor";

    //torrc (tor config file)
    String TORRC_ASSET_KEY = "torrc";
    String TOR_CONTROL_COOKIE = "control_auth_cookie";

    //how to launch tor
//    public final static String TOR_COMMAND_LINE_ARGS = "-f " + TORRC_INSTALL_PATH  + " || exit\n";

    //privoxy
    String PRIVOXY_ASSET_KEY = "privoxy";

    //privoxy.config
    String PRIVOXYCONFIG_ASSET_KEY = "privoxy.config";

    //various console cmds
    String SHELL_CMD_CHMOD = "chmod";
    String SHELL_CMD_KILL = "kill -9";
    String SHELL_CMD_RM = "rm";
    String SHELL_CMD_PS = "ps";
    String SHELL_CMD_PIDOF = "pidof";

    String CHMOD_EXE_VALUE = "777";

    //path of the installed APK file
    //public final static String APK_PATH = "/data/app/org.torproject.android.apk";
    //public final static String APK_PATH_BASE = "/data/app";



    int FILE_WRITE_BUFFER_SIZE = 2048;

    //HTTP Proxy server port
    int PORT_HTTP = 8118; //just like Privoxy!

    //Socks port client connects to, server is the Tor binary
    int PORT_SOCKS = 9050;

    //what is says!
    String IP_LOCALHOST = "127.0.0.1";
    int TOR_CONTROL_PORT = 9051;
    int UPDATE_TIMEOUT = 1000;
    int TOR_TRANSPROXY_PORT = 9040;
    int STANDARD_DNS_PORT = 53;
    int TOR_DNS_PORT = 5400;

    //path to check Tor against
    String URL_TOR_CHECK = "https://check.torproject.org";

    //IPTABLES
//    public final static String CMD_IPTABLES_PREROUTING = "iptables -t nat -A OUTPUT -p tcp --dport 80 -j DNAT --to 127.0.0.1:8118 || exit\n";
    //public final static String CMD_IPTABLES_PREROUTING_FLUSH = "iptables -t nat -F || exit\n";

    //control port
    String TOR_CONTROL_PORT_MSG_BOOTSTRAP_DONE = "Bootstrapped 100%";

    int STATUS_OFF = 0;
 //   public final static int STATUS_READY = 0;
 int STATUS_ON = 1;
    int STATUS_CONNECTING = 2;

    int PROFILE_OFF = -1;
    int PROFILE_ON = 1;

    int STATUS_MSG = 1;
    int ENABLE_TOR_MSG = 2;
    int DISABLE_TOR_MSG = 3;
    int LOG_MSG = 4;
}
