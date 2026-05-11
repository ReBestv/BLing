package com.standbyus.app.data.remote;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class SupabaseService_Factory implements Factory<SupabaseService> {
  @Override
  public SupabaseService get() {
    return newInstance();
  }

  public static SupabaseService_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static SupabaseService newInstance() {
    return new SupabaseService();
  }

  private static final class InstanceHolder {
    private static final SupabaseService_Factory INSTANCE = new SupabaseService_Factory();
  }
}
