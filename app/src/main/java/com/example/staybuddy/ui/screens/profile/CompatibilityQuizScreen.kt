package com.example.staybuddy.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.staybuddy.ui.theme.RoyalBlue
import com.example.staybuddy.ui.theme.SoftOffWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityQuizScreen(
    onBack: () -> Unit,
    onComplete: () -> Unit,
    viewModel: CompatibilityQuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onComplete()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compatibility Quiz", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 0) viewModel.previousStep()
                        else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Brush.verticalGradient(listOf(Color.White, SoftOffWhite)))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress Section
            LinearProgressIndicator(
                progress = { (uiState.currentStep + 1).toFloat() / uiState.totalSteps },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = RoyalBlue,
                trackColor = RoyalBlue.copy(alpha = 0.1f)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Step ${uiState.currentStep + 1} of ${uiState.totalSteps}",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Animated Question Container
            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "QuizStepTransition"
            ) { step ->
                QuizStep(
                    step = step,
                    onAnswer = { viewModel.onAnswerSelected(it) }
                )
            }

            if (uiState.isSubmitting) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(color = RoyalBlue)
            }
        }
    }
}

@Composable
fun QuizStep(step: Int, onAnswer: (String) -> Unit) {
    val questionData = remember(step) {
        when (step) {
            0 -> QuizQuestion(
                "What is your sleep schedule?",
                listOf(
                    QuizOption("Early Bird", "early_bird", "Wake up early, sleep early"),
                    QuizOption("Night Owl", "night_owl", "Productive at night"),
                    QuizOption("Flexible", "flexible", "Changes as per need")
                )
            )
            1 -> QuizQuestion(
                "How do you feel about cleanliness?",
                listOf(
                    QuizOption("Obsessive", "obsessive", "Everything matches perfectly"),
                    QuizOption("Average", "average", "Keep it tidy enough"),
                    QuizOption("Relaxed", "relaxed", "Clean up later")
                )
            )
            2 -> QuizQuestion(
                "Food Preferences?",
                listOf(
                    QuizOption("Vegetarian", "veg", "Pure veg preferred"),
                    QuizOption("Non-Vegetarian", "non_veg", "Can cook/eat meat"),
                    QuizOption("Flexible", "flexible", "Anything works")
                )
            )
            3 -> QuizQuestion(
                "Smoking/Drinking Habits?",
                listOf(
                    QuizOption("Strict No", "no", "No substances in house"),
                    QuizOption("Occasional/Outside", "outside", "Only outside premises"),
                    QuizOption("Chill", "yes", "Comfortable with it")
                )
            )
            4 -> QuizQuestion(
                "Guests/Visitors Preference?",
                listOf(
                    QuizOption("No Guests", "no_guests", "Maintain private zone"),
                    QuizOption("Daytime Only", "day_only", "Visitors leave by night"),
                    QuizOption("Anytime Welcome", "anytime", "Friends can visit anytime")
                )
            )
            else -> QuizQuestion("", emptyList())
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = questionData.question,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(32.dp))

        questionData.options.forEach { option ->
            OutlinedButton(
                onClick = { onAnswer(option.value) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = RoyalBlue
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(text = option.label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = option.description, fontSize = 14.sp, color = Color.Gray)
                }
            }
        }
    }
}

data class QuizQuestion(val question: String, val options: List<QuizOption>)
data class QuizOption(val label: String, val value: String, val description: String)
