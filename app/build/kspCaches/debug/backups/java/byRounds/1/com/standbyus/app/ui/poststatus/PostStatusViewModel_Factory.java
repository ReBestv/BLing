package com.standbyus.app.ui.poststatus;

import com.standbyus.app.data.remote.SupabaseService;
import com.standbyus.app.data.repository.StatusRepository;
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
public final class PostStatusViewModel_Factory implements Factory<PostStatusViewModel> {
  private final Provider<StatusRepository> statusRepositoryProvider;

  private final Provider<SupabaseService> supabaseServiceProvider;

  public PostStatusViewModel_Factory(Provider<StatusRepository> statusRepositoryProvider,
      Provider<SupabaseService> supabaseServiceProvider) {
    this.statusRepositoryProvider = statusRepositoryProvider;
    this.supabaseServiceProvider = supabaseServiceProvider;
  }

  @Override
  public PostStatusViewModel get() {
    return newInstance(statusRepositoryProvider.get(), supabaseServiceProvider.get());
  }

  public static PostStatusViewModel_Factory create(
      Provider<StatusRepository> statusRepositoryProvider,
      Provider<SupabaseService> supabaseServiceProvider) {
    return new PostStatusViewModel_Factory(statusRepositoryProvider, supabaseServiceProvider);
  }

  public static PostStatusViewModel newInstance(StatusRepository statusRepository,
      SupabaseService supabaseService) {
    return new PostStatusViewModel(statusRepository, supabaseService);
  }
}
