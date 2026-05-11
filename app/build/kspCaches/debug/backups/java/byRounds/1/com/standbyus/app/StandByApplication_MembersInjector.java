package com.standbyus.app;

import com.standbyus.app.data.remote.SupabaseService;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class StandByApplication_MembersInjector implements MembersInjector<StandByApplication> {
  private final Provider<SupabaseService> supabaseServiceProvider;

  public StandByApplication_MembersInjector(Provider<SupabaseService> supabaseServiceProvider) {
    this.supabaseServiceProvider = supabaseServiceProvider;
  }

  public static MembersInjector<StandByApplication> create(
      Provider<SupabaseService> supabaseServiceProvider) {
    return new StandByApplication_MembersInjector(supabaseServiceProvider);
  }

  @Override
  public void injectMembers(StandByApplication instance) {
    injectSupabaseService(instance, supabaseServiceProvider.get());
  }

  @InjectedFieldSignature("com.standbyus.app.StandByApplication.supabaseService")
  public static void injectSupabaseService(StandByApplication instance,
      SupabaseService supabaseService) {
    instance.supabaseService = supabaseService;
  }
}
