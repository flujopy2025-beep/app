package com.seoaudit.app.di

import com.seoaudit.app.core.domain.repository.AiRepository
import com.seoaudit.app.core.domain.repository.FileSystemRepository
import com.seoaudit.app.core.domain.repository.GscRepository
import com.seoaudit.app.core.domain.repository.McpRepository
import com.seoaudit.app.core.domain.repository.WordPressRepository
import com.seoaudit.app.feature.ai.data.AiRepositoryImpl
import com.seoaudit.app.feature.codegen.data.DiffEngineImpl
import com.seoaudit.app.feature.codegen.domain.engine.DiffEngine
import com.seoaudit.app.feature.filesystem.data.SafFileRepository
import com.seoaudit.app.feature.gsc.data.GscRepositoryImpl
import com.seoaudit.app.feature.mcp.data.McpClientImpl
import com.seoaudit.app.feature.wordpress.data.WordPressRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGscRepository(impl: GscRepositoryImpl): GscRepository

    @Binds
    @Singleton
    abstract fun bindWordPressRepository(impl: WordPressRepositoryImpl): WordPressRepository

    @Binds
    @Singleton
    abstract fun bindMcpRepository(impl: McpClientImpl): McpRepository

    @Binds
    @Singleton
    abstract fun bindAiRepository(impl: AiRepositoryImpl): AiRepository

    @Binds
    @Singleton
    abstract fun bindFileSystemRepository(impl: SafFileRepository): FileSystemRepository

    @Binds
    @Singleton
    abstract fun bindDiffEngine(impl: DiffEngineImpl): DiffEngine
}
