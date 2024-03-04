@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.thusarakap.flagguesser.ui.theme.FlagGuesserTheme
import kotlinx.coroutines.delay
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale


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
            IconButton(onClick = { navController.navigateUp() }) {
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen1(navController: NavHostController) {
    var selectedCountry by remember { mutableStateOf("") }
    var countryNames by remember { mutableStateOf<List<String>>(emptyList()) }
    var flagCountryCode by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var correctCountry by remember { mutableStateOf("") }
    var resultPopupShown by remember { mutableStateOf(false) }
    var wasPopupShown by remember { mutableStateOf(false) } // New state variable to track if the popup was shown

    // Fetch country names synchronously
    countryNames = loadCountryNames(LocalContext.current)
    flagCountryCode = getRandomFlagCountryCode(LocalContext.current)
    correctCountry = getCountryNameByCountryCode(LocalContext.current, flagCountryCode)

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
                Spacer(modifier = Modifier.height(16.dp))

                // Flag Image
                FlagImage(countryCode = flagCountryCode, modifier = Modifier.size(200.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // Selected Country
                Text("Selected Country: $selectedCountry")

                // Country List
                if (countryNames.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(countryNames) { countryCode ->
                            CountryListItem(countryCode = countryCode, selectedCountry = selectedCountry) {
                                selectedCountry = it
                            }
                        }
                    }
                } else {
                    // Show loading indicator or placeholder
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
                }

                val buttonText = if (wasPopupShown) "Next" else "Submit"

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
                        Log.d("Answer Comparison", "Selected: $selectedCountry, Correct: $correctCountry, Result: $isCorrect")
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
                    }
                }
            }
        }
    )
}

@Composable
fun ResultPopup(isCorrect: Boolean, correctAnswer: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = if (isCorrect) "Correct Answer" else "Incorrect Answer",
            )
        },
        text = {
            if (isCorrect) {
                Text(
                    text = "Congratulations! Your answer is correct."
                )
            } else {
                Text(
                    text = "The correct answer is: $correctAnswer"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() }
            ) {
                Text("OK")
            }
        }
    )
}

@Composable
fun FlagImage(countryCode: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current // Get the context
    val resourceId = getFlagResourceIdByCountryCode(context, countryCode)
    val flagImage: Painter = painterResource(id = resourceId)
    Image(
        painter = flagImage,
        contentDescription = "Flag",
        modifier = modifier
    )
}

fun getFlagResourceIdByCountryCode(context: Context, countryCode: String): Int {
    val jsonString = getJsonStringFromAssets(context, "countries.json")
    val jsonObject = JSONObject(jsonString)
    val countryName = jsonObject.optString(countryCode, "Unknown")
    val drawableName = countryCode.toLowerCase(Locale.ROOT)

    // Log the country and drawable names
    Log.d("Country and Drawable", "Country: $countryName, Drawable: $drawableName")

    return context.resources.getIdentifier(drawableName, "drawable", context.packageName)
}


fun getCountryNameByCountryCode(context: Context, countryCode: String): String {
    val jsonString = getJsonStringFromAssets(context, "countries.json")
    val jsonObject = JSONObject(jsonString)
    return jsonObject.optString(countryCode, "Unknown")
}

fun getRandomFlagCountryCode(context: Context): String {
    val jsonString = getJsonStringFromAssets(context, "countries.json")
    val jsonObject = JSONObject(jsonString)
    val countryCodes = jsonObject.keys().asSequence().toList()

    val randomCountryCode = countryCodes.random()
    Log.d("RandomCountryCode", "Selected Country Code: $randomCountryCode")

    return randomCountryCode
}

@Composable
fun CountryListItem(
    countryCode: String,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    val countryName = getCountryNameByCountryCode(LocalContext.current, countryCode)
    Surface(
        color = if (countryName == selectedCountry) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable { onCountrySelected(countryName) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = countryName,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

fun loadCountryNames(context: Context): List<String> {
    val jsonString = getJsonStringFromAssets(context, "countries.json")
    val jsonObject = JSONObject(jsonString)
    return jsonObject.keys().asSequence().toList()
}

fun getJsonStringFromAssets(context: Context, fileName: String): String {
    val inputStream = context.assets.open(fileName)
    val reader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) {
        stringBuilder.append(line)
    }
    inputStream.close()
    return stringBuilder.toString()
}

//@Composable
//fun ResultPopup(isCorrect: Boolean, onDismiss: () -> Unit) {
//    AlertDialog(
//        onDismissRequest = { onDismiss() },
//        title = {
//            Text(
//                text = if (isCorrect) "Correct Answer" else "Incorrect Answer",
//            )
//        },
//        text = {
//            Text(
//                text = if (isCorrect) "Congratulations! Your answer is correct." else "Oops! Your answer is incorrect.",
//            )
//        },
//        confirmButton = {
//            Button(
//                onClick = { onDismiss() }
//            ) {
//                Text("OK")
//            }
//        }
//    )
//}

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

// Previews
@Preview(showBackground = true)
@Composable
fun Screen1Preview() {
    val navController = rememberNavController()
    FlagGuesserTheme {
        Surface {
            Screen1(navController = navController)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlagGuesserTheme {
        MainMenu(rememberNavController())
    }
}