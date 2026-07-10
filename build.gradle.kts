// Top-level build file. Plugins are declared here and applied per-module.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    id("com.google.gms.google-services") version "4.5.0" apply false
    id("com.google.firebase.appdistribution") version "5.1.1" apply false
}
