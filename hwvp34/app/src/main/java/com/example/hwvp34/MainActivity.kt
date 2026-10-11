
package com.example.hwvp34

import android.os.Bundle
import android.os.SystemClock
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hwvp34.ui.theme.Hwvp34Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Hwvp34Theme() {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    soal1(Modifier.padding(innerPadding))
                }
            }
        }
    }
}

enum class state {
    START, WAIT, GO, RESULT, FINAL, FAILED
}

enum class response(val title: String) {
    FAST("DANG YOU ARE SO FAST BRO!"),
    GOOD("YOUR REFLEX IS GOOD"),
    NORMAL("MEH LIKE OTHER PERSON"),
    SLOW("YOU LIKE A SNAIL BRO")
}

fun cat(avgTime: Long): response {
    return when {
        avgTime < 180 -> response.FAST
        avgTime < 280 -> response.GOOD
        avgTime < 450 -> response.NORMAL
        else -> response.SLOW
    }
}

@Composable
fun soal1(modifier: Modifier = Modifier) {

    var sState by remember { mutableStateOf(state.START) }
    var displayTxt by remember { mutableStateOf("Reaction") }
    var txt by remember { mutableStateOf("Test") }
    var subTxt by remember { mutableStateOf("Click to Start") }

    var reactionTime by remember { mutableLongStateOf(0L) }
    var startTime by remember { mutableLongStateOf(0L) }
    var trial by remember { mutableIntStateOf(0) }
    var successfulTrials by remember { mutableIntStateOf(0) }
    var totalTime by remember { mutableLongStateOf(0L) }

    var trialTimes by remember {
        mutableStateOf(listOf<Pair<Int, Long>>())
    }

    val avgTime = if (successfulTrials > 0) {
        totalTime / successfulTrials
    } else {
        0L
    }

    val finalCategory = cat(avgTime)

    val finalColor = when (finalCategory) {
        response.FAST -> Color.Green
        response.GOOD -> Color.Cyan
        response.NORMAL -> Color.Yellow
        response.SLOW -> Color.Red
    }

    val finalImage = when (finalCategory) {
        response.FAST -> R.drawable.dang
        response.GOOD -> R.drawable.good
        response.NORMAL -> R.drawable.meh
        response.SLOW -> R.drawable.snail
    }

    val scope = rememberCoroutineScope()

    fun starts() {
        sState = state.WAIT
        displayTxt = "Get Ready"
        txt = "Wait for green light..."
        subTxt = "DON'T CLICK YET!"

        scope.launch {
            delay(Random.nextLong(500L, 4500L))

            if (sState == state.WAIT) {
                startTime = SystemClock.elapsedRealtime()
                sState = state.GO
                displayTxt = "GO!"
                txt = "CLICK NOW!"
                subTxt = "TAP AS FAST AS YOU CAN!"
            }
        }
    }

    fun showFinalResult() {
        sState = state.FINAL
        displayTxt = "Final Result"
        txt = "Average: ${avgTime}ms"
        subTxt = finalCategory.title
    }

    fun handleClick() {
        when (sState) {
            state.START -> {
                starts()
            }

            state.WAIT -> {
                trial++
                sState = state.FAILED
                displayTxt = "FAIL!"
                txt = "You clicked too early!"
                subTxt = "TRY AGAIN"
            }

            state.GO -> {
                reactionTime =
                    SystemClock.elapsedRealtime() - startTime

                totalTime += reactionTime
                trialTimes = trialTimes + (trial + 1 to reactionTime)
                trial++
                successfulTrials++

                sState = state.RESULT
                displayTxt = "Trial $trial Complete!"
                txt = "Time: ${reactionTime}ms"

                subTxt = if (trial < 3) {
                    "Continue to Trial ${trial + 1}"
                } else {
                    "See Final Results"
                }
            }

            state.RESULT, state.FAILED -> {
                if (trial < 3) {
                    starts()
                } else {
                    showFinalResult()
                }
            }

            state.FINAL -> {
                sState = state.START
                displayTxt = "Reaction"
                txt = "Test"
                subTxt = "Click to Start"

                reactionTime = 0L
                startTime = 0L
                trial = 0
                successfulTrials = 0
                totalTime = 0L
                trialTimes = emptyList()
            }
        }
    }

    val backgroundColor = when (sState) {
        state.START -> Color.Cyan
        state.WAIT -> Color.LightGray
        state.GO -> Color.Green
        state.RESULT -> Color.Green
        state.FINAL -> finalColor
        state.FAILED -> Color.Red
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable { handleClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {

        when (sState) {
            state.FINAL -> {
                Text(
                    text = finalCategory.title,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Image(
                    painter = painterResource(id = finalImage),
                    contentDescription = finalCategory.title,
                    modifier = Modifier
                        .padding(top = 20.dp)
                        .size(210.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    text = "Average: ${avgTime}ms",
                    color = Color.White,
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 22.dp)
                )

                Text(
                    text = "Click to Start New Test",
                    color = Color.White,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(
                        top = 18.dp,
                        bottom = 18.dp
                    )
                )
            }

            state.FAILED -> {
                Text(
                    text = "FAIL!",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.ThumbDown,
                    contentDescription = "Failed",
                    modifier = Modifier
                        .padding(top = 20.dp, bottom = 20.dp)
                        .size(120.dp),
                    tint = Color.White
                )

                Text(
                    text = "You clicked too early, TRY TO READ THE RULE BRO",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "TRY AGAIN",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 18.dp)
                )
            }

            else -> {
                Text(
                    text = displayTxt,
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 40.dp)
                )

                Icon(
                    imageVector = when (sState) {
                        state.START -> Icons.Default.FlashOn
                        state.WAIT -> Icons.Default.Warning
                        state.GO -> Icons.Default.DirectionsRun
                        state.RESULT -> Icons.Default.CheckCircle
                        state.FINAL -> Icons.Default.CheckCircle
                        state.FAILED -> Icons.Default.Warning
                    },
                    contentDescription = "Reaction Icon",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(bottom = 40.dp),
                    tint = Color.White
                )

                Text(
                    text = txt,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                Text(
                    text = subTxt,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }

        // Card hanya muncul di Final Result.
        if (sState == state.FINAL ||
            sState == state.FAILED ||
            sState == state.RESULT) {
            Card(
                modifier = Modifier.width(230.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 7.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Trial Results",
                        color = Color(0xFF1976D2),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (i in 1..3) {
                            Text(
                                text = "$i",
                                color = Color(0xFF4CAF50),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (i in 1..3) {
                            val time = trialTimes.find {
                                it.first == i
                            }?.second

                            Text(
                                text = time?.let { "${it}ms" } ?: "-",
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }


                    if (sState == state.FINAL) {
                        Text(
                            text = "Average Score",
                            color = Color(0xFF1976D2),
                            fontSize = 15.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )

                        Text(
                            text = if (successfulTrials > 0) {
                                "${avgTime}ms"
                            } else {
                                "-"
                            },
                            color = finalColor,
                            fontSize = 19.sp,
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
fun soal1Preview() {
    Hwvp34Theme() {
        soal1()
    }
}
