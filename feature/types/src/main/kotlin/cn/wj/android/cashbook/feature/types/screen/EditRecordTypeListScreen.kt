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

package cn.wj.android.cashbook.feature.types.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import cn.wj.android.cashbook.core.design.icon.CbIcons
import cn.wj.android.cashbook.core.model.entity.RECORD_TYPE_SETTINGS
import cn.wj.android.cashbook.core.model.entity.RecordTypeEntity
import cn.wj.android.cashbook.core.model.enums.RecordTypeCategoryEnum
import cn.wj.android.cashbook.core.ui.expand.typeColor
import cn.wj.android.cashbook.feature.types.viewmodel.EditRecordTypeListViewModel

/**
 * 编辑记录页面标签列表
 *
 * @param typeCategory 记录大类
 * @param defaultTypeId 默认类型 id
 * @param onTypeSelect 类型选中回调
 */
@Composable
internal fun EditRecordTypeListRoute(
    typeCategory: RecordTypeCategoryEnum,
    defaultTypeId: Long,
    onTypeSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditRecordTypeListViewModel = hiltViewModel<EditRecordTypeListViewModel>().apply {
        update(typeCategory, defaultTypeId)
    },
) {
    val currentTypeCategory by viewModel.currentTypeCategoryData.collectAsStateWithLifecycle()
    val typeList by viewModel.typeListData.collectAsStateWithLifecycle()

    val currentSelectedTypeId by viewModel.currentSelectedTypeId.collectAsStateWithLifecycle()

    LaunchedEffect(currentSelectedTypeId) {
        if (currentSelectedTypeId != -1L) {
            onTypeSelect(currentSelectedTypeId)
        }
    }

    EditRecordTypeListScreen(
        currentTypeCategory = currentTypeCategory,
        typeList = typeList,
        onTypeSelect = viewModel::updateTypeId,
        modifier = modifier,
    )
}

/**
 * 编辑记录页面标签列表
 *
 * @param onTypeSelect 类型选中回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EditRecordTypeListScreen(
    currentTypeCategory: RecordTypeCategoryEnum,
    typeList: List<RecordTypeEntity>,
    onTypeSelect: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeColor = currentTypeCategory.typeColor
    val firstTypeList = typeList.filter { it.parentId == -1L && it != RECORD_TYPE_SETTINGS }
    val expandedStateMap = remember { mutableStateMapOf<Long, Boolean>() }

    Column(modifier = modifier) {
        firstTypeList.forEach { first ->
            // 一级类型：单独占一行
            val hasChild = first.child.isNotEmpty()
            val defaultExpanded = first.child.any { it.selected }
            val isExpanded = expandedStateMap[first.id] ?: defaultExpanded

            androidx.compose.foundation.layout.Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .then(
                        if (hasChild) {
                            Modifier.clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ) {
                                expandedStateMap[first.id] = !isExpanded
                            }
                        } else {
                            Modifier
                        },
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = first.name,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (hasChild) {
                    Icon(
                        imageVector = if (isExpanded) {
                            CbIcons.KeyboardArrowDown
                        } else {
                            CbIcons.KeyboardArrowRight
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (hasChild && isExpanded) {
                // 二级类型：默认合上，可展开
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp),
                ) {
                    first.child.forEach { second ->
                        ElevatedFilterChip(
                            selected = second.selected,
                            onClick = {
                                if (!second.selected) {
                                    onTypeSelect(second.id)
                                } else {
                                    val parent =
                                        firstTypeList.firstOrNull { it.id == second.parentId }
                                            ?: firstTypeList.firstOrNull()
                                    parent?.let { onTypeSelect(it.id) }
                                }
                            },
                            label = { Text(text = second.name) },
                            modifier = Modifier,
                            shape = RoundedCornerShape(6.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = typeColor,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                            ),
                        )
                    }
                }
            }
        }

    }
}
