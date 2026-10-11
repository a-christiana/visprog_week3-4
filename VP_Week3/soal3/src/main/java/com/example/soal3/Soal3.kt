package com.example.soal3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soal3.ui.theme.VP_Week3Theme
import kotlinx.coroutines.delay
import kotlin.random.Random

// --- 1. ENUMS & DATA MODELS ---
enum class GameState {
    WELCOME,
    COUNTDOWN,
    RUNNING,
    GAMEOVER
}

enum class GameMode {
    COLOR,
    TEXT
}

enum class Choice {
    INK,
    WORD
}

enum class ColorData(val label: String, val color: Color) {
    RED("RED", Color(0xFFE53935)),
    BLUE("BLUE", Color(0xFF1E88E5)),
    GREEN("GREEN", Color(0xFF43A047)),
    ORANGE("ORANGE", Color(0xFFFB8C00))
}

class Soal3 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VP_Week3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ColorWordMatchingApp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// --- 2. UTAMA APLIKASI & STATE MANAGEMENT ---
@Composable
fun ColorWordMatchingApp(modifier: Modifier = Modifier) {
    var gameState by remember { mutableStateOf(GameState.WELCOME) }
    var score by remember { mutableStateOf(0) }
    var mistakes by remember { mutableStateOf(0) }
    var bestScore by remember { mutableStateOf(0) }

    // Countdown State
    var countdownText by remember { mutableStateOf("3") }

    // Question State
    var mode by remember { mutableStateOf(GameMode.COLOR) }
    var currentWord by remember { mutableStateOf(ColorData.RED) }
    var currentInk by remember { mutableStateOf(ColorData.BLUE) }

    // Button Shuffle State
    var leftButtonText by remember { mutableStateOf("") }
    var rightButtonText by remember { mutableStateOf("") }
    var isLeftInk by remember { mutableStateOf(true) }

    // Timer State (in milliseconds, max 5000ms = 5s)
    var timeLeftMs by remember { mutableStateOf(5000L) }
    val totalTimeMs = 5000L
    val tickIntervalMs = 100L

    // Fungsi untuk mengacak soal baru
    val generateQuestion = {
        mode = GameMode.entries.random()
        currentWord = ColorData.entries.random()
        currentInk = ColorData.entries.random()

        isLeftInk = Random.nextBoolean()
        leftButtonText = if (isLeftInk) currentInk.label else currentWord.label
        rightButtonText = if (isLeftInk) currentWord.label else currentInk.label

        timeLeftMs = totalTimeMs
    }

    // Coroutine untuk Countdown 3 -> 2 -> 1 -> Start!
    LaunchedEffect(gameState) {
        if (gameState == GameState.COUNTDOWN) {
            for (i in 3 downTo 1) {
                countdownText = i.toString()
                delay(1000L)
            }
            countdownText = "Start!"
            delay(700L)
            score = 0
            mistakes = 0
            generateQuestion()
            gameState = GameState.RUNNING
        }
    }

    // Coroutine untuk Timer per Soal (Running Mode)
    LaunchedEffect(gameState, currentWord, currentInk, timeLeftMs) {
        if (gameState == GameState.RUNNING && timeLeftMs > 0) {
            delay(tickIntervalMs)
            timeLeftMs -= tickIntervalMs
            if (timeLeftMs <= 0) {
                mistakes += 1
                if (mistakes >= 3) {
                    if (score > bestScore) bestScore = score
                    gameState = GameState.GAMEOVER
                } else {
                    generateQuestion()
                }
            }
        }
    }

    // Handler ketika tombol ditekan pemain
    val handleAnswerSelection = { chosenIsLeft: Boolean ->
        val selectedLabel = if (chosenIsLeft) leftButtonText else rightButtonText
        val correctTargetLabel = if (mode == GameMode.COLOR) currentInk.label else currentWord.label

        if (selectedLabel == correctTargetLabel) {
            score += 1
        } else {
            mistakes += 1
        }

        if (mistakes >= 3) {
            if (score > bestScore) bestScore = score
            gameState = GameState.GAMEOVER
        } else {
            generateQuestion()
        }
    }

    // --- 3. UI TAMPILAN BERDASARKAN GAME STATE ---
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)),
        contentAlignment = Alignment.Center
    ) {
        when (gameState) {
            GameState.WELCOME -> {
                WelcomeScreen(onStartClick = { gameState = GameState.COUNTDOWN })
            }
            GameState.COUNTDOWN -> {
                CountdownScreen(text = countdownText)
            }
            GameState.RUNNING -> {
                RunningScreen(
                    mode = mode,
                    score = score,
                    mistakes = mistakes,
                    timeLeftMs = timeLeftMs,
                    totalTimeMs = totalTimeMs,
                    wordText = currentWord.label,
                    inkColor = currentInk.color,
                    leftButtonText = leftButtonText,
                    rightButtonText = rightButtonText,
                    onLeftClick = { handleAnswerSelection(true) },
                    onRightClick = { handleAnswerSelection(false) }
                )
            }
            GameState.GAMEOVER -> {
                GameOverScreen(
                    score = score,
                    bestScore = bestScore,
                    onRestartClick = { gameState = GameState.COUNTDOWN },
                    onExitClick = { gameState = GameState.WELCOME }
                )
            }
        }
    }
}

// --- 4. KOMPONEN LAYAR (SCREENS) ---

@Composable
fun WelcomeScreen(onStartClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(text = "Welcome", fontSize = 28.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        Text(text = "to", fontSize = 20.sp, fontWeight = FontWeight.Medium, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
        Text(
            text = "Color Word Matching",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Button(
            onClick = onStartClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5))
        ) {
            Text(text = "Start Game", color = Color.DarkGray, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
        }
    }
}

@Composable
fun CountdownScreen(text: String) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            text = text,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray
        )
    }
}

@Composable
fun RunningScreen(
    mode: GameMode,
    score: Int,
    mistakes: Int,
    timeLeftMs: Long,
    totalTimeMs: Long,
    wordText: String,
    inkColor: Color,
    leftButtonText: String,
    rightButtonText: String,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit
) {
    val secondsRemaining = (timeLeftMs + 999) / 1000

    // Menggunakan Arrangement.spacedBy agar komponen berdekatan secara proporsional di tengah
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(96.dp, Alignment.CenterVertically)
    ) {
        // Header: Mode (Diperbesar) & Statistik Skor/Kesalahan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mode: ${mode.name}",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "☑ $score", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "❌ $mistakes/3", color = Color(0xFFC62828), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }

        // Bagian Tengah: Timer dan Kata Utama
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$secondsRemaining s",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Teks Utama dengan Warna Tinta (Stroop Effect)
            Text(
                text = wordText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = inkColor
            )
        }

        // Bagian Bawah: Dua Tombol Jawaban Teracak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = onLeftClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5)),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = leftButtonText, color = Color.DarkGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onRightClick,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5)),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(text = rightButtonText, color = Color.DarkGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GameOverScreen(
    score: Int,
    bestScore: Int,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp)
    ) {
        Text(
            text = "Game Over!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(text = "Your Score", fontSize = 16.sp, color = Color.Gray)
        Text(
            text = "$score",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(text = "Best Score", fontSize = 14.sp, color = Color.Gray)
        Text(
            text = "$bestScore",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.DarkGray,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onRestartClick,
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB0BEC5)),
            modifier = Modifier.padding(bottom = 12.dp).width(180.dp)
        ) {
            Text(text = "Restart Game", color = Color.DarkGray, fontSize = 15.sp)
        }

        OutlinedButton(
            onClick = onExitClick,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.width(180.dp)
        ) {
            Text(text = "Exit", color = Color.DarkGray, fontSize = 15.sp)
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    VP_Week3Theme {
        ColorWordMatchingApp()
    }
}