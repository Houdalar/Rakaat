package com.example.rakaat.data

import android.content.Context
import android.bluetooth.BluetoothAdapter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideBluetoothAdapter(): BluetoothAdapter =
        BluetoothAdapter.getDefaultAdapter()

    @Provides
    @Singleton
    fun provideBluetoothReceiver(@ApplicationContext context: Context, bluetoothAdapter: BluetoothAdapter): BluetoothReceiver =
        BluetoothReceiver(context, bluetoothAdapter)
}