package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.DarkBg
import com.example.ui.theme.FintechCyan
import com.example.ui.theme.FintechGold
import com.example.ui.theme.FintechGreen
import com.example.ui.theme.FintechPurple
import com.example.ui.theme.FintechRed
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardBorder
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.AiChatMessage
import com.example.ui.viewmodel.ExchangeViewModel
import com.example.ui.viewmodel.MessageSender
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AiPersona(val titleFa: String, val emoji: String, val promptPrefix: String) {
    ALL("همه بازارها", "🌐", ""),
    CRYPTO("تخصصی کریپتو", "💎", "به عنوان تحلیل‌گر فوق‌تخصصی بازار ارزهای دیجیتال و بیت‌کوین، "),
    GOLD_FX("طلا و دلار آزاد", "🪙", "به عنوان معامله‌گر ارشد بازار طلا، سکه امامی و حواله درهم، "),
    PORTFOLIO("پورتفوی ضد تورم", "🛡️", "به عنوان مشاور ارشد مدیریت ثروت و سرمایه‌گذاری ضد تورمی، ")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAnalystScreen(
    viewModel: ExchangeViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val messages by viewModel.aiChatMessages.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAiAnalyzing.collectAsStateWithLifecycle()
    var inputText by remember { mutableStateOf("") }
    var selectedPersona by remember { mutableStateOf(AiPersona.ALL) }
    val listState = rememberLazyListState()

    // Text to Speech
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var speakingMessageId by remember { mutableStateOf<String?>(null) }

    DisposableEffect(context) {
        val ttsInstance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("fa")
            }
        }
        tts = ttsInstance
        onDispose {
            ttsInstance.stop()
            ttsInstance.shutdown()
        }
    }

    // Voice recognition launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenList?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val finalPrompt = if (selectedPersona.promptPrefix.isNotBlank()) {
                    selectedPersona.promptPrefix + spokenText
                } else {
                    spokenText
                }
                viewModel.askAi(finalPrompt)
            }
        }
    }

    val quickQuestions = when (selectedPersona) {
        AiPersona.CRYPTO -> listOf(
            "🚀 تحلیل تکنیکال بیت‌کوین و اهداف قیمتی آن",
            "💎 بهترین آلت‌کوین‌ها برای خرید پله‌ای امروز",
            "📊 دامیننس تتر و بیت‌کوین چه سیگنالی می‌دهند؟",
            "⚖️ استراتژی معاملاتی اتریوم و سولانا"
        )
        AiPersona.GOLD_FX -> listOf(
            "🪙 طلای آبشده ۱۸ عیار بخرم یا سکه امامی؟",
            "💵 پیش‌بینی نوسان دلار آزاد و نرخ حواله درهم",
            "🎯 سطوح حمایت و مقاومت کلیدی طلای ۱۸ عیار",
            "⚠️ وضعیت حباب ربع سکه و نیم سکه بهار آزادی"
        )
        AiPersona.PORTFOLIO -> listOf(
            "🛡️ فرمول بهینه پورتفوی ضدتورم با سرمایه فعلی",
            "💡 چقدر طلا، چقدر تتر و چقدر ریال نگه دارم؟",
            "📈 محاسبه نسبت ریسک به ریوارد و حد ضرر",
            "🔮 سناریوهای ۳گانه اقتصاد تا پایان ماه"
        )
        AiPersona.ALL -> listOf(
            "🎙️ با هوش مصنوعی درباره بازار امروز صحبت کن",
            "🎯 سیگنال دقیق، حد ضرر و تارگت سود طلا و بیت‌کوین",
            "🔮 تحلیل سناریوهای ۳ گانه دلار و طلا تا پایان ماه",
            "📈 تحلیل جامع وضعیت امروز دلار و طلا",
            "🪙 استراتژی خرید پله‌ای (DCA) بیت‌کوین و تتر",
            "🛡️ بهترین چیدمان سبد دارایی ضد تورم"
        )
    }

    // Auto-scroll when new messages arrive
    LaunchedEffect(messages.size, isAnalyzing) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(FintechCyan.copy(alpha = 0.15f))
                                .border(1.dp, FintechCyan.copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FintechCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "گفتگو با هوش مصنوعی مالی",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(FintechGreen.copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "صوتی و متنی 🎙️",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FintechGreen
                                    )
                                }
                            }
                            Text(
                                text = "دستیار سخنگو با مدل Gemini 3.5 Flash",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = FintechGold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            tts?.stop()
                            speakingMessageId = null
                            viewModel.clearAiChat()
                        },
                        modifier = Modifier.testTag("clear_ai_chat_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "پاک‌سازی تاریخچه",
                            tint = TextMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        bottomBar = {
            // Input Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceCardBorder)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Quick Question Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickQuestions) { q ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(SurfaceCardLight)
                                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(12.dp))
                                .clickable(enabled = !isAnalyzing) {
                                    val finalPrompt = if (selectedPersona.promptPrefix.isNotBlank()) {
                                        selectedPersona.promptPrefix + q
                                    } else {
                                        q
                                    }
                                    viewModel.askAi(finalPrompt)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = q,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = TextSecondary
                            )
                        }
                    }
                }

                // Text Input Field, Voice Mic Button & Send Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Microphone Voice Input Button
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(FintechPurple.copy(alpha = 0.2f))
                            .border(1.dp, FintechPurple.copy(alpha = 0.5f), CircleShape)
                            .clickable(enabled = !isAnalyzing) {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa")
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "با تحلیل‌گر هوش مصنوعی مالی EXCHANCE صحبت کنید...")
                                    }
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "سرویس تشخیص صدای گوگل در دسترس نیست", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("voice_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "صحبت صوتی با هوش مصنوعی",
                            tint = FintechPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_input_field"),
                        placeholder = {
                            Text(
                                text = "بنویسید یا با میکروفون صحبت کنید...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                color = TextMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FintechCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = FintechCyan
                        ),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        enabled = !isAnalyzing
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isAnalyzing) FintechCyan else SurfaceCardLight)
                            .clickable(enabled = inputText.isNotBlank() && !isAnalyzing) {
                                val textToSend = inputText
                                inputText = ""
                                val finalPrompt = if (selectedPersona.promptPrefix.isNotBlank()) {
                                    selectedPersona.promptPrefix + textToSend
                                } else {
                                    textToSend
                                }
                                viewModel.askAi(finalPrompt)
                            }
                            .testTag("send_ai_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = FintechCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "ارسال",
                                tint = if (inputText.isNotBlank()) Color.Black else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Persona Selector Row
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AiPersona.values()) { persona ->
                        val isSelected = persona == selectedPersona
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) FintechCyan.copy(alpha = 0.2f) else SurfaceCardLight)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) FintechCyan else SurfaceCardBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { selectedPersona = persona }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = persona.emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = persona.titleFa,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) FintechCyan else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            items(messages, key = { it.id }) { message ->
                ChatMessageItem(
                    message = message,
                    isSpeaking = speakingMessageId == message.id,
                    onToggleSpeak = {
                        if (speakingMessageId == message.id) {
                            tts?.stop()
                            speakingMessageId = null
                        } else {
                            tts?.stop()
                            speakingMessageId = message.id
                            // Clean markdown stars for smoother speech
                            val cleanText = message.text.replace("*", "").replace("#", "")
                            tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, message.id)
                        }
                    }
                )
            }

            if (isAnalyzing) {
                item {
                    AnalyzingIndicator()
                }
            }
        }
    }
}

@Composable
private fun ChatMessageItem(
    message: AiChatMessage,
    isSpeaking: Boolean,
    onToggleSpeak: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER
    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(FintechCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = FintechCyan,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) FintechCyan.copy(alpha = 0.2f) else SurfaceCardLight)
                .border(
                    width = 1.dp,
                    color = if (isUser) FintechCyan.copy(alpha = 0.5f) else SurfaceCardBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 22.sp,
                        fontSize = 13.sp
                    ),
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!isUser) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Text to speech button
                            IconButton(
                                onClick = onToggleSpeak,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                    contentDescription = if (isSpeaking) "توقف خواندن" else "خواندن صوتی پاسخ",
                                    tint = if (isSpeaking) FintechGold else FintechCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            if (isSpeaking) {
                                Text(
                                    text = "در حال صحبت...",
                                    fontSize = 10.sp,
                                    color = FintechGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val formattedTime = remember(message.timestamp) {
                            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
                        }
                        Text(
                            text = formattedTime,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("تحلیل EXCHANCE", message.text)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "متن تحلیل کپی شد", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "کپی",
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyzingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(FintechCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = FintechCyan,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCardLight)
                .border(1.dp, SurfaceCardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = FintechCyan,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "هوش مصنوعی در حال تحلیل زنده بازار و پاسخ است...",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
            }
        }
    }
}
