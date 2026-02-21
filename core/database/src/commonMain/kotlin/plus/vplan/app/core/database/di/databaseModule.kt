package plus.vplan.app.core.database.di

import androidx.room.RoomDatabase
import org.koin.core.module.Module
import org.koin.dsl.module
import plus.vplan.app.core.database.VppDatabase

val databaseModule = module {
    includes(roomModule)

    single { get<VppDatabase>().besteSchuleYearDao }
    single { get<VppDatabase>().vppIdDao }
}

expect val roomModule: Module

internal fun RoomDatabase.Builder<VppDatabase>.config(): RoomDatabase.Builder<VppDatabase> = this
    .addMigrations(VppDatabase.Migration8to9)
    .addMigrations(VppDatabase.Migration9to10)