package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Cover Page Generator Card designed with the EXACT same aesthetic as the
 * workspace HubGridCards (Exams, Assignments, CGPA Calculator, Routine, Focus Timer).
 * Pure white card in light mode, deep navy in dark mode, matching rounded corners,
 * squircle icon container, pastel aura, and clean typography.
 */
@Composable
fun CoverPageHubBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val containerColor = if (isDark) Color(0xFF131B2E) else Color.White
    val titleColor = if (isDark) Color.White else Color(0xFF0F172A)
    val subtitleColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val chevronTint = if (isDark) Color(0xFF64748B) else Color(0xFF94A3B8)
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    val iconContainerColor = if (isDark) Color(0xFF312E81) else Color(0xFFEEF2FF)
    val iconTint = if (isDark) Color(0xFFA5B4FC) else Color(0xFF4F46E5)

    val shadowElevation = if (isDark) 3.dp else 8.dp
    val shadowSpotColor = if (isDark) Color(0x60000000) else Color(0x3E0F172A)
    val shadowAmbientColor = if (isDark) Color(0x40000000) else Color(0x221E293B)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(98.dp)
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(20.dp),
                spotColor = shadowSpotColor,
                ambientColor = shadowAmbientColor
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("hub_cover_page_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 3.dp else 6.dp,
            pressedElevation = 10.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Subtle pastel decorative aura in top right corner (matching HubGridCard style)
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 18.dp, y = (-18).dp)
                    .clip(CircleShape)
                    .background(iconContainerColor.copy(alpha = if (isDark) 0.15f else 0.35f))
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Soft squircle icon box (exactly matching HubGridCard)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconContainerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Cover Page Generator",
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title, Subtitle, and Badges
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Cover Page Generator",
                            style = MaterialTheme.typography.titleSmall.copy(fontSize = 14.5.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = titleColor,
                            maxLines = 1
                        )

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isDark) Color(0xFF1E3A8A) else Color(0xFFDBEAFE)
                        ) {
                            Text(
                                text = "6 Templates",
                                color = if (isDark) Color(0xFF93C5FD) else Color(0xFF1D4ED8),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = "Generate print-ready university cover pages with your logo",
                        fontSize = 11.5.sp,
                        color = subtitleColor,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Chevron
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open Cover Page Generator",
                        tint = chevronTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
