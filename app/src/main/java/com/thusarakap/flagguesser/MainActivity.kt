@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.thusarakap.flagguesser

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader
import com.thusarakap.flagguesser.ui.theme.FlagGuesserTheme

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var context: Context
            private set
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = applicationContext
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


fun readCountriesJsonFromRaw(): String {
    val inputStream = MainActivity.context.resources.openRawResource(R.raw.countries)
    val bufferedReader = BufferedReader(InputStreamReader(inputStream))
    val stringBuilder = StringBuilder()
    var line: String? = bufferedReader.readLine()
    while (line != null) {
        stringBuilder.append(line)
        line = bufferedReader.readLine()
    }
    return stringBuilder.toString()
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
fun FlagImage(resourceId: Int, modifier: Modifier = Modifier) {
    val flagImage: Painter = painterResource(id = resourceId)
    Image(
        painter = flagImage,
        contentDescription = "Flag",
        modifier = modifier
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Screen1(navController: NavHostController) {
    val flagImageId by remember { mutableStateOf(getRandomFlagResourceId()) }
    val countryNames by remember { mutableStateOf(loadCountryNames()) }

    Scaffold(
        topBar = { TopBar(navController) },
        content = {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                FlagImage(flagImageId, modifier = Modifier.size(200.dp))
                LazyColumn(
                    modifier = Modifier.weight(1f),
                ) {
                    items(countryNames) { countryName ->
                        Text(countryName)
                    }
                }
            }
        }
    )
}

fun loadCountryNames(): List<String> {
    val countriesJson = readCountriesJsonFromRaw()
    val gson = Gson()
    val countryNamesMap: Map<String, String> = gson.fromJson(countriesJson, Map::class.java) as Map<String, String>
    return countryNamesMap.values.toList()
}


fun getRandomFlagResourceId(): Int {
    val drawableResources = listOf(
        R.drawable.fr,
        R.drawable.eu,
        R.drawable.ae,
        R.drawable.es,
        R.drawable.gb
        // Add more drawable resource IDs here as needed
    )
    return drawableResources.random()
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
