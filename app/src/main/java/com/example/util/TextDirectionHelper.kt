package com.example.util

import android.content.Context
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Configuration
import android.content.res.Resources
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

object TextDirectionHelper {

    /**
     * Inspects the first strong directional character of a string and returns
     * LayoutDirection.Rtl for Arabic/Hebrew/Persian scripts, or LayoutDirection.Ltr
     * for Latin/English scripts. This allows Arabic and English notifications to
     * align naturally in their original language regardless of the app UI locale.
     */
    fun detectTextLayoutDirection(
        text: String,
        fallbackDirection: LayoutDirection = LayoutDirection.Rtl
    ): LayoutDirection {
        for (char in text) {
            val directionality = Character.getDirectionality(char)
            when (directionality) {
                Character.DIRECTIONALITY_RIGHT_TO_LEFT,
                Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC,
                Character.DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING,
                Character.DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE -> return LayoutDirection.Rtl

                Character.DIRECTIONALITY_LEFT_TO_RIGHT,
                Character.DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING,
                Character.DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE -> return LayoutDirection.Ltr
            }
        }
        return fallbackDirection
    }

    fun isRtlText(text: String, defaultRtl: Boolean = true): Boolean {
        val fallback = if (defaultRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
        return detectTextLayoutDirection(text, fallback) == LayoutDirection.Rtl
    }

    /**
     * Wraps an Android Context with the user's selected locale ("ar" by default, or "en")
     * while preserving the ContextWrapper chain (such as ComponentActivity / ActivityResultRegistryOwner).
     */
    fun createLocalizedContext(baseContext: Context, languageCode: String): Context {
        val cleanLang = if (languageCode == "en") "en" else "ar"
        val locale = Locale(cleanLang)
        Locale.setDefault(locale)
        val config = Configuration(baseContext.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        val configContext = baseContext.createConfigurationContext(config)
        return object : ContextWrapper(baseContext) {
            override fun getResources(): Resources = configContext.resources
            override fun getAssets(): AssetManager = configContext.assets
        }
    }
}
