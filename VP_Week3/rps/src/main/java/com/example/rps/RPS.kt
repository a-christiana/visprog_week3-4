package com.example.rps

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rps.ui.theme.VP_Week3Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Enum classes sesuai spesifikasi game
enum class Rps(val emoji: String, val label: String) {
    ROCK("✊", "Rock"),
    PAPER("✋", "Paper"),
    SCISSORS("✌", "Scissor")
}

enum class GameState {
    INITIAL, PICK, REVEAL, FINISHED
}

enum class RoundResult {
    WIN, LOSE, DRAW
}

class RPS : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VP_Week3Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Soal4(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Soal4(modifier: Modifier = Modifier) {
    val bestOfN = 5
    val targetPoints = (bestOfN / 2) + 1

    var gameState by rememberSaveable { mutableStateOf(GameState.INITIAL) }
    var yourScore by rememberSaveable { mutableStateOf(0) }
    var cpuScore by rememberSaveable { mutableStateOf(0) }
    var bestScore by rememberSaveable { mutableStateOf(0) }

    var playerChoice by remember { mutableStateOf<Rps?>(null) }
    var cpuChoice by remember { mutableStateOf<Rps?>(null) }
    var roundResult by remember { mutableStateOf<RoundResult?>(null) }

    val randomizedButtons = remember { mutableStateOf(Rps.values().asList().shuffled()) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Skor & Mode
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧑 $yourScore — $cpuScore 🤖",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.semantics { contentDescription = "Score header: Your score $yourScore, CPU score $cpuScore" }
            )
            Text(
                text = "Best of $bestOfN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.semantics { contentDescription = "Game mode Best of $bestOfN" }
            )
        }

        // Konten Tengah Sesuai State
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (gameState) {
                GameState.INITIAL -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Rock • Paper • Scissors",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        Button(
                            onClick = {
                                yourScore = 0
                                cpuScore = 0
                                randomizedButtons.value = Rps.values().asList().shuffled()
                                gameState = GameState.PICK
                            },
                            modifier = Modifier
                                .width(160.dp)
                                .height(48.dp)
                                .semantics { contentDescription = "Start Button" }
                        ) {
                            Text(text = "Start", fontSize = 16.sp)
                        }
                    }
                }

                GameState.PICK, GameState.REVEAL -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (gameState == GameState.PICK) {
                            Text(
                                text = "Pick your move!",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .semantics { contentDescription = "Pick your move prompt" }
                            )
                        } else {
                            val resultText = when (roundResult) {
                                RoundResult.WIN -> "You Win!"
                                RoundResult.LOSE -> "You Lose"
                                RoundResult.DRAW -> "Draw"
                                null -> ""
                            }
                            Text(
                                text = resultText,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .semantics { contentDescription = resultText }
                            )
                        }

                        // Area VS
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 48.dp)
                                .semantics {
                                    val desc = if (gameState == GameState.PICK) {
                                        "Question mark VS Question mark"
                                    } else {
                                        "Player chose ${playerChoice?.label}, CPU chose ${cpuChoice?.label}"
                                    }
                                    contentDescription = desc
                                }
                        ) {
                            Text(
                                text = if (gameState == GameState.PICK) "?" else (playerChoice?.emoji ?: "?"),
                                fontSize = 48.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "VS",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = if (gameState == GameState.PICK) "?" else (cpuChoice?.emoji ?: "?"),
                                fontSize = 48.sp
                            )
                        }

                        // Tombol Pilihan (Acak)
                        if (gameState == GameState.PICK) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                randomizedButtons.value.forEach { rps ->
                                    Button(
                                        onClick = {
                                            playerChoice = rps
                                            cpuChoice = Rps.values().random()

                                            roundResult = when {
                                                playerChoice == cpuChoice -> RoundResult.DRAW
                                                (playerChoice == Rps.ROCK && cpuChoice == Rps.SCISSORS) ||
                                                        (playerChoice == Rps.PAPER && cpuChoice == Rps.ROCK) ||
                                                        (playerChoice == Rps.SCISSORS && cpuChoice == Rps.PAPER) -> {
                                                    yourScore += 1
                                                    RoundResult.WIN
                                                }
                                                else -> {
                                                    cpuScore += 1
                                                    RoundResult.LOSE
                                                }
                                            }

                                            gameState = GameState.REVEAL

                                            coroutineScope.launch {
                                                delay(700)
                                                if (yourScore >= targetPoints || cpuScore >= targetPoints) {
                                                    val currentDiff = yourScore - cpuScore
                                                    if (currentDiff > bestScore) {
                                                        bestScore = currentDiff
                                                    }
                                                    gameState = GameState.FINISHED
                                                } else {
                                                    randomizedButtons.value = Rps.values().asList().shuffled()
                                                    gameState = GameState.PICK
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 4.dp)
                                            .semantics { contentDescription = rps.label }
                                    ) {
                                        Text(text = "${rps.emoji} ${rps.label}", fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                GameState.FINISHED -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val isMatchWin = yourScore > cpuScore
                        Text(
                            text = if (isMatchWin) "You Win the Match!" else "You Lose the Match",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .semantics { contentDescription = if (isMatchWin) "You Win the Match!" else "You Lose the Match" }
                        )

                        Text(
                            text = "Final Score: $yourScore — $cpuScore",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Best Score (Diff): $bestScore",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(bottom = 32.dp)
                                .semantics { contentDescription = "Best Score $bestScore" }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    yourScore = 0
                                    cpuScore = 0
                                    randomizedButtons.value = Rps.values().asList().shuffled()
                                    gameState = GameState.PICK
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp)
                                    .semantics { contentDescription = "Restart Button" }
                            ) {
                                Text(text = "Restart")
                            }

                            Button(
                                onClick = {
                                    gameState = GameState.INITIAL
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 8.dp)
                                    .semantics { contentDescription = "Exit Button" }
                            ) {
                                Text(text = "Exit")
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun Soal4Preview() {
    VP_Week3Theme {
        Soal4()
    }
}