package com.standbyus.app.ui.home;

import android.content.Context;
import com.standbyus.app.data.remote.SupabaseService;
import com.standbyus.app.data.repository.PairingRepository;
import com.standbyus.app.data.repository.StatusRepository;
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
public final class HomeViewModel_Factory implements Factory<HomeViewModel> {
  private final Provider<StatusRepository> statusRepositoryProvider;

  private final Provider<PairingRepository> pairingRepositoryProvider;

  private final Provider<SupabaseService> supabaseServiceProvider;

  private final Provider<Context> contextProvider;

  public HomeViewModel_Factory(Provider<StatusRepository> statusRepositoryProvider,
      Provider<PairingRepository> pairingRepositoryProvider,
      Provider<SupabaseService> supabaseServiceProvider, Provider<Context> contextProvider) {
    this.statusRepositoryProvider = statusRepositoryProvider;
    this.pairingRepositoryProvider = pairingRepositoryProvider;
    this.supabaseServiceProvider = supabaseServiceProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public HomeViewModel get() {
    return newInstance(statusRepositoryProvider.get(), pairingRepositoryProvider.get(), supabaseServiceProvider.get(), contextProvider.get());
  }

  public static HomeViewModel_Factory create(Provider<StatusRepository> statusRepositoryProvider,
      Provider<PairingRepository> pairingRepositoryProvider,
      Provider<SupabaseService> supabaseServiceProvider, Provider<Context> contextProvider) {
    return new HomeViewModel_Factory(statusRepositoryProvider, pairingRepositoryProvider, supabaseServiceProvider, contextProvider);
  }

  public static HomeViewModel newInstance(StatusRepository statusRepository,
      PairingRepository pairingRepository, SupabaseService supabaseService, Context context) {
    return new HomeViewModel(statusRepository, pairingRepository, supabaseService, context);
  }
}
