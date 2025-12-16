package com.example.quickbook.ui.account;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

import com.example.quickbook.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class LanguageSelectionBottomSheet extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_language, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.buttonEnglish).setOnClickListener(v -> {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags("en");
            AppCompatDelegate.setApplicationLocales(appLocale);
            dismiss();
        });

        view.findViewById(R.id.buttonPortuguese).setOnClickListener(v -> {
            LocaleListCompat appLocale = LocaleListCompat.forLanguageTags("pt-BR");
            AppCompatDelegate.setApplicationLocales(appLocale);
            dismiss();
        });
    }
}
