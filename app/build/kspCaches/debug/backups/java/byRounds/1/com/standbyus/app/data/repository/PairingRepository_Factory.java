package com.standbyus.app.data.repository;

import com.standbyus.app.data.remote.SupabaseService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class PairingRepository_Factory implements Factory<PairingRepository> {
  private final Provider<SupabaseService> supabaseServiceProvider;

  public PairingRepository_Factory(Provider<SupabaseService> supabaseServiceProvider) {
    this.supabaseServiceProvider = supabaseServiceProvider;
  }

  @Override
  public PairingRepository get() {
    return newInstance(supabaseServiceProvider.get());
  }

  public static PairingRepository_Factory create(
      Provider<SupabaseService> supabaseServiceProvider) {
    return new PairingRepository_Factory(supabaseServiceProvider);
  }

  public static PairingRepository newInstance(SupabaseService supabaseService) {
    return new PairingRepository(supabaseService);
  }
}
