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
import androidx.activity.viewModels
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
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
import java.util.UUID

class FlagViewModel : ViewModel() {
    var selectedCountry by mutableStateOf("")
    var countryNames by mutableStateOf<List<String>>(emptyList())
    var flagCountryCode by mutableStateOf("")
    var isCorrect by mutableStateOf<Boolean?>(null)
    var correctCountry by mutableStateOf("")
    var resultPopupShown by mutableStateOf(false)
    var wasPopupShown by mutableStateOf(false)
}

class MainActivity : ComponentActivity() {
    private val viewModel: FlagViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagGuesserTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "menu") {
                    composable("menu") { MainMenu(navController) }
                    composable("screen1") {
                        // Generate a random UUID as the key
                        val key = remember { UUID.randomUUID().toString() }
                        Screen1(navController, viewModel, applicationContext) // pass applicationContext
                    }
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

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen1(navController: NavHostController, viewModel: FlagViewModel, context: Context) {
    // Fetch country names synchronously
    if (viewModel.countryNames.isEmpty()) {
        viewModel.countryNames = loadCountryNames(context)
    }

    // Update flag country code every time Screen1 recomposes
    viewModel.flagCountryCode = getRandomFlagCountryCode(context)

    // Update correct country whenever flag country code changes
    LaunchedEffect(viewModel.flagCountryCode) {
        viewModel.correctCountry = getCountryNameByCountryCode(context, viewModel.flagCountryCode)
    }

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
                FlagImage(countryCode = viewModel.flagCountryCode, modifier = Modifier.size(200.dp))
                Spacer(modifier = Modifier.height(16.dp))

                // Selected Country
                Text("Selected Country: ${viewModel.selectedCountry}")

                // Country List
                if (viewModel.countryNames.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(viewModel.countryNames) { countryCode ->
                            CountryListItem(countryCode = countryCode, selectedCountry = viewModel.selectedCountry) {
                                viewModel.selectedCountry = it
                            }
                        }
                    }
                } else {
                    // Show loading indicator or placeholder
                    CircularProgressIndicator(modifier = Modifier.padding(vertical = 16.dp))
                }

                val buttonText = if (viewModel.wasPopupShown) "Next" else "Submit"

                // Submit Button
                Button(onClick = {
                    if (viewModel.wasPopupShown) {
                        // Load a new flag and continue the game
                        navController.navigate("screen1")
                        viewModel.isCorrect = null
                        viewModel.wasPopupShown = false
                    } else {
                        // Inside the onClick lambda of the submit button
                        viewModel.isCorrect = viewModel.selectedCountry.equals(viewModel.correctCountry, ignoreCase = true)
                        Log.d("Answer Comparison", "Selected: ${viewModel.selectedCountry}, Correct: ${viewModel.correctCountry}, Result: ${viewModel.isCorrect}")
                        viewModel.resultPopupShown = true
                    }
                }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(buttonText)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result Popup
                if (viewModel.resultPopupShown) {
                    LaunchedEffect(viewModel.isCorrect) {
                        // Show the result popup for 2 seconds
                        delay(2000)
                        viewModel.resultPopupShown = false
                        viewModel.wasPopupShown = true // Set the state to indicate that the popup was shown
                    }
                    ResultPopup(viewModel.isCorrect ?: false, viewModel.correctCountry) {
                        viewModel.resultPopupShown = false
                        viewModel.wasPopupShown = true // Update the flag when the popup closes
                    }
                }
            }
        }
    )
}

@Composable
fun ResultPopup(isCorrect: Boolean, correctCountry: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = if (isCorrect) "CORRECT!" else "WRONG!",
                color = if (isCorrect) Color.Green else Color.Red
            )
        },
        text = {
            if (!isCorrect) {
                Text(
                    text = correctCountry,
                    color = Color.Blue
                )
            } else {
                // Empty Text composable for correct answers
                Text(text = "")
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
fun DefaultPreview() {
    FlagGuesserTheme {
        MainMenu(rememberNavController())
    }
}