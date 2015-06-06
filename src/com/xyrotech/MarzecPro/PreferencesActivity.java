package com.xyrotech.MarzecPro;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.util.Log;
import android.widget.Toast;

public class PreferencesActivity extends android.preference.PreferenceActivity
        implements OnSharedPreferenceChangeListener {
    CheckBoxPreference useChecksumCheckBox;
    CheckBoxPreference installOnInternalStorageBox;
    CheckBoxPreference limitTo4gbCheckBox;
    CheckBoxPreference startAutomatically;
    EditTextPreference imagepathEditText;
    EditTextPreference postStartEditText;
    EditTextPreference preStopEditText;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.pref_image_path_key))) {
            String path = prefs.getString(key, NativeHelper.default_image_path);
            File f = new File(path);
            File folder = f.getParentFile();
            if (f.exists())
                Toast.makeText(this, R.string.image_path_exists_message, Toast.LENGTH_LONG).show();
            if (path == null
                    || path.length() < 2 // the shortest possible path is "/a"
                    || folder == null
                    || ! folder.exists()) {
                NativeHelper.image_path = NativeHelper.default_image_path;
                imagepathEditText.setText(NativeHelper.image_path);
                Toast.makeText(this, R.string.image_path_parent_does_not_exist_message,
                        Toast.LENGTH_LONG).show();
            } else
                NativeHelper.image_path = prefs.getString(key, NativeHelper.default_image_path);
            setSummaries();
        } else if (key.equals(getString(R.string.pref_post_start_key))) {
            NativeHelper.postStartScript = prefs.getString(key,
                    getString(R.string.default_post_start_script));
            setSummaries();
        } else if (key.equals(getString(R.string.pref_pre_stop_key))) {
            NativeHelper.preStopScript = prefs.getString(key,
                    getString(R.string.default_pre_stop_script));
            setSummaries();
        } else if (key.equals(getString(R.string.pref_use_checksum_key))) {
            Boolean checked = prefs.getBoolean(getString(R.string.pref_use_checksum_key), false);
            if (checked)
                Toast.makeText(this, R.string.calculating_checksum,
                        Toast.LENGTH_LONG).show();
            new ShellAsyncTask().execute(checked);
        } else if (key.equals(getString(R.string.pref_install_on_internal_storage_key))) {
            Boolean checked = prefs.getBoolean(
                    getString(R.string.pref_install_on_internal_storage_key), false);

            if (checked) {
                NativeHelper.installInInternalStorage = true;
                NativeHelper.image_path = NativeHelper.mnt;
                limitTo4gbCheckBox.setEnabled(false);
                useChecksumCheckBox.setEnabled(false);
                imagepathEditText.setEnabled(false);
            } else {
                NativeHelper.installInInternalStorage = false;
                NativeHelper.image_path = imagepathEditText.getText();
                limitTo4gbCheckBox.setEnabled(true);
                useChecksumCheckBox.setEnabled(true);
                imagepathEditText.setEnabled(true);
            }
            setSummaries();
        } else if (key.equals(getString(R.string.pref_limit_to_4gb_key))) {
            NativeHelper.limitTo4GB = prefs.getBoolean(
                    getString(R.string.pref_limit_to_4gb_key), true);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        startAutomatically = (CheckBoxPreference) findPreference(getString(R.string.pref_start_automatically_key));
        imagepathEditText = (EditTextPreference) findPreference(getString(R.string.pref_image_path_key));
        postStartEditText = (EditTextPreference) findPreference(getString(R.string.pref_post_start_key));
        preStopEditText = (EditTextPreference) findPreference(getString(R.string.pref_pre_stop_key));
        useChecksumCheckBox = (CheckBoxPreference) findPreference(getString(R.string.pref_use_checksum_key));
        installOnInternalStorageBox = (CheckBoxPreference) findPreference(getString(R.string.pref_use_checksum_key));
        limitTo4gbCheckBox = (CheckBoxPreference) findPreference(getString(R.string.pref_limit_to_4gb_key));

        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        Boolean checked = prefs.getBoolean(
                getString(R.string.pref_install_on_internal_storage_key), false);

        if (checked) {
            NativeHelper.installInInternalStorage = true;
            NativeHelper.image_path = NativeHelper.mnt;
            useChecksumCheckBox.setEnabled(false);
            imagepathEditText.setEnabled(false);
            limitTo4gbCheckBox.setEnabled(false);
            setSummaries();
        }

        if (NativeHelper.installInInternalStorage)
            startAutomatically
                    .setSummary(getString(R.string.pref_start_on_boot_summary));
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getPreferenceScreen().getSharedPreferences();
        prefs.registerOnSharedPreferenceChangeListener(this);
        setSummaries();
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setSummaries() {
        // this is extra complicated because R.string.default_image_path can only be set
        // statically in preferences.xml, but the actual default image_path is based on
        // the "external storage" location, which might not always be /sdcard. Also
        // some devices don't have the /sdcard symlink.
        if (NativeHelper.installInInternalStorage) {
            imagepathEditText.setSummary(getString(R.string.install_in_internal_storage_summary));
            startAutomatically
                    .setSummary(getString(R.string.pref_start_on_boot_summary));
        }
        else if (NativeHelper.image_path.equals(NativeHelper.default_image_path)
                || NativeHelper.image_path.equals(getString(R.string.default_image_path))) {
            imagepathEditText
                    .setSummary(getString(R.string.pref_image_path_summary));
            startAutomatically
                    .setSummary(getString(R.string.pref_start_on_mount_summary));
        } else {
            imagepathEditText.setSummary(NativeHelper.image_path);
            startAutomatically
                    .setSummary(getString(R.string.pref_start_on_mount_summary));
        }
        if (NativeHelper.postStartScript
                .equals(getString(R.string.default_post_start_script))) {
            postStartEditText
                    .setSummary(getString(R.string.pref_post_start_summary));
        } else {
            postStartEditText.setSummary(NativeHelper.postStartScript);
        }
        if (NativeHelper.preStopScript
                .equals(getString(R.string.default_pre_stop_script))) {
            preStopEditText
                    .setSummary(getString(R.string.pref_pre_stop_summary));
        } else {
            preStopEditText.setSummary(NativeHelper.preStopScript);
        }
    }

    private void showCompleteToast() {
        Toast.makeText(this, R.string.checksum_complete, Toast.LENGTH_LONG).show();
    }

    private class ShellAsyncTask extends AsyncTask<Boolean, Void, Void> {

        @Override
        protected Void doInBackground(Boolean... values) {
            String app_bin = NativeHelper.app_bin.getAbsolutePath();
            File sha1file = new File(app_bin + "/../" + new File(NativeHelper.image_path + ".sha1").getName());
            String command;
            try {
                if (values[0]) {
                    if (NativeHelper.isStarted()) {
                        // if mounted, just make a blank file, then
                        // stop-marzec.sh
                        // will do the sha1sum
                        sha1file.createNewFile();
                        command = app_bin + "/chmod 0600 " + sha1file;
                        Log.i(MarzecPro.TAG, command);
                        Runtime.getRuntime().exec(command);
                    } else {
                        command = app_bin + "/sha1sum " + NativeHelper.image_path;
                        Log.i(MarzecPro.TAG, command);
                        MarzecPro_ac.log.append(command + "\n");
                        Process p = Runtime.getRuntime().exec(command);
                        p.waitFor();
                        InputStreamReader isr = new InputStreamReader(p.getInputStream());
                        BufferedReader br = new BufferedReader(isr);
                        FileWriter outFile = new FileWriter(sha1file);
                        PrintWriter out = new PrintWriter(outFile);
                        out.println(br.readLine());
                        out.close();
                        br.close();
                    }
                } else {
                    sha1file.delete();
                }
            } catch (Exception e) {
                String msg = e.getLocalizedMessage();
                MarzecPro_ac.log.append("Error with checksum file: " + msg);
                Log.e(MarzecPro.TAG, "Error with checksum file: " + msg);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            showCompleteToast();
        }
    }
}
