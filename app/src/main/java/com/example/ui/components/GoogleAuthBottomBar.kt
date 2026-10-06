package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.service.FirebaseAuthManager
import com.example.service.FirestoreSyncManager
import com.example.service.SyncState
import com.example.service.UserProfile
import com.example.ui.theme.AppTheme
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleAuthBottomBar(
    currentUser: UserProfile?,
    isLoading: Boolean,
    syncState: SyncState,
    onSignInGoogle: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var showDetailsSheet by remember { mutableStateOf(false) }

    // Compact Google Auth Bar Docked at Bottom
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceLight)
            .border(
                1.dp,
                if (currentUser != null) FintechCyan.copy(alpha = 0.5f) else colors.surfaceBorder,
                RoundedCornerShape(18.dp)
            )
            .clickable { showDetailsSheet = true }
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("google_auth_bottom_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Action Button
            if (currentUser == null) {
                Button(
                    onClick = onSignInGoogle,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.surfaceBorder),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("google_signin_button")
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = FintechCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ورود با گوگل",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Google Logo Icon (Colorful)
                            GoogleIconSmall()
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(FintechCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = FintechCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Firestore متصل",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = FintechCyan
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "خروج",
                            tint = FintechRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // User Info or Google Prompt
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    if (currentUser == null) {
                        Text(
                            text = "حساب گوگل و همگام‌سازی ابری",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary
                        )
                        Text(
                            text = "ذخیره دائمی دیده‌بان و هشدارها در Firestore",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                            color = colors.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = currentUser.displayName ?: "کاربر گرامی",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = currentUser.email ?: "حساب متصل گوگل",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Avatar / Icon
                if (currentUser?.photoUrl != null) {
                    AsyncImage(
                        model = currentUser.photoUrl,
                        contentDescription = "پروفایل گوگل",
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, FintechCyan, CircleShape)
                    )
                } else if (currentUser != null) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(FintechCyan.copy(alpha = 0.2f))
                            .border(1.dp, FintechCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (currentUser.displayName?.take(1) ?: "G").uppercase(),
                            color = FintechCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(colors.surface)
                            .border(1.dp, colors.surfaceBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        GoogleIconSmall()
                    }
                }
            }
        }
    }

    // Detailed Google Login & Firestore Sheet
    if (showDetailsSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showDetailsSheet = false },
            sheetState = sheetState,
            containerColor = colors.surface,
            modifier = Modifier.testTag("google_auth_details_sheet")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Google Logo Header
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceLight)
                        .border(1.5.dp, colors.surfaceBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    GoogleIconLarge()
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (currentUser == null) "ورود به حساب کاربری گوگل" else "مدیریت حساب و همگام‌سازی ابری",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (currentUser == null) {
                        "با اتصال حساب کاربری گوگل، لیست دیده‌بان و هشدارهای قیمت تعیین‌شده شما به صورت آنی در پایگاه داده ابری Firebase Firestore ذخیره و در تمام دستگاه‌ها همگام می‌گردد."
                    } else {
                        "حساب گوگل شما متصل است و تغییرات دیده‌بان و هشدارهای قیمتی به طور مستقیم با فضای ابری Firebase Firestore همگام‌سازی می‌شوند."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Cloud Status Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.surfaceLight)
                        .border(1.dp, colors.surfaceBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = syncState.icon + " " + syncState.labelFa,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (currentUser != null) FintechGreen else colors.textMuted
                            )
                            Text(
                                text = "وضعیت Firestore:",
                                fontSize = 11.sp,
                                color = colors.textMuted
                            )
                        }

                        if (currentUser != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = currentUser.email ?: "---",
                                    fontSize = 11.sp,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "شناسه کاربری:",
                                    fontSize = 11.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button
                if (currentUser == null) {
                    Button(
                        onClick = {
                            onSignInGoogle()
                            showDetailsSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("modal_google_signin_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            GoogleIconSmall()
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ادامه با حساب گوگل (Google Sign-In)",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            onSignOut()
                            showDetailsSheet = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("modal_signout_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = FintechRed.copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FintechRed.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = FintechRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "خروج از حساب گوگل",
                                color = FintechRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun GoogleIconSmall() {
    Text(
        text = "G",
        fontWeight = FontWeight.Black,
        fontSize = 16.sp,
        color = Color(0xFF4285F4)
    )
}

@Composable
fun GoogleIconLarge() {
    Text(
        text = "G",
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        color = Color(0xFF4285F4)
    )
}
