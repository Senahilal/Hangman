package com.example.hangman

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items


import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangman.ui.theme.HangmanTheme
import org.xmlpull.v1.XmlPullParser

//Returns true if the screen is in landscape mode
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

data class Word(val word: String, val hint: String)
val words = listOf(
    Word("Strawberry", "Fruit"),
    Word("Cat", "Animal"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangmanTheme {
                var selectedLetters by remember { mutableStateOf(setOf<Char>()) }
                var hintClicks by remember { mutableStateOf(0) } //Tracks hint button clicks
                var remainingTurns by remember { mutableStateOf(6) } //initially 6 incorrect guesses
                val context = LocalContext.current
                //var chosenWord = words[0] //hardcoding for now //TODO: fetch from words list randomly
                var chosenWord by remember { mutableStateOf(words.random()) }

                val wordLetters = chosenWord.word.uppercase().toSet()

                //when selectedLetters changes, calls getRevealedWord //remember( ) is like dependency array in useEffect-react
                val revealedWord = remember(selectedLetters) {
                    getRevealedWord(chosenWord.word, selectedLetters)
                }

                // Check if if the player has found all the letters
                var hasWon = true
                for (letter in wordLetters) {
                    if (!selectedLetters.contains(letter)) {
                        hasWon = false
                        break
                    }
                }

                // Game Over if there is no turns remaining
                if (remainingTurns == 0 || hasWon==true) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (hasWon==true)
                            {
                                Text(
                                    text = "You Won",
                                    fontSize = 20.sp,
                                    color = Color.Green,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            else{
                                Text(
                                    text = "Game Over! The word was: ${chosenWord.word}",
                                    fontSize = 20.sp,
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }


                            // Restart game
                            Button(
                                onClick = {
                                    selectedLetters = emptySet()
                                    hintClicks = 0
                                    remainingTurns = 6
                                    chosenWord = words.random() },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(text = "New Game")
                            }
                        }
                    }

                }

                //DISPLAYING CONTENT
                else {

                    val landscape = isLandscape()

                    if (!(landscape)) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            // Panel 3: Main Game Play Screen

                            //Hangman Stage Images
                            HangmanImage(context, remainingTurns)

                            //Word
                            WordDisplay(revealedWord)

                            // Panel 1: Letter Selection
                            LetterSelectionPanel(
                                selectedLetters = selectedLetters,
                                remainingTurns = remainingTurns,
                                chosenWord = chosenWord,
                                onLetterSelected = { letter, newTurns ->
                                    selectedLetters = selectedLetters + letter
                                    remainingTurns = newTurns as Int
                                }
                            )

                            Text(
                                text = "Remaining turns ${remainingTurns}",
                                modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                            )

                            // Panel 2: Hint Button
                            Button(
                                onClick = {
                                    handleHintClick(
                                        hintClicks,
                                        remainingTurns,
                                        selectedLetters,
                                        chosenWord,
                                        updateGameState = { updatedLetters, updatedHintClicks, updatedTurns ->
                                            selectedLetters = updatedLetters
                                            hintClicks = updatedHintClicks
                                            remainingTurns = updatedTurns
                                        },
                                        context = context
                                    )
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) {
                                Text(text = "Get Hint")
                            }

                            // Display hint after first button click
                            if (hintClicks >= 1) {
                                Text(
                                    text = "Hint: ${chosenWord.hint}",
                                    modifier = Modifier.padding(top = 8.dp),
                                    fontSize = 18.sp
                                )
                            }
                        }
                    } else {
                        // Landscape mode
                        // Using Row layout to show sections side by side.
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(10.dp).weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                // Panel 1: Letter Selection
                                LetterSelectionPanel(
                                    selectedLetters = selectedLetters,
                                    remainingTurns = remainingTurns,
                                    chosenWord = chosenWord,
                                    onLetterSelected = { letter, newTurns ->
                                        selectedLetters = selectedLetters + letter
                                        remainingTurns = newTurns as Int
                                    }
                                )

                                Text(
                                    text = "Remaining turns ${remainingTurns}",
                                    modifier = Modifier.padding(top = 10.dp, bottom = 10.dp)
                                )

                                // Panel 2: Hint Button
                                Button(
                                    onClick = {
                                        handleHintClick(
                                            hintClicks,
                                            remainingTurns,
                                            selectedLetters,
                                            chosenWord,
                                            updateGameState = { updatedLetters, updatedHintClicks, updatedTurns ->
                                                selectedLetters = updatedLetters
                                                hintClicks = updatedHintClicks
                                                remainingTurns = updatedTurns
                                            },
                                            context = context
                                        )
                                    },
                                    modifier = Modifier.padding(top = 16.dp)
                                ) {
                                    Text(text = "Get Hint")
                                }

                                // Display hint after first button click
                                if (hintClicks >= 1) {
                                    Text(
                                        text = "Hint: ${chosenWord.hint}",
                                        modifier = Modifier.padding(top = 8.dp),
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp).weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {

                                // Panel 3: Main Game Play Screen

                                //Hangman Stage Images
                                HangmanImage(context, remainingTurns)

                                //Word
                                WordDisplay(revealedWord)
                            }

                        }

                    }
                }




            }
        }
    }
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

@Composable
fun LetterSelectionPanel(selectedLetters: Set<Char>, remainingTurns: Int, chosenWord: Word,
                         onLetterSelected: (Char, Any?) -> Unit) {
    // A-Z Letters
    val letters = ('A'..'Z').toList()
    val wordUpper = chosenWord.word.uppercase()


    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(text = "CHOOSE A LETTER", modifier = Modifier.padding(bottom = 8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(letters) { letter ->
                Button(
                    onClick = {
                        val isCorrect = letter in wordUpper
                        val newTurns = if (!isCorrect) remainingTurns - 1 else remainingTurns

                        onLetterSelected(letter, newTurns)
                    },
                    enabled = letter !in selectedLetters,
                    shape = RoundedCornerShape(8.dp),
                    //modifier = Modifier.padding(4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFCCCCFF),
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WordDisplay(revealedWord: String) {
    Text(
        text = revealedWord,
        fontSize = 24.sp,
        modifier = Modifier.padding(30.dp)
    )
}

@Composable
fun HangmanImage(context: Context, remainingTurns: Int) {

    // Get drawable resource
    val imageResId = getDrawableId(remainingTurns)

    Image(
        painter = painterResource(id = imageResId),
        contentDescription = "Hangman Stage",
        modifier = Modifier
            .size(200.dp)
            .padding(16.dp)
    )
}


////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//Non composable functions

//handling hint button clicks
fun handleHintClick( hintClicks: Int, remainingTurns: Int, selectedLetters: Set<Char>, chosenWord: Word,
                     updateGameState: (Set<Char>, Int, Int) -> Unit, context: Context) {
    var newHintClicks = hintClicks + 1
    var newRemainingTurns = remainingTurns
    var newSelectedLetters = selectedLetters

    if (newRemainingTurns <= 1 && newHintClicks>1) {
        Toast.makeText(context, "No remaining turns! Hint not available", Toast.LENGTH_SHORT).show()
        newHintClicks-- // Prevent hint from applying
    }

    //Second hint click
    //disables half of the remaining letters (that are not part of the word)
    //costs a turn
    else if(newHintClicks == 2) {
        val incorrectLetters = ('A'..'Z')
            .filter { it !in chosenWord.word.uppercase() && it !in selectedLetters }

        //getting half of the incorrect unselected letters
        val lettersToDisable = incorrectLetters.shuffled().take(incorrectLetters.size / 2)

        //adding lettersToDisable into selectedLetters to disable buttons
        newSelectedLetters = selectedLetters + lettersToDisable //Disable half of incorrect letters
        newRemainingTurns--


    }

    // Third hint click
    // Reveal all vowels
    // costs a turn
    else if (newHintClicks == 3) {
        val vowels = setOf('A', 'E', 'I', 'O', 'U')

        //add all vowels into newSelectedLetters
        newSelectedLetters = selectedLetters + vowels
        newRemainingTurns--
    }

    updateGameState(newSelectedLetters, newHintClicks, newRemainingTurns) // update state in main
}

fun getRevealedWord(word: String, selectedLetters: Set<Char>): String {
    //checks every letter and if letter is in selectedLetters list reveals the letter
    return word.uppercase().map { letter ->
        if (letter in selectedLetters) letter else '_'
    }.joinToString(" ") // join with space "_ _ _"
}



fun getDrawableId(remainingTurns: Int): Int {
    return when (remainingTurns) {
        6 -> R.drawable.hhhhh1 // start here - when there is 6 remaining turns
        5 -> R.drawable.hhhhh2
        4 -> R.drawable.hhhhh3
        3 -> R.drawable.hhhhh4
        2 -> R.drawable.hhhhh5
        1 -> R.drawable.hhhhh6
        0 -> R.drawable.hhhhh7
        else -> R.drawable.hhhhh7
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HangmanTheme {

    }
}