package com.standbyus.app.ui.history;

import com.standbyus.app.data.remote.SupabaseService;
import com.standbyus.app.data.repository.PairingRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
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
public final class HistoryViewModel_Factory implements Factory<HistoryViewModel> {
  private final Provider<SupabaseService> supabaseServiceProvider;

  private final Provider<PairingRepository> pairingRepositoryProvider;

  public HistoryViewModel_Factory(Provider<SupabaseService> supabaseServiceProvider,
      Provider<PairingRepository> pairingRepositoryProvider) {
    this.supabaseServiceProvider = supabaseServiceProvider;
    this.pairingRepositoryProvider = pairingRepositoryProvider;
  }

  @Override
  public HistoryViewModel get() {
    return newInstance(supabaseServiceProvider.get(), pairingRepositoryProvider.get());
  }

  public static HistoryViewModel_Factory create(Provider<SupabaseService> supabaseServiceProvider,
      Provider<PairingRepository> pairingRepositoryProvider) {
    return new HistoryViewModel_Factory(supabaseServiceProvider, pairingRepositoryProvider);
  }

  public static HistoryViewModel newInstance(SupabaseService supabaseService,
      PairingRepository pairingRepository) {
    return new HistoryViewModel(supabaseService, pairingRepository);
  }
}
