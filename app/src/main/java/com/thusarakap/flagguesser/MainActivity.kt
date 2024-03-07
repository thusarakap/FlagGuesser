@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.thusarakap.flagguesser

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thusarakap.flagguesser.ui.theme.FlagGuesserTheme
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagGuesserTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "menu") {
                    composable("menu") { MainMenu(navController) }
                    composable("screen1") { Screen1(navController) }
                    composable("screen2") { Screen2(navController) }
                    composable("screen3") { Screen3(navController) }
                    composable("screen4") { Screen4(navController) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavHostController) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        ),
        title = { Text("Menu") },
        navigationIcon = {
            IconButton(onClick = { navController.navigate("menu") }) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainMenu(navController: NavHostController) {
    val buttonWidth = 230.dp

    Scaffold(
        content = { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Menu")
                Spacer(modifier = Modifier.height(30.dp))
                Button(
                    onClick = { navController.navigate("screen1") },
                    modifier = Modifier
                        .width(buttonWidth)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Guess the Country")
                }
                Button(
                    onClick = { navController.navigate("screen2") },
                    modifier = Modifier
                        .width(buttonWidth)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Guess-Hints")
                }
                Button(
                    onClick = { navController.navigate("screen3") },
                    modifier = Modifier
                        .width(buttonWidth)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Guess the Flag")
                }
                Button(
                    onClick = { navController.navigate("screen4") },
                    modifier = Modifier
                        .width(buttonWidth)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Advanced Level")
                }
            }
        }
    )
}

@Composable
fun FlagImage(countryCode: String, modifier: Modifier = Modifier) {
    val flagCode = getFlagImageByCountryCode(countryCode)
    val flagImage: Painter = painterResource(id = flagCode)
    Image(
        painter = flagImage,
        contentDescription = "Flag",
        modifier = modifier
    )
}

fun getFlagImageByCountryCode(countryCode: String): Int {
    val countryCodeUpperCase = countryCode.toUpperCase()
    return when (countryCodeUpperCase) {
        in CountryCodeMap.codeToNameMap.keys -> {
            val countryCodeLowerCase = countryCodeUpperCase.toLowerCase() // Convert to lowercase
            val drawableName = countryCodeLowerCase.take(2) // Take first two characters
            val resourceId = try {
                R.drawable::class.java.getField(drawableName).getInt(null)
            } catch (e: Exception) {
                // Handle the case where the drawable is not found
                Log.e("FlagImageDebug", "Error retrieving drawable resource ID: ${e.message}")
                R.drawable.gb // Default flag resource ID
            }
            resourceId
        }
        else -> {
            Log.d("FlagImageDebug", "Country code not found in map: $countryCodeUpperCase")
            R.drawable.gb // Default flag resource ID
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen1(navController: NavHostController) {
    var selectedCountry by remember { mutableStateOf("") }
    var countryNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var flagCountryCode by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var correctCountry by remember { mutableStateOf("") }
    var wasPopupShown by remember { mutableStateOf(false) }
    var resultPopupShown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        countryNames = loadCountryNames()
        flagCountryCode = getRandomFlagCountryCode()
        correctCountry = getCountryNameByCountryCode(flagCountryCode)
    }

    val buttonText = if (wasPopupShown) "Next" else "Submit"

    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // Flag Image
                FlagImage(countryCode = flagCountryCode, modifier = Modifier.size(225.dp))
                Spacer(modifier = Modifier.height(10.dp))

                // Country List
                if (countryNames.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(countryNames) { countryName ->
                            CountryListItem(countryName = countryName, selectedCountry = selectedCountry) {
                                selectedCountry = it
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                } else {
                    // Show loading indicator or placeholder
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
                }

                // Submit Button
                Button(onClick = {
                    if (wasPopupShown) {
                        // Load a new flag and continue the game
                        navController.navigate("screen1")
                        isCorrect = null
                        wasPopupShown = false
                    } else {
                        // Inside the onClick lambda of the submit button
                        isCorrect = selectedCountry.equals(correctCountry, ignoreCase = true)
                        Log.d("Answer Comparison", "Selected: ${selectedCountry}, Correct: ${correctCountry}, Result: ${isCorrect}")
                        resultPopupShown = true
                    }
                }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(buttonText)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result Popup
                if (resultPopupShown) {
                    LaunchedEffect(isCorrect) {
                        // Show the result popup for 2 seconds
                        delay(2000)
                        resultPopupShown = false
                        wasPopupShown = true // Set the state to indicate that the popup was shown
                    }
                    ResultPopup(isCorrect ?: false, correctCountry) {
                        resultPopupShown = false
                        wasPopupShown = true // Update the flag when the popup closes
                    }
                }
            }
        }
    )
}

@Composable
fun ResultPopup(isCorrect: Boolean, correctCountry: String?, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = if (isCorrect) "CORRECT!" else "INCORRECT!",
                color = if (isCorrect) Color.Green else Color.Red
            )
        },
        text = {
            if (!isCorrect && correctCountry != null) {
                Text(
                    text = "$correctCountry.",
                    color = Color.Blue,
                    style = TextStyle(fontSize = 22.sp),
                )
            }
        },
        confirmButton = {
            // No confirm button
        }
    )
}


fun getCountryNameByCountryCode(countryCode: String): String {
    return CountryCodeMap.codeToNameMap[countryCode] ?: "Unknown"
}

fun getRandomFlagCountryCode(): String {
    val countryCodes = CountryCodeMap.codeToNameMap.keys.toList()
    val randomCountryCode = countryCodes.random()
    val randomCountryName = CountryCodeMap.codeToNameMap[randomCountryCode]
    Log.d("RandomCountryInfo", "Generated random country code: $randomCountryCode, Name: $randomCountryName")
    return randomCountryCode
}

@Composable
fun CountryListItem(
    countryName: String,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp), // Set rounded corners
        color = if (countryName == selectedCountry) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth() // Center the row horizontally
                .padding(16.dp)
                .clickable { onCountrySelected(countryName) },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center // Center the text horizontally
        ) {
            Text(
                text = countryName,
                style = TextStyle(fontSize = 20.sp)
            )
        }
    }
}

fun loadCountryNames(): List<String> {
    val countryNames = CountryCodeMap.codeToNameMap.values.toList()
    Log.d("CountryNamesDebug", "Loaded country names: $countryNames")
    return countryNames
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen2(navController: NavHostController) {
    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Guess-Hints")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* submit action */ }) {
                    Text("Submit")
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen3(navController: NavHostController) {
    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Guess the Flag")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* submit action */ }) {
                    Text("Submit")
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen4(navController: NavHostController) {
    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Advanced Level")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { /* submit action */ }) {
                    Text("Submit")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlagGuesserTheme {
        MainMenu(rememberNavController())
    }
}