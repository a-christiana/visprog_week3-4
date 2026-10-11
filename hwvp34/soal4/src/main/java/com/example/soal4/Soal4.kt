package com.example.hwvp34

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// ==========================================
// 1. ENUM CLASSES
// ==========================================

enum class GameState {
    INITIAL,
    PICK,
    REVEAL,
    FINISHED
}

enum class Rps(val emoji: String, val label: String) {
    ROCK("✊", "Rock"),
    PAPER("✋", "Paper"),
    SCISSOR("✌", "Scissor")
}

enum class GameResult {
    WIN,
    LOSE,
    DRAW
}

// ==========================================
// 2. MAIN ACTIVITY
// ==========================================

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    RpsGameScreen()
                }
            }
        }
    }
}

// ==========================================
// 3. MAIN COMPOSABLE & GAME LOGIC
// ==========================================

@Composable
fun RpsGameScreen() {
    var gameState by rememberSaveable { mutableStateOf(GameState.INITIAL) }
    var nValue by rememberSaveable { mutableStateOf(5) } // Best of 5 (ganjil)
    var yourScore by rememberSaveable { mutableStateOf(0) }
    var cpuScore by rememberSaveable { mutableStateOf(0) }
    var bestScore by rememberSaveable { mutableStateOf(0) }

    var playerMove by rememberSaveable { mutableStateOf<Rps?>(null) }
    var cpuMove by rememberSaveable { mutableStateOf<Rps?>(null) }
    var roundResult by rememberSaveable { mutableStateOf<GameResult?>(null) }

    val targetPoints = (nValue / 2) + 1

    var shuffledButtons by remember { mutableStateOf(Rps.values().toList().shuffled()) }

    // Auto-advance coroutine handler for Reveal state (~700ms)
    LaunchedEffect(gameState, playerMove) {
        if (gameState == GameState.REVEAL) {
            delay(700)
            if (yourScore >= targetPoints || cpuScore >= targetPoints) {
                gameState = GameState.FINISHED
                val currentDiff = yourScore - cpuScore
                if (currentDiff > bestScore) {
                    bestScore = currentDiff
                }
            } else {
                shuffledButtons = Rps.values().toList().shuffled()
                playerMove = null
                cpuMove = null
                roundResult = null
                gameState = GameState.PICK
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        when (gameState) {
            GameState.INITIAL -> {
                InitialScreen(
                    nValue = nValue,
                    onNChanged = { nValue = it },
                    onStart = {
                        yourScore = 0
                        cpuScore = 0
                        shuffledButtons = Rps.values().toList().shuffled()
                        playerMove = null
                        cpuMove = null
                        roundResult = null
                        gameState = GameState.PICK
                    }
                )
            }
            GameState.PICK, GameState.REVEAL -> {
                RunningScreen(
                    gameState = gameState,
                    nValue = nValue,
                    yourScore = yourScore,
                    cpuScore = cpuScore,
                    playerMove = playerMove,
                    cpuMove = cpuMove,
                    roundResult = roundResult,
                    shuffledButtons = shuffledButtons,
                    onPlayerPick = { chosen ->
                        if (gameState == GameState.PICK) {
                            playerMove = chosen
                            val generatedCpuMove = Rps.values().random()
                            cpuMove = generatedCpuMove

                            roundResult = when {
                                chosen == generatedCpuMove -> GameResult.DRAW
                                (chosen == Rps.ROCK && generatedCpuMove == Rps.SCISSOR) ||
                                        (chosen == Rps.PAPER && generatedCpuMove == Rps.ROCK) ||
                                        (chosen == Rps.SCISSOR && generatedCpuMove == Rps.PAPER) -> {
                                    yourScore++
                                    GameResult.WIN
                                }
                                else -> {
                                    cpuScore++
                                    GameResult.LOSE
                                }
                            }
                            gameState = GameState.REVEAL
                        }
                    }
                )
            }
            GameState.FINISHED -> {
                GameOverScreen(
                    yourScore = yourScore,
                    cpuScore = cpuScore,
                    bestScore = bestScore,
                    onRestart = {
                        yourScore = 0
                        cpuScore = 0
                        shuffledButtons = Rps.values().toList().shuffled()
                        playerMove = null
                        cpuMove = null
                        roundResult = null
                        gameState = GameState.PICK
                    },
                    onExit = {
                        gameState = GameState.INITIAL
                    }
                )
            }
        }
    }
}

// ==========================================
// 4. SUB-SCREENS / COMPONENT UI
// ==========================================

@Composable
fun InitialScreen(
    nValue: Int,
    onNChanged: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Rock • Paper • Scissors",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { contentDescription = "Game Title" }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Best of $nValue",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.semantics { contentDescription = "Mode Best of $nValue" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(3, 5, 7).forEach { value ->
                OutlinedButton(
                    onClick = { onNChanged(value) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (nValue == value) Color.LightGray.copy(alpha = 0.4f) else Color.Transparent
                    )
                ) {
                    Text(text = "Best of $value")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onStart,
            modifier = Modifier.width(160.dp).height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(text = "Start", fontSize = 16.sp)
        }
    }
}

@Composable
fun RunningScreen(
    gameState: GameState,
    nValue: Int,
    yourScore: Int,
    cpuScore: Int,
    playerMove: Rps?,
    cpuMove: Rps?,
    roundResult: GameResult?,
    shuffledButtons: List<Rps>,
    onPlayerPick: (Rps) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🧑 $yourScore — 🤖 $cpuScore",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.semantics { contentDescription = "Score header: your $yourScore - cpu $cpuScore" }
            )
            Text(
                text = "Best of $nValue",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(80.dp))

        if (gameState == GameState.PICK) {
            Text(
                text = "Pick your move!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.semantics { contentDescription = "Pick your move prompt" }
            )
            Spacer(modifier = Modifier.height(40.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "❓ VS ❓",
                    fontSize = 32.sp,
                    modifier = Modifier.semantics { contentDescription = "Empty VS placeholders" }
                )
            }
        } else {
            val playerEmoji = playerMove?.emoji ?: "❓"
            val cpuEmoji = cpuMove?.emoji ?: "❓"

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = playerEmoji,
                    fontSize = 48.sp,
                    modifier = Modifier.semantics { contentDescription = "Player chose ${playerMove?.label}" }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "VS",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = cpuEmoji,
                    fontSize = 48.sp,
                    modifier = Modifier.semantics { contentDescription = "CPU chose ${cpuMove?.label}" }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val resultText = when (roundResult) {
                GameResult.WIN -> "You Win!"
                GameResult.LOSE -> "You Lose"
                GameResult.DRAW -> "Draw"
                null -> ""
            }

            Text(
                text = resultText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = when (roundResult) {
                    GameResult.WIN -> Color(0xFF2E7D32)
                    GameResult.LOSE -> Color(0xFFC62828)
                    else -> Color.DarkGray
                },
                modifier = Modifier.semantics { contentDescription = "Round result: $resultText" }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            shuffledButtons.forEach { rps ->
                Button(
                    onClick = { onPlayerPick(rps) },
                    enabled = gameState == GameState.PICK,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.5f),
                        contentColor = Color.DarkGray
                    ),
                    modifier = Modifier.semantics { contentDescription = rps.label }
                ) {
                    Text(
                        text = "${rps.emoji} ${rps.label}",
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun GameOverScreen(
    yourScore: Int,
    cpuScore: Int,
    bestScore: Int,
    onRestart: () -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isMatchWin = yourScore > cpuScore
        Text(
            text = if (isMatchWin) "You Win the Match!" else "You Lose the Match",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.semantics { contentDescription = if (isMatchWin) "You Win the Match!" else "You Lose the Match" }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "🧑 $yourScore — 🤖 $cpuScore",
            fontSize = 18.sp,
            modifier = Modifier.semantics { contentDescription = "Final score: your $yourScore - cpu $cpuScore" }
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Best Score: $bestScore",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.semantics { contentDescription = "Best Score: $bestScore" }
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onRestart,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = "Restart")
            }

            OutlinedButton(
                onClick = onExit,
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(text = "Exit")
            }
        }
    }
}