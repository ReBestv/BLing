package com.standbyus.app.di;

import com.standbyus.app.data.local.AppDatabase;
import com.standbyus.app.data.local.StatusDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideStatusDaoFactory implements Factory<StatusDao> {
  private final Provider<AppDatabase> databaseProvider;

  public AppModule_ProvideStatusDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public StatusDao get() {
    return provideStatusDao(databaseProvider.get());
  }

  public static AppModule_ProvideStatusDaoFactory create(Provider<AppDatabase> databaseProvider) {
    return new AppModule_ProvideStatusDaoFactory(databaseProvider);
  }

  public static StatusDao provideStatusDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideStatusDao(database));
  }
}
