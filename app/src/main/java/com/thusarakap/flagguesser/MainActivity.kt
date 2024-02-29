@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.thusarakap.flagguesser

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
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

@Composable
fun FlagImage(countryCode: String, modifier: Modifier = Modifier) {
    val resourceId = getFlagResourceIdByCountryCode(countryCode)
    val flagImage: Painter = painterResource(id = resourceId)
    Image(
        painter = flagImage,
        contentDescription = "Flag",
        modifier = modifier
    )
}

fun getFlagResourceIdByCountryCode(countryCode: String): Int {
    return when (countryCode) {
        "FR" -> R.drawable.fr
        "EU" -> R.drawable.eu
        "AE" -> R.drawable.ae
        "ES" -> R.drawable.es
        // Add more cases for other country codes here
        else -> R.drawable.gb // Default flag resource ID
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

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        countryNames = loadCountryNames(context)
        flagCountryCode = getRandomFlagCountryCode()
        correctCountry = getCountryNameByCountryCode(flagCountryCode)
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

                // Submit Button
                Button(onClick = {
                    // Inside the onClick lambda of the submit button
                    isCorrect = selectedCountry == correctCountry
                }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Submit")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Result Popup
                isCorrect?.let {
                    ResultPopup(isCorrect = it) {
                        isCorrect = null
                        selectedCountry = ""
                        flagCountryCode = getRandomFlagCountryCode()
                        correctCountry = getCountryNameByCountryCode(flagCountryCode)
                    }
                }
            }
        }
    )
}






@Composable
fun ResultPopup(isCorrect: Boolean, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = if (isCorrect) "Correct Answer" else "Incorrect Answer",
            )
        },
        text = {
            Text(
                text = if (isCorrect) "Congratulations! Your answer is correct." else "Oops! Your answer is incorrect.",
            )
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

fun getCountryNameByCountryCode(countryCode: String): String {
    return when (countryCode) {
        "FR" -> "France"
        "GB" -> "United Kingdom"
        "EU" -> "European Union"
        "AE" -> "United Arab Emirates"
        "ES" -> "Spain"
        // Add more cases for other country codes here
        else -> "Unknown"
    }
}

fun getRandomFlagCountryCode(): String {
    val countryCodes = listOf(
        "FR", "EU", "AE", "ES", "GB"
        // Add more country codes here as needed
    )
    return countryCodes.random()
}

@Composable
fun CountryListItem(
    countryCode: String,
    selectedCountry: String,
    onCountrySelected: (String) -> Unit
) {
    val countryName = getCountryNameByCountryCode(countryCode)
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FlagGuesserTheme {
        MainMenu(rememberNavController())
    }
}