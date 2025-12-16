package com.example.quickbook.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class CredentialsManager {

    private static final String PREF_FILE_NAME = "quickbook_credentials";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_KEYWORDS = "keywords";

    private SharedPreferences sharedPreferences;

    public CredentialsManager(Context context) {
        try {
            String masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    PREF_FILE_NAME,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Could not create EncryptedSharedPreferences", e);
        }
    }

    public void saveCredentials(String email, String password) {
        sharedPreferences.edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply();
    }

    public void saveKeywords(String keywords) {
        sharedPreferences.edit().putString(KEY_KEYWORDS, keywords).apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(KEY_EMAIL, null);
    }

    public String getPassword() {
        return sharedPreferences.getString(KEY_PASSWORD, null);
    }

    public String getKeywords() {
        return sharedPreferences.getString(KEY_KEYWORDS, "");
    }

    public void clearCredentials() {
        sharedPreferences.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return getEmail() != null && getPassword() != null;
    }
}
