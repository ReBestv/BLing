package com.standbyus.app.data.repository;

import android.content.Context;
import com.standbyus.app.data.local.StatusDao;
import com.standbyus.app.data.remote.SupabaseService;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class StatusRepository_Factory implements Factory<StatusRepository> {
  private final Provider<SupabaseService> supabaseServiceProvider;

  private final Provider<StatusDao> statusDaoProvider;

  private final Provider<Context> contextProvider;

  public StatusRepository_Factory(Provider<SupabaseService> supabaseServiceProvider,
      Provider<StatusDao> statusDaoProvider, Provider<Context> contextProvider) {
    this.supabaseServiceProvider = supabaseServiceProvider;
    this.statusDaoProvider = statusDaoProvider;
    this.contextProvider = contextProvider;
  }

  @Override
  public StatusRepository get() {
    return newInstance(supabaseServiceProvider.get(), statusDaoProvider.get(), contextProvider.get());
  }

  public static StatusRepository_Factory create(Provider<SupabaseService> supabaseServiceProvider,
      Provider<StatusDao> statusDaoProvider, Provider<Context> contextProvider) {
    return new StatusRepository_Factory(supabaseServiceProvider, statusDaoProvider, contextProvider);
  }

  public static StatusRepository newInstance(SupabaseService supabaseService, StatusDao statusDao,
      Context context) {
    return new StatusRepository(supabaseService, statusDao, context);
  }
}
