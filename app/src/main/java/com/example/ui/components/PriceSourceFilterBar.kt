package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PriceSource
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold

@Composable
fun PriceSourceFilterBar(
    selectedSource: PriceSource?,
    onSelectSource: (PriceSource?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val sources = PriceSource.values()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("price_source_filter_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "انتخاب منبع قیمت از سایت‌ها:",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = FintechGold
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (selectedSource == null) "همه منابع فعال" else selectedSource.nameFa,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = colors.textMuted
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // "همه سایت‌ها" Chip
            val isAllSelected = selectedSource == null
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isAllSelected) FintechCyan else colors.surfaceLight)
                    .border(
                        1.dp,
                        if (isAllSelected) FintechCyan else colors.surfaceBorder,
                        RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelectSource(null) }
                    .padding(horizontal = 12.dp, vertical = 7.dp)
                    .testTag("source_chip_all"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🌐 همه سایت‌ها",
                    fontSize = 11.sp,
                    fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAllSelected) Color.Black else colors.textPrimary
                )
            }

            // Individual Price Sources
            sources.forEach { source ->
                val isSelected = selectedSource == source
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) FintechGold else colors.surfaceLight)
                        .border(
                            1.dp,
                            if (isSelected) FintechGold else colors.surfaceBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelectSource(source) }
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .testTag("source_chip_${source.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = source.icon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = source.shortName,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else colors.textPrimary
                        )
                    }
                }
            }
        }
    }
}
