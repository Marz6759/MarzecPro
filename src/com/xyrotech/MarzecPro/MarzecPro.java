package com.guardianproject.MarzecPro;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.xyrotech.MarzecPro.*;
import eu.chainfire.libsuperuser.Shell;

import java.io.*;

public class MarzecPro extends Activity implements OnCreateContextMenuListener {
    public static final String TAG = "marzecpro";

    private static TextView statusTitle;
    private static TextView statusText;
    private static Button startStopButton;
    private static ScrollView consoleScroll;
    private TextView consoleText;
    private static int savedStatus = -1;

    private Handler commandThreadHandler;
    private com.xyrotech.MarzecPro.MarzecPro_ac action;

    private boolean foundSU = false;
    Thread findSUThread = new Thread() {
        public void run() {
            foundSU = Shell.SU.available();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.MarzecPro);
        statusTitle = (TextView) findViewById(R.id.statusTitle);
        statusText = (TextView) findViewById(R.id.statusText);
        startStopButton = (Button) findViewById(R.id.startStopButton);
        consoleScroll = (ScrollView) findViewById(R.id.consoleScroll);
        consoleText = (TextView) findViewById(R.id.consoleText);

        // All SU calls are blocking calls. libsuperuser doen't allow
        // to make such blocking calls from Main Thread.
        findSUThread.start();

        commandThreadHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.arg1 == MarzecPro_ac.COMMAND_FINISHED)
                    updateScreenStatus();
                else if (msg.arg1 == MarzecPro_ac.LOG_UPDATE)
                    updateLog();
            }
        };

        action = new MarzecPro_ac(this, commandThreadHandler);

        if (savedInstanceState != null)
            MarzecPro_ac.log.append(savedInstanceState.getString("log"));
        else
            Log.i(TAG, "savedInstanceState was null");

        NativeHelper.installOrUpgradeAppBin(this);
        installBusyboxSymlinks();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // check again for SU
        if (!foundSU && !findSUThread.isAlive())
            findSUThread.start();
        if (NativeHelper.isInstallRunning) {
            // go back to the running install screen
            Intent intent = new Intent(this, InstallActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        updateScreenStatus();
        updateLog();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.layout.activity_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_jackpal_terminal).setEnabled(com.xyrotech.MarzecPro.NativeHelper.isStarted());
        menu.findItem(R.id.menu_install_log).setEnabled(com.xyrotech.MarzecPro.NativeHelper.install_log.exists());
        return true;
    }

    @SuppressLint("InlinedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_preferences:
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;
        case R.id.menu_install_log:
            startActivity(new Intent(this, InstallLogViewActivity.class));
            return true;
        case R.id.menu_jackpal_terminal:
            Intent i = new Intent("jackpal.androidterm.RUN_SCRIPT");
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.putExtra("jackpal.androidterm.iInitialCommand", "su -c \"PATH="
                    + NativeHelper.app_bin + ":$PATH "
                    + "chroot /debian /bin/bash -l\"");

                try {
                    startActivity(i);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, R.string.terminal_emulator_not_installed,
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=jackpal.androidterm"));
                    startActivity(intent);
                } catch (SecurityException e) {
                    /*
                     * For jackpal.androidterm.permission.RUN_SCRIPT to be set
                     * up in Android, Terminal Emulator must have been installed
                     * before Marzec was installed. That's just a limitation
                     * of the permissions system...
                     */
                    e.printStackTrace();
                    Toast.makeText(this, R.string.terminal_emulator_installed_after_MarzecPro,
                            Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.xyrotech.marzecpro"));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            return true;
        case R.id.menu_delete:
            new AlertDialog.Builder(this).setMessage(R.string.confirm_delete_message)
                    .setCancelable(false).setPositiveButton(R.string.doit,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    action.removeDebianSetup();
                                }
                            }).setNegativeButton(R.string.cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            }).show();
            return true;
        }
        return false;
    }

    private void updateScreenStatus() {
        startStopButton.setEnabled(true);
        setProgressBarIndeterminateVisibility(false);
        if (!NativeHelper.isSdCardPresent()
                && !NativeHelper.installInInternalStorage) {
            Toast.makeText(getApplicationContext(), R.string.no_sdcard_message,
                    Toast.LENGTH_LONG).show();
            statusTitle.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(R.string.no_sdcard_status);
            savedStatus = R.string.no_sdcard_status;
            startStopButton.setVisibility(View.GONE);
            return;
        }

        try {
            findSUThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (!foundSU) {
            statusTitle.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(R.string.needs_superuser_message);
            savedStatus = R.string.needs_superuser_message;
            startStopButton.setVisibility(View.VISIBLE);
            startStopButton.setText(R.string.get_superuser);
            startStopButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    final String APP_MARKET_URL = "market://details?id=com.koushikdutta.superuser";
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri
                            .parse(APP_MARKET_URL));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                }
            });
        } else if (NativeHelper.isInstalled()) {
            if (!new File(NativeHelper.mnt).exists()) {
                // we have a manually downloaded img file, config for it
                MarzecPro_ac.log.append(String.format(
                        getString(R.string.mount_point_not_found_format),
                        NativeHelper.mnt) + "\n");
                statusTitle.setVisibility(View.VISIBLE);
                statusText.setVisibility(View.VISIBLE);
                statusText.setText(R.string.not_configured_message);
                savedStatus = R.string.not_configured_message;
                startStopButton.setVisibility(View.VISIBLE);
                startStopButton.setText(R.string.title_configure);
                startStopButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        startStopButton.setEnabled(false);
                        setProgressBarIndeterminateVisibility(true);
                        action.configureDownloadedImage();
                    }
                });
            } else if (NativeHelper.isStarted()) {
                // we have a configured and mounted Debian setup, stop it
                statusTitle.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);
                statusText.setText(R.string.mounted_message);
                savedStatus = R.string.mounted_message;
                startStopButton.setVisibility(View.VISIBLE);
                startStopButton.setText(R.string.title_stop);
                startStopButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        startStopButton.setEnabled(false);
                        setProgressBarIndeterminateVisibility(true);
                        action.stopDebian();
                    }
                });
            } else {
                // we have a configured Debian setup that is not mounted, start it
                statusTitle.setVisibility(View.VISIBLE);
                statusText.setVisibility(View.VISIBLE);
                statusText.setText(R.string.not_mounted_message);
                savedStatus = R.string.not_mounted_message;
                startStopButton.setVisibility(View.VISIBLE);
                startStopButton.setText(R.string.title_start);
                startStopButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        startStopButton.setEnabled(false);
                        setProgressBarIndeterminateVisibility(true);
                        action.startDebian();
                    }
                });
            }
        } else if (! isOnline()) {
            statusTitle.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(R.string.no_network_message);
            savedStatus = R.string.no_network_message;
            startStopButton.setVisibility(View.GONE);
        } else {
            // we've got nothing, run the install
            statusTitle.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(R.string.not_installed_message);
            savedStatus = R.string.not_installed_message;
            startStopButton.setVisibility(View.VISIBLE);
            startStopButton.setText(R.string.install);
            startStopButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(),
                            InstallActivity.class);
                    startActivityForResult(intent, NativeHelper.STARTING_INSTALL);
                    return;
                }
            });
        }
    }

    public static void sdcardUnmounted() {
        if (startStopButton != null && !NativeHelper.isSdCardPresent()) {
            statusTitle.setVisibility(View.VISIBLE);
            statusText.setVisibility(View.VISIBLE);
            statusText.setText(R.string.no_sdcard_status);
            startStopButton.setVisibility(View.GONE);
        }
    }

    public static void sdcardMounted() {
        if (startStopButton != null && NativeHelper.isSdCardPresent()) {

            if (NativeHelper.isStarted()) {
                // marzecpro started after sdcard mounted
                savedStatus = R.string.mounted_message;
                startStopButton.setText(R.string.title_stop);
            } else if (savedStatus == R.string.mounted_message) {
                // marzecpro was running before sdcard unmounted
                savedStatus = R.string.not_mounted_message;
                startStopButton.setText(R.string.title_start);
            }

            if (savedStatus != -1) {
                statusTitle.setVisibility(View.VISIBLE);
                statusText.setVisibility(View.VISIBLE);
                statusText.setText(savedStatus);
            } else {
                // we don't know current status
                statusTitle.setVisibility(View.GONE);
                statusText.setVisibility(View.GONE);
            }
            startStopButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            return true;
        }
        return false;
    }

    private void updateLog() {
        final String logContents = MarzecPro_ac.log.toString();
        if (logContents != null && logContents.trim().length() > 0)
            consoleText.setText(logContents);
        consoleScroll.scrollTo(0, consoleText.getHeight());
    }

    private void installBusyboxSymlinks() {
        if (! NativeHelper.sh.exists()) {
            File busybox = new File(NativeHelper.app_bin, "busybox");
            if (!busybox.exists()) {
                String msg = "busybox is missing from the apk!";
                Log.e(TAG, msg);
                MarzecPro_ac.log.append(msg + "\n");
                return;
            }
            Log.i(TAG, "Installing busybox symlinks into " + NativeHelper.app_bin);
            // setup busybox so we have the utils we need, guaranteed
            String cmd = busybox.getAbsolutePath()
                    + " --install -s " + NativeHelper.app_bin.getAbsolutePath();
            Log.i(TAG, cmd);
            MarzecPro_ac.log.append("# " + cmd + "\n\n");
            // this can't use CommandThread because CommandThread depends on busybox sh
            try {
                Process sh = Runtime.getRuntime().exec("/system/bin/sh");
                OutputStream os = sh.getOutputStream();
                os.write(cmd.getBytes("ASCII"));
                os.write(";\nexit\n".getBytes("ASCII"));
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(sh.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null)
                    MarzecPro_ac.log.append(line);
            } catch (IOException e) {
                e.printStackTrace();
                MarzecPro_ac.log.append("Exception triggered by " + cmd);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
        savedInstanceState.putString("log", MarzecPro_ac.log.toString());
        super.onSaveInstanceState(savedInstanceState);
    }
    // the saved state is restored in onCreate()
}
