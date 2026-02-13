/*
 * Copyright 2021 The Cashbook Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.wj.android.cashbook.core.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cn.wj.android.cashbook.core.common.ext.logger
import org.intellij.lang.annotations.Language

/**
 * 数据库升级 11 -> 12
 * - db_record：新增记录星期几字段
 *
 * > [王杰](mailto:15555650921@163.com) 创建于 2026/2/13
 */
object Migration11To12 : Migration(11, 12) {

    override fun migrate(db: SupportSQLiteDatabase) {
        logger().i("migrate(db)")
        with(db) {
            migrateRecordWeekday()
        }
    }

    /** 添加记录星期几字段 */
    @Language("SQL")
    private const val SQL_ADD_RECORD_WEEKDAY = """
        ALTER TABLE `db_record` ADD COLUMN `record_weekday` TEXT NOT NULL DEFAULT ''
    """

    /** 根据 record_time 填充星期几 */
    @Language("SQL")
    private const val SQL_UPDATE_RECORD_WEEKDAY = """
        UPDATE `db_record`
        SET `record_weekday` = CASE strftime('%w', `record_time` / 1000, 'unixepoch', 'localtime')
            WHEN '0' THEN '周日'
            WHEN '1' THEN '周一'
            WHEN '2' THEN '周二'
            WHEN '3' THEN '周三'
            WHEN '4' THEN '周四'
            WHEN '5' THEN '周五'
            WHEN '6' THEN '周六'
            ELSE ''
        END
    """

    private fun SupportSQLiteDatabase.migrateRecordWeekday() {
        execSQL(SQL_ADD_RECORD_WEEKDAY)
        execSQL(SQL_UPDATE_RECORD_WEEKDAY)
    }
}
