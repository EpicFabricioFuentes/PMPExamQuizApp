package com.fax.passyourpmpexam.core.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Migration-test scaffolding for [PmpDatabase]. The schema is exported to `core/data/schemas` (see
 * the `room { schemaDirectory(...) }` block), which the Room Gradle plugin bundles into the
 * androidTest APK so [MigrationTestHelper] can find it.
 *
 * There are no migrations yet (schema version 1), so [schemaV1CreatesCleanly] just proves the helper
 * and the exported schema are wired correctly. When the first schema change lands, bump
 * [PmpDatabase] to version 2 with a `Migration(1, 2)` and add a test following the template in
 * [migrate1To2] below.
 */
@RunWith(AndroidJUnit4::class)
class MigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PmpDatabase::class.java,
    )

    @Test
    fun schemaV1CreatesCleanly() {
        // Creates the database at version 1 from the exported schema and validates it. Throws if the
        // exported schema is missing or malformed, so this guards the migration-test plumbing itself.
        helper.createDatabase(TEST_DB, 1).close()
    }

    // Template for the first real migration — uncomment and adapt once PmpDatabase reaches version 2:
    //
    // @Test
    // fun migrate1To2() {
    //     helper.createDatabase(TEST_DB, 1).apply {
    //         // Seed version-1 rows with execSQL(...) to assert they survive the migration.
    //         close()
    //     }
    //     val db = helper.runMigrationsAndValidate(TEST_DB, 2, /* validateDroppedTables = */ true, MIGRATION_1_2)
    //     // Assert the migrated data / new columns here.
    //     db.close()
    // }

    private companion object {
        const val TEST_DB = "migration-test"
    }
}
