package ai.folded.fitstyle.di

import ai.folded.fitstyle.data.AppDatabase
import ai.folded.fitstyle.data.StyledImageDao
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    fun provideStyledImageDao(appDatabase: AppDatabase): StyledImageDao {
        return appDatabase.styledImageDao()
    }
}