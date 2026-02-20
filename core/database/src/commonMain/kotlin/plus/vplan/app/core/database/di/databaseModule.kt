package plus.vplan.app.core.database.di

import androidx.room.RoomDatabase
import org.koin.core.module.Module
import plus.vplan.app.core.database.VppDatabase

expect val databaseModule: Module

internal fun RoomDatabase.Builder<VppDatabase>.config(): RoomDatabase.Builder<VppDatabase> = this
    .addMigrations(VppDatabase.Migration8to9)
    .addMigrations(VppDatabase.Migration9to10)