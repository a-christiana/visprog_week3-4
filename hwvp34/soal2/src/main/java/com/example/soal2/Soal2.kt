package com.example.soal2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.soal2.ui.theme.Hwvp34Theme
import kotlin.math.roundToInt

class Soal2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Hwvp34Theme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CatClickerGame(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun CatClickerGame(modifier: Modifier = Modifier) {
    var coins by remember { mutableStateOf(0) }
    var coinsPerTap by remember { mutableStateOf(1.0) }
    var upgradeCost by remember { mutableStateOf(10) }
    var isCatMouthOpen by remember { mutableStateOf(false) }
    val nextCoinsPerTap = coinsPerTap * 1.5
    val canAffordUpgrade = coins >= upgradeCost
    val coinsNeeded = if (!canAffordUpgrade) upgradeCost - coins else 0

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Card(
                modifier = Modifier.padding(top = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.LightGray.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Coins",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "$coins",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00FF00)
                    )
                    Text(
                        text = "${coinsPerTap.roundToInt()} coins per tap",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tap the Cat!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isCatMouthOpen = true
                                    coins += coinsPerTap.roundToInt()

                                    tryAwaitRelease()

                                    isCatMouthOpen = false
                                }
                            )
                        }
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isCatMouthOpen) R.drawable.open else R.drawable.close
                        ),
                        contentDescription = "Tap Cat",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Purr~",
                    fontSize = 16.sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Give Me Your Coin",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Next upgrade: +${(nextCoinsPerTap - coinsPerTap).roundToInt()} coins per tap",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Button(
                        onClick = {
                            if (canAffordUpgrade) {
                                coins -= upgradeCost
                                coinsPerTap = nextCoinsPerTap
                                upgradeCost *= 2
                            }
                        },
                        enabled = canAffordUpgrade,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black,
                            disabledContainerColor = Color.LightGray.copy(alpha = 0.5f),
                            disabledContentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (canAffordUpgrade) "Pay for $upgradeCost coins" else "Find $coinsNeeded more coins",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun GreetingPreview() {
    Hwvp34Theme() {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            CatClickerGame(modifier = Modifier.padding(innerPadding))
        }
    }
}