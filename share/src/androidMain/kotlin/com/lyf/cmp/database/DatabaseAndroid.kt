package com.lyf.cmp.database

import android.content.Context
import androidx.room.Room

internal object DatabaseAndroid {
    fun createDatabase(context: Context): AppDatabase =
        Room.databaseBuilder<AppDatabase>(
            context = context.applicationContext,
            name = context.applicationContext.getDatabasePath(DATABASE_NAME).absolutePath,
        ).buildAppDatabase()
}
