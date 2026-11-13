/*
 * Copyright (C) 2023-2025 The Clover Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.util.neko;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Per-package allowlist used to report the developer/ADB settings as disabled
 * to selected applications.
 *
 * <p>The package list lives in {@link Settings.Secure#HIDE_DEVELOPER_STATUS} as a
 * comma separated string. Framework accessors for the settings in
 * {@link #settingsToHide} consult {@link #shouldHideDevStatus} and return "0" for
 * packages on the list, which makes both the string and the numeric getters report
 * the setting as disabled without changing the real value.
 *
 * <p>Ported from The Clover Project, originally authored by someone5678 and later
 * improved by rmp22.
 */
public class HideDeveloperStatusUtils {

    private static final Set<String> settingsToHide =
        new HashSet<>(
            Arrays.asList(
                Settings.Global.ADB_ENABLED,
                Settings.Global.ADB_WIFI_ENABLED,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED
            ));

    enum Action {
        ADD,
        REMOVE,
        SET
    }

    public static boolean shouldHideDevStatus(
            ContentResolver cr, String packageName, String name) {
        if (cr == null || packageName == null || name == null) {
            return false;
        }

        if (!settingsToHide.contains(name)) return false;

        Set<String> apps = getApps(cr);
        if (apps.isEmpty()) {
            return false;
        }

        return apps.contains(packageName);
    }

    private static Set<String> getApps(Context context) {
        if (context == null) {
            return new HashSet<>();
        }

        return getApps(context.getContentResolver());
    }

    private static Set<String> getApps(ContentResolver cr) {
        if (cr == null) {
            return new HashSet<>();
        }

        String apps = Settings.Secure.getString(cr, Settings.Secure.HIDE_DEVELOPER_STATUS);
        if (apps != null && !apps.isEmpty() && !apps.equals(",")) {
            return new HashSet<>(Arrays.asList(apps.split(",")));
        }

        return new HashSet<>();
    }

    private static void putAppsForUser(
            Context context, String packageName,
            int userId, Action action) {
        if (context == null || userId < 0) {
            return;
        }

        final Set<String> apps = getApps(context);
        switch (action) {
            case ADD:
                apps.add(packageName);
                break;
            case REMOVE:
                apps.remove(packageName);
                break;
            case SET:
                // Don't change
                break;
        }

        Settings.Secure.putStringForUser(context.getContentResolver(),
                Settings.Secure.HIDE_DEVELOPER_STATUS, String.join(",", apps), userId);
    }

    public void addApp(Context mContext, String packageName, int userId) {
        if (mContext == null || packageName == null || userId < 0) {
            return;
        }

        putAppsForUser(mContext, packageName, userId, Action.ADD);
    }

    public void removeApp(Context mContext, String packageName, int userId) {
        if (mContext == null || packageName == null || userId < 0) {
            return;
        }

        putAppsForUser(mContext, packageName, userId, Action.REMOVE);
    }

    public void setApps(Context mContext, int userId) {
        if (mContext == null || userId < 0) {
            return;
        }

        putAppsForUser(mContext, null, userId, Action.SET);
    }
}
