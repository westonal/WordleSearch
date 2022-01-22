package com.wordle

import com.google.gson.Gson
import java.io.InputStreamReader
import java.time.LocalDate

fun main() {
    // Wordle contains 2 word lists, one is the answers in the order they will appear, the other is a list of other
    // words that are valid guesses.
    val answers = loadWords("wordleAnswerList.json")
    val extendedWords = loadWords("wordleValidWordList.json")

    // Date prediction
    val referenceDate = answers.indexOf("robot") to LocalDate.parse("2022-01-20") // robot was the answer on 1/20/2022
    println("The Wordle will end on ${dateOf(answers.last(), answers, referenceDate)}")

    val allWords = answers + extendedWords
    val setOfLetterSets = allWords.map { it.toLetterSet() }.toSet()
    println("Loaded ${allWords.toSet().size} words, which has ${setOfLetterSets.size} unique letter sets")
    println()

    val winningWords = mutableSetOf<String>()
    val otherWordsThatFitTheBill = mutableSetOf<String>()
    val quickExample = mutableMapOf<String, String>()
    val count = mutableMapOf<String, Int>()

    val allWordsByLetterSet = allWords.groupBy { it.toLetterSet() }

    println("All solutions for words that are scheduled to win wordle one day:")
    println("  Legend:")
    println("  Word1, [Word2, Word2Alternatives...] -> Can only leave Word3")
    println()
    setOfLetterSets.toList()
        .forEachPair { w1, w2 ->
            val usedLetters = w1 or w2

            // Find a single word where there is no intersection between the set of used letters and its letter set
            val w3 = setOfLetterSets.singleOrNull { (it and usedLetters) == 0 }

            if (w3 != null) {
                // found a single matching word
                val theAnswer = findAllWords(allWordsByLetterSet, w3).single()

                val isAScheduledAnswer = answers.indexOf(theAnswer) != -1
                if (quickExample[theAnswer] == null) {
                    quickExample[theAnswer] =
                        "https://wordlyze.crud.net/analyze/" +
                                exampleWord(allWordsByLetterSet, w1) +
                                "," +
                                exampleWord(allWordsByLetterSet, w2)
                }
                count[theAnswer] = (count[theAnswer] ?: 0) + 1

                if (isAScheduledAnswer) {
                    // for brevity, I'm just listing all solutions for words that are scheduled to win wordle one day
                    println(
                        "${findAllWordsAsString(allWordsByLetterSet, w1)}, ${
                            findAllWordsAsString(
                                allWordsByLetterSet,
                                w2
                            )
                        } -> $theAnswer"
                    )
                    winningWords += theAnswer
                } else {
                    // but there's nothing wrong with these answers either
                    otherWordsThatFitTheBill += theAnswer
                }
            }
        }

    println()
    println("These words will win wordle in the future, and satisfy the scenario:")
    winningWords.map { it to dateOf(it, answers, referenceDate) }.sortedBy { it.first }
        .forEach { (w, date) -> println("  on $date $w") }
    println()

    println("These ${otherWordsThatFitTheBill.size} words aren't scheduled to win, but satisfy the scenario:")
    otherWordsThatFitTheBill.sortedBy { it }
        .forEach { w -> println("  $w") }

    println()
    println("Some examples links for all ${quickExample.size} words")
    quickExample.map { it }.sortedBy { count[it.key] }
        .forEach { (key, value) -> println("$value,$key (x${count[key]})") }
}

fun dateOf(s: String, answers: Array<String>, reference: Pair<Int, LocalDate>): LocalDate {
    val (referenceIndex, referenceDate) = reference
    val days = answers.indexOf(s) - referenceIndex
    return referenceDate.plusDays(days.toLong())
}

fun findAllWords(allWords: Map<LetterSet, List<String>>, letters: LetterSet) =
    allWords[letters] ?: emptyList()

fun findAllWordsAsString(allWords: Map<LetterSet, List<String>>, letters: LetterSet) =
    findAllWords(allWords, letters).run {
        singleOrNull() ?: ("[" + sorted().joinToString(", ") + "]")
    }

fun exampleWord(allWords: Map<LetterSet, List<String>>, letters: LetterSet) =
    findAllWords(allWords, letters).first { w -> w.toLetterSet() == letters }

fun LetterSet.describe(): String {
    val stringBuilder = StringBuilder(5)
    for (i in 0 until 26) {
        if (this and (1 shl i) != 0) {
            stringBuilder.append(('a' + i))
        }
    }
    return stringBuilder.toString()
}

/**
 * Letter set is a binary number with the least significant bit representing 'a' and 26th least significant bit
 * representing 'z'.
 */
typealias LetterSet = Int

const val allLetters = (1 shl 26) - 1

// I guess I didn't need this in the end but can be used to find out the available letters
fun LetterSet.invert(): LetterSet = allLetters and this.inv()

private operator fun LetterSet.plus(c: Char): LetterSet = this or (1 shl c - 'a')

/**
 * Example:
 * (1..5).toList().forEachPair { a, b -> println("$a, $b") }
 */
private fun <E> List<E>.forEachPair(function: (a: E, b: E) -> Unit) {
    for (i in 0..size - 2) {
        for (j in i + 1 until size) {
            function(this[i], this[j])
        }
    }
}

private fun loadWords(wordFile: String) =
    Gson().fromJson(
        InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(wordFile)!!),
        Json::class.java
    ).words

fun String.toLetterSet(): LetterSet {
    var result = 0
    this.toCharArray()
        .forEach { c ->
            result += c
        }
    return result
}

class Json(val words: Array<String>)
