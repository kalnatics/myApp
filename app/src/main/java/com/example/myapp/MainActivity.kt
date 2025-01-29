package com.example.myapp

import android.app.AlertDialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var deskripsi: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        deskripsi = findViewById(R.id.deskripsi)

        deskripsi.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("About Me")
                .setMessage(
                    "As a final-year student specializing in Rekayasa Perangkat Lunak " +
                            "at SMK Negeri 24 Jakarta, I am honing my skills in web development, " +
                            "demonstrated through my role as a Web Designer at Cerapproval International " +
                            "where I created several WordPress-based websites. " +
                            "My competencies lie in wireframing and back-end web development, " +
                            "with proficiency in tools like 3D Blender."
                )
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }
    fun openCalculator(view: View) {
        val intent = Intent(this, CalculatorActivity::class.java)
        startActivity(intent)
    }


}

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val usernameInput = findViewById<EditText>(R.id.username)
        val passwordInput = findViewById<EditText>(R.id.password)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val username = usernameInput.text.toString()
            val password = passwordInput.text.toString()

            if (username == "admin" && password == "1234") {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Login Gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

class CalculatorActivity : AppCompatActivity() {
    private lateinit var display: TextView
    private lateinit var resultDisplay: TextView
    private var currentInput: String = ""
    private var currentResult: Double = 0.0
    private var lastInputWasOperator = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        display = findViewById(R.id.display)
        resultDisplay = findViewById(R.id.resultDisplay)

        val buttonIds = listOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3, R.id.button4,
            R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9,
            R.id.buttonDot, R.id.buttonPlus, R.id.buttonMinus, R.id.buttonMultiply,
            R.id.buttonDivide, R.id.buttonEqual, R.id.clearButton, R.id.buttonBackspace
        )

        for (id in buttonIds) {
            findViewById<Button>(id).setOnClickListener { onButtonClick(it) }
        }

        findViewById<Button>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }

    private fun onButtonClick(view: View) {
        val button = view as Button
        val buttonText = button.text.toString()

        when (buttonText) {
            "CLEAR" -> onClearClick()
            "⌫" -> onBackspaceClick()
            "=" -> onEqualClick()
            "+", "-", "×", "÷" -> onOperatorClick(buttonText)
            else -> onNumberClick(buttonText)
        }
    }

    private fun onNumberClick(value: String) {
        if (value == "." && currentInput.contains(".")) return

        if (value == "0" && currentInput.isEmpty()) {
            currentInput = "0"
            updateDisplay()
            return
        }

        if (currentInput == "0" && value != ".") {
            currentInput = value
        } else {
            currentInput += value
        }

        lastInputWasOperator = false
        updateDisplay()
        calculateOnTheFly()
        playSound(R.raw.keypress)
    }

    private fun onOperatorClick(operator: String) {
        if (currentInput.isEmpty() && operator == "-") {
            currentInput = "-"
            updateDisplay()
            return
        }

        if (currentInput.isNotEmpty() && !lastInputWasOperator) {
            currentInput += " $operator "
            lastInputWasOperator = true
            updateDisplay()
            playSound(R.raw.keypress)
        } else if (lastInputWasOperator) {
            // Ganti operator terakhir
            currentInput = currentInput.substring(0, currentInput.length - 3) + " $operator "
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        val displayText = currentInput
            .replace("*", "×")
            .replace("/", "÷")
        display.text = displayText
    }

    private fun evaluateExpression(expression: String): Double {
        try {
            val evaluationString = expression
                .replace("×", "*")
                .replace("÷", "/")

            val tokens = evaluationString.trim().split("\\s+".toRegex())
            if (tokens.isEmpty()) return 0.0

            var result = tokens[0].toDouble()
            var i = 1

            while (i < tokens.size - 1) {
                val operator = tokens[i]
                val operand = tokens[i + 1].toDouble()

                result = when (operator) {
                    "+" -> result + operand
                    "-" -> result - operand
                    "*" -> result * operand
                    "/" -> {
                        if (operand == 0.0) throw ArithmeticException("Division by zero")
                        result / operand
                    }
                    else -> throw IllegalArgumentException("Operator tidak valid: $operator")
                }
                i += 2
            }
            return result
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("Format angka tidak valid")
        } catch (e: Exception) {
            throw e
        }
    }

    private fun calculateOnTheFly() {
        if (currentInput.isNotEmpty() && !lastInputWasOperator) {
            try {
                val evaluationString = currentInput
                    .replace("×", "*")
                    .replace("÷", "/")
                val result = evaluateExpression(evaluationString)
                resultDisplay.text = "= ${formatResult(result)}"
            } catch (e: Exception) {
                when (e) {
                    is ArithmeticException -> {
                        resultDisplay.text = "Error: Pembagian dengan nol"
                    }
                    is IllegalArgumentException -> {
                        resultDisplay.text = "Error: Format tidak valid"
                    }
                    else -> {
                        resultDisplay.text = "Error"
                    }
                }
            }
        } else {
            resultDisplay.text = ""
        }
    }

    private fun onClearClick() {
        currentInput = ""
        currentResult = 0.0
        lastInputWasOperator = false
        updateDisplay()
        resultDisplay.text = ""
        playSound(R.raw.clear)
    }

    private fun onBackspaceClick() {
        if (currentInput.isNotEmpty()) {
            if (currentInput.endsWith(" ")) {
                currentInput = currentInput.substring(0, currentInput.length - 3)
                lastInputWasOperator = false
            } else {
                currentInput = currentInput.substring(0, currentInput.length - 1)
            }
            updateDisplay()
            calculateOnTheFly()
            playSound(R.raw.keypress)
        }
    }

    private fun formatResult(result: Double): String {
        return if (result % 1 == 0.0) {
            result.toLong().toString()
        } else {
            String.format("%.8f", result).trimEnd('0').trimEnd('.')
        }
    }

    private fun playSound(soundResId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, soundResId)
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun onEqualClick() {
        if (currentInput.isNotEmpty() && !lastInputWasOperator) {
            try {
                val evaluationString = currentInput
                    .replace("×", "*")
                    .replace("÷", "/")
                val result = evaluateExpression(evaluationString)
                currentResult = result
                currentInput = formatResult(result)
                updateDisplay()
                playSound(R.raw.equal)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                resultDisplay.text = "Error"
            }
        }
    }
}

