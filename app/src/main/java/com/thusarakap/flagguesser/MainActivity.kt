@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class
)

package com.thusarakap.flagguesser

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _switchState = MutableStateFlow(false)
    val switchState: StateFlow<Boolean> = _switchState

    fun saveSwitchState(state: Boolean) {
        viewModelScope.launch {
            _switchState.emit(state)
        }
    }
}

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlagGuesserTheme {
                val navController = rememberNavController()
                val switchState by viewModel.switchState.collectAsState()

                NavHost(navController, startDestination = "menu") {
                    composable("menu") { MainMenu(navController, switchState) { newState ->
                        viewModel.saveSwitchState(newState)
                    } }
                    composable("screen1") { Screen1(navController, switchState) }
                    composable("screen2") { Screen2(navController, switchState) }
                    composable("screen3") { Screen3(navController, switchState) }
                    composable("screen4") { Screen4(navController, switchState) }
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
fun MainMenu(navController: NavHostController, switchState: Boolean, onSwitchToggle: (Boolean) -> Unit) {
    val buttonWidth = 230.dp

    Scaffold(
        content = { _ ->
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Menu")

                Switch(checked = switchState, onCheckedChange = onSwitchToggle)

                Spacer(modifier = Modifier.height(15.dp))
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
    return when (val countryCodeUpperCase = countryCode.uppercase()) {
        in CountryCodeMap.codeToNameMap.keys -> {
            val countryCodeLowerCase = countryCodeUpperCase.lowercase() // Convert to lowercase
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
fun Screen1(navController: NavHostController, switchState: Boolean) {
    var selectedCountry by rememberSaveable { mutableStateOf("") }
    var countryNames by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var flagCountryCode by rememberSaveable { mutableStateOf("") }
    var isCorrect by rememberSaveable { mutableStateOf<Boolean?>(null) }
    var correctCountry by rememberSaveable { mutableStateOf("") }
    var wasPopupShown by rememberSaveable { mutableStateOf(false) }
    var resultPopupShown by rememberSaveable { mutableStateOf(false) }

    var remainingTime by rememberSaveable { mutableIntStateOf(10) }

    val scrollState = rememberScrollState()

    LaunchedEffect(switchState) {
        if (switchState) {
            while (remainingTime > 0) {
                delay(1000L)
                remainingTime--
            }
            isCorrect = selectedCountry.equals(correctCountry, ignoreCase = true)
            Log.d("Answer Comparison", "Selected: $selectedCountry, Correct: $correctCountry, Result: $isCorrect")
            resultPopupShown = true
        }
    }

    LaunchedEffect(Unit) {
        countryNames = loadCountryNames()
        if (flagCountryCode.isEmpty()) {
            flagCountryCode = getRandomFlagCountryCode()
        }
        correctCountry = getCountryNameByCountryCode(flagCountryCode)
    }

    val buttonText = if (wasPopupShown) "Next" else "Submit"

    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(scrollState),

                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))

                // Flag Image
                FlagImage(countryCode = flagCountryCode, modifier = Modifier.size(225.dp))

                if (switchState) {
                    Text(text = "Timer: $remainingTime")
                } else {
                    Text(text = "Timer is off")
                }

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
                    Spacer(modifier = Modifier.height(25.dp))
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
                    text = correctCountry,
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
fun Screen2(navController: NavHostController, switchState: Boolean) {
    var countryCode by rememberSaveable { mutableStateOf("") }
    var countryName by rememberSaveable { mutableStateOf("") }
    var dashes by rememberSaveable { mutableStateOf("") }
    var userInput by rememberSaveable { mutableStateOf("") }
    var incorrectGuesses by rememberSaveable { mutableIntStateOf(0) }
    var resultMessage by rememberSaveable { mutableStateOf("") }
    var resultPopupShown by rememberSaveable { mutableStateOf(false) }
    var wasPopupShown by rememberSaveable { mutableStateOf(false) }

    var remainingTime by rememberSaveable { mutableIntStateOf(10) }

    val scrollState = rememberScrollState()

    LaunchedEffect(switchState) {
        if (switchState) {
            while (remainingTime > 0) {
                delay(1000L)
                remainingTime--
            }
            resultMessage = "WRONG!"
            resultPopupShown = true
            wasPopupShown = true
        }
    }

    LaunchedEffect(Unit) {
        // Generate a random flag country code and corresponding country name
        if (countryCode.isEmpty()) {
            countryCode = getRandomFlagCountryCode()
        }
        countryName = getCountryNameByCountryCode(countryCode)

        // Initialize the dashes string with spaces between each dash
        dashes = countryName.map { if (it.isWhitespace()) "  " else "- " }.joinToString("")
    }

    fun handleSubmit(userInput: String) {
        // Convert the user input to uppercase to make the comparison case-insensitive
        val inputChar = userInput.uppercase().firstOrNull() ?: return

        // Convert the country name and dashes to char arrays for easy manipulation
        val countryNameChars = countryName.uppercase().toCharArray()
        val dashesChars = dashes.replace(" ", "").toCharArray()

        var isGuessCorrect = false

        // Iterate over the country name characters
        for (i in countryNameChars.indices) {
            // If the current character is equal to the user input, replace the corresponding dash
            if (countryNameChars[i] == inputChar) {
                dashesChars[i] = inputChar
                isGuessCorrect = true
            }
        }

        // Convert the dashes char array back to a string with spaces between each character
        dashes = dashesChars.joinToString(" ")

        if (!isGuessCorrect) {
            incorrectGuesses++
        }

        if (incorrectGuesses >= 3) {
            resultMessage = "WRONG!"
            resultPopupShown = true
            wasPopupShown = true
        } else if (!dashes.contains('-')) {
            resultMessage = "CORRECT!"
            resultPopupShown = true
            wasPopupShown = true
        }
    }

    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Flag Image
                FlagImage(countryCode = countryCode, modifier = Modifier.size(225.dp))

                Spacer(modifier = Modifier.height(1.dp))

                if (switchState) {
                    Text(text = "Timer: $remainingTime")
                } else {
                    Text(text = "Timer is off")
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Dashes representing the country name
                Text(text = dashes, style = TextStyle(fontSize = 24.sp))

                Spacer(modifier = Modifier.height(16.dp))

                // Textbox for user input
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    label = { Text("Enter a character") },
                    singleLine = true,
                    modifier = Modifier.width(350.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                val buttonText = if (wasPopupShown) "Next" else "Submit"

                // Submit Button
                Button(onClick = {
                    if (wasPopupShown) {
                        // Reset the game and load a new flag
                        navController.navigate("screen2")
                    } else {
                        handleSubmit(userInput)
                    }
                }) {
                    Text(buttonText)
                }

                // Result Popup
                if (resultPopupShown) {
                    ResultPopup(isCorrect = resultMessage == "CORRECT!", correctCountry = if (resultMessage == "WRONG!") countryName else null) {
                        resultPopupShown = false
                    }
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen3(navController: NavHostController, switchState: Boolean) {
    var countryCodes by rememberSaveable { mutableStateOf<Set<String>>(emptySet()) }
    var correctCountryCode by rememberSaveable { mutableStateOf("") }
    var correctCountryName by rememberSaveable { mutableStateOf("") }
    var hasAttempted by rememberSaveable { mutableStateOf(false) }
    var isCorrect by rememberSaveable { mutableStateOf(false) }

    var remainingTime by rememberSaveable { mutableIntStateOf(10) }

    val scrollState = rememberScrollState()

    LaunchedEffect(switchState) {
        if (switchState) {
            while (remainingTime > 0) {
                delay(1000L)
                remainingTime--
            }
            hasAttempted = true
        }
    }

    LaunchedEffect(Unit) {
        if (countryCodes.isEmpty()) {
            val generatedCountryCodes = mutableSetOf<String>()
            while (generatedCountryCodes.size < 3) {
                val newCountryCode = getRandomFlagCountryCode()
                generatedCountryCodes.add(newCountryCode)
            }
            countryCodes = generatedCountryCodes
        }
        if (correctCountryCode.isEmpty()) {
            correctCountryCode = countryCodes.random()
        }
        correctCountryName = getCountryNameByCountryCode(correctCountryCode)
    }

    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Text(
                    text = correctCountryName,
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (switchState) {
                    Text(text = "Timer: $remainingTime")
                } else {
                    Text(text = "Timer is off")
                }

                countryCodes.forEach { countryCode ->
                    FlagImage(countryCode = countryCode, modifier = Modifier
                        .size(200.dp)
                        .clickable(enabled = !hasAttempted) {
                            if (!hasAttempted) {
                                hasAttempted = true
                                isCorrect = (countryCode == correctCountryCode)
                            }
                        })
                }

                if (hasAttempted) {
                    Text(
                        text = if (isCorrect) "CORRECT!" else "INCORRECT!",
                        color = if (isCorrect) Color.Green else Color.Red
                    )
                } else {
                    Spacer(modifier = Modifier.height(22.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = { navController.navigate("screen3") }) {
                    Text("Next")
                }
            }
        }
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen4(navController: NavHostController, switchState: Boolean) {
    var countryCodes by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
    var userInput1 by rememberSaveable { mutableStateOf("") }
    var userInput2 by rememberSaveable { mutableStateOf("") }
    var userInput3 by rememberSaveable { mutableStateOf("") }
    var isCorrect1 by rememberSaveable { mutableStateOf(false) }
    var isCorrect2 by rememberSaveable { mutableStateOf(false) }
    var isCorrect3 by rememberSaveable { mutableStateOf(false) }
    var isSubmitted by rememberSaveable { mutableStateOf(false) }
    var incorrectAttempts by rememberSaveable { mutableIntStateOf(0) }

    var remainingTime by rememberSaveable { mutableIntStateOf(10) }

    val scrollState = rememberScrollState()

    LaunchedEffect(switchState) {
        if (switchState) {
            while (remainingTime > 0) {
                delay(1000L)
                remainingTime--
            }
            // When the timer reaches 0, perform the same logic as clicking the submit button three times
            if (incorrectAttempts < 3) {
                incorrectAttempts = 3
            }
            isCorrect1 = userInput1.equals(getCountryNameByCountryCode(countryCodes[0]), ignoreCase = true)
            isCorrect2 = userInput2.equals(getCountryNameByCountryCode(countryCodes[1]), ignoreCase = true)
            isCorrect3 = userInput3.equals(getCountryNameByCountryCode(countryCodes[2]), ignoreCase = true)
            isSubmitted = true
        }
    }

    LaunchedEffect(Unit) {
        if (countryCodes.isEmpty()) {
            val generatedCountryCodes = mutableSetOf<String>()
            while (generatedCountryCodes.size < 3) {
                val newCountryCode = getRandomFlagCountryCode()
                generatedCountryCodes.add(newCountryCode)
            }
            countryCodes = generatedCountryCodes.toList()
        }
    }

    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(60.dp))

                if (switchState) {
                    Text(text = "Timer: $remainingTime")
                } else {
                    Text(text = "Timer is off")
                }

                if (countryCodes.size == 3) {
                    FlagImage(countryCode = countryCodes[0], modifier = Modifier.size(150.dp))
                    OutlinedTextField(
                        value = userInput1,
                        onValueChange = { userInput1 = it },
                        label = { Text("Enter Flag 1 name") },
                        readOnly = isCorrect1,
                        textStyle = TextStyle(color = if (isSubmitted && isCorrect1) Color.Green else if (isSubmitted && userInput1.isNotEmpty() && !isCorrect1) Color.Red else Color.Unspecified)
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    if (incorrectAttempts >= 3 && !isCorrect1) {
                        Text(getCountryNameByCountryCode(countryCodes[0]), color = Color.Blue)
                    }


                    FlagImage(countryCode = countryCodes[1], modifier = Modifier.size(150.dp))
                    OutlinedTextField(
                        value = userInput2,
                        onValueChange = { userInput2 = it },
                        label = { Text("Enter Flag 2 name") },
                        readOnly = isCorrect2,
                        textStyle = TextStyle(color = if (isSubmitted && isCorrect2) Color.Green else if (isSubmitted && userInput2.isNotEmpty() && !isCorrect2) Color.Red else Color.Unspecified)
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    if (incorrectAttempts >= 3 && !isCorrect2) {
                        Text(getCountryNameByCountryCode(countryCodes[1]), color = Color.Blue)
                    }

                    FlagImage(countryCode = countryCodes[2], modifier = Modifier.size(150.dp))
                    OutlinedTextField(
                        value = userInput3,
                        onValueChange = { userInput3 = it },
                        label = { Text("Enter Flag 3 name") },
                        readOnly = isCorrect3,
                        textStyle = TextStyle(color = if (isSubmitted && isCorrect3) Color.Green else if (isSubmitted && userInput3.isNotEmpty() && !isCorrect3) Color.Red else Color.Unspecified)
                    )
                    Spacer(modifier = Modifier.height(5.dp))

                    if (incorrectAttempts >= 3 && !isCorrect3) {
                        Text(getCountryNameByCountryCode(countryCodes[2]), color = Color.Blue)
                    }
                }
                if (incorrectAttempts >= 3) {
                    Text("WRONG!", color = Color.Red)
                } else if (isSubmitted && isCorrect1 && isCorrect2 && isCorrect3) {
                    Text("CORRECT!", color = Color.Green)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Button(onClick = {
                    if (incorrectAttempts >= 3 || (isSubmitted && isCorrect1 && isCorrect2 && isCorrect3)) {
                        // Reset the game and load new flags
                        navController.navigate("screen4")
                    } else {
                        isSubmitted = true
                        if (userInput1.equals(getCountryNameByCountryCode(countryCodes[0]), ignoreCase = true)) {
                            isCorrect1 = true
                        }
                        if (userInput2.equals(getCountryNameByCountryCode(countryCodes[1]), ignoreCase = true)) {
                            isCorrect2 = true
                        }
                        if (userInput3.equals(getCountryNameByCountryCode(countryCodes[2]), ignoreCase = true)) {
                            isCorrect3 = true
                        }
                        if (!isCorrect1 || !isCorrect2 || !isCorrect3) {
                            incorrectAttempts++
                        }
                    }
                }) {
                    Text(if (incorrectAttempts >= 3 || (isSubmitted && isCorrect1 && isCorrect2 && isCorrect3)) "Next" else "Submit")
                }
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainMenuPreview() {
    FlagGuesserTheme {
        val navController = rememberNavController()
        MainMenu(navController, false) { }
    }
}

@Preview(showBackground = true)
@Composable
fun Screen1Preview() {
    FlagGuesserTheme {
        val navController = rememberNavController()
        Screen1(navController, false)
    }
}

@Preview(showBackground = true)
@Composable
fun Screen2Preview() {
    FlagGuesserTheme {
        val navController = rememberNavController()
        Screen2(navController, false)
    }
}

@Preview(showBackground = true)
@Composable
fun Screen3Preview() {
    FlagGuesserTheme {
        val navController = rememberNavController()
        Screen3(navController, false)
    }
}

@Preview(showBackground = true)
@Composable
fun Screen4Preview() {
    FlagGuesserTheme {
        val navController = rememberNavController()
        Screen4(navController, false)
    }
}