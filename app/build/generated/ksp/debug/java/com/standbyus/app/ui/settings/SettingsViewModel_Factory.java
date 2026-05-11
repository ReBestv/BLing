package com.standbyus.app.ui.settings;

import android.content.Context;
import com.standbyus.app.data.remote.SupabaseService;
import com.standbyus.app.data.repository.PairingRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<PairingRepository> pairingRepositoryProvider;

  private final Provider<SupabaseService> supabaseServiceProvider;

  private final Provider<Context> contextProvider;

  public SettingsViewModel_Factory(Provider<PairingRepository> pairingRepositoryProvider,
      Provider<SupabaseService> supabaseServiceProvider, Provider<Context> contextProvider) {
    this.pairingRepositoryProvider = pairingRepositoryProvider;
    this.supabaseServiceProvider = supabaseServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(pairingRepositoryProvider.get(), supabaseServiceProvider.get(), contextProvider.get());
  }

  public static SettingsViewModel_Factory create(
      Provider<PairingRepository> pairingRepositoryProvider,
      Provider<SupabaseService> supabaseServiceProvider, Provider<Context> contextProvider) {
    return new SettingsViewModel_Factory(pairingRepositoryProvider, supabaseServiceProvider, contextProvider);
  }

  public static SettingsViewModel newInstance(PairingRepository pairingRepository,
      SupabaseService supabaseService, Context context) {
    return new SettingsViewModel(pairingRepository, supabaseService, context);
  }
}
