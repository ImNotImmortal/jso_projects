package com.example.cauculate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.cauculate.ui.theme.CauculateTheme
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CauculateTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CalculatorScreen()
                }
            }
        }
    }
}

@Composable
fun CalculatorScreen() {
    var steps by remember { mutableStateOf(listOf("0")) }
    var hasCalculated by remember { mutableStateOf(false) }
    var showAdvancedBubble by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    val isZeroDisabled = remember(steps) {
        val last = steps.lastOrNull()
        last == "÷"
    }

    LaunchedEffect(steps.size) {
        if (steps.isNotEmpty()) {
            listState.animateScrollToItem(steps.size - 1)
        }
    }

    fun onNumber(num: String) {
        if (hasCalculated) {
            steps = listOf(num)
            hasCalculated = false
            return
        }

        val last = steps.lastOrNull() ?: "0"
        if (last == "0" && steps.size == 1) {
            steps = listOf(num)
        } else if (isOperator(last) || last == "=" || last == "²" || last == "√" || last == "1/x" || last == "!" || last == "±" || last == "%") {
            steps = steps + num
        } else {
            val updated = last + num
            steps = steps.dropLast(1) + updated
        }
    }

    fun onDecimal() {
        if (hasCalculated) {
            steps = listOf("0.")
            hasCalculated = false
            return
        }

        val last = steps.lastOrNull() ?: "0"
        if (isOperator(last) || last == "=") {
            steps = steps + "0."
        } else if (!last.contains(".")) {
            val updated = "$last."
            steps = steps.dropLast(1) + updated
        }
    }

    fun onOperator(op: String) {
        if (hasCalculated) {
            val res = evaluateExpression(steps)
            steps = listOf(formatResult(res), op)
            hasCalculated = false
            return
        }

        val last = steps.lastOrNull() ?: "0"
        if (last == "=") {
            val res = evaluateExpression(steps)
            steps = listOf(formatResult(res), op)
        } else if (isOperator(last)) {
            steps = steps.dropLast(1) + op
        } else {
            steps = steps + op
        }
    }

    fun onEquals() {
        if (hasCalculated) return
        val last = steps.lastOrNull() ?: "0"
        if (!isOperator(last) && last != "=") {
            val res = evaluateExpression(steps)
            steps = steps + "=" + formatResult(res)
            hasCalculated = true
        }
    }

    fun onPercentage() {
        steps = steps + "%"
        hasCalculated = true
    }

    fun onClear() {
        steps = listOf("0")
        hasCalculated = false
    }

    fun onUndo() {
        val last = steps.lastOrNull() ?: return

        if (last == "²" || last == "√" || last == "1/x" || last == "!" || last == "±" || last == "%") {
            steps = steps.dropLast(1)
            hasCalculated = steps.contains("=")
            return
        }

        if (isOperator(last)) {
            steps = steps.dropLast(1)
        } else if (steps.size > 2) {
            steps = steps.dropLast(2)
            hasCalculated = steps.lastOrNull() == "=" || steps.contains("=")
        } else if (steps.size == 2) {
            steps = steps.dropLast(1)
            hasCalculated = false
        } else {
            steps = listOf("0")
            hasCalculated = false
        }
    }

    fun onBackSpace() {
        if (hasCalculated) {
            steps = listOf("0")
            hasCalculated = false
            return
        }

        val last = steps.lastOrNull() ?: "0"
        if (isOperator(last)) {
            steps = steps.dropLast(1)
        } else {
            if (last.length > 1) {
                val updated = last.dropLast(1)
                steps = steps.dropLast(1) + updated
            } else {
                if (steps.size > 1) {
                    steps = steps.dropLast(1)
                } else {
                    steps = listOf("0")
                }
            }
        }
    }

    // Advanced operations handler preserving history tape
    fun applyAdvancedOperation(opKey: String) {
        showAdvancedBubble = false
        val symbol = when (opKey) {
            "x2" -> "²"
            "sqrt" -> "√"
            "reciprocal" -> "1/x"
            "factorial" -> "!"
            "negate" -> "±"
            "percent" -> "%"
            else -> ""
        }
        steps = steps + symbol
        hasCalculated = false
    }

    AdvancedOperationsPopup(
        expanded = showAdvancedBubble,
        onDismiss = { showAdvancedBubble = false },
        onSelect = { opKey -> applyAdvancedOperation(opKey) }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        // App Title
        Text(
            text = "Kalkulačka",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Step-by-step tape & Live Result Display Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Steps Tape (Auto-scrolling to newest item)
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    items(steps) { step ->
                        Text(
                            text = step,
                            style = if (step == "=" || isOperator(step)) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            color = when {
                                step == "=" -> MaterialTheme.colorScheme.primary
                                isOperator(step) -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(8.dp))

                // Live Result Always Visible at the bottom
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Výsledok",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = formatResult(evaluateExpression(steps)),
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Keypad Grid
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Row 1: C, ⌫, ↶, fx, =
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    text = "C",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ) { onClear() }
                CalcButton(
                    text = "⌫",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) { onBackSpace() }
                CalcButton(
                    text = "↶",
                    modifier = Modifier.weight(1f),
                    enabled = steps.size > 1 || (steps.size == 1 && steps[0] != "0"),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) { onUndo() }
                CalcButton(
                    text = "fx",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) { showAdvancedBubble = true }
                CalcButton(
                    text = "=",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) { onEquals() }
            }

            // Row 2: 7, 8, 9, +
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(text = "7", modifier = Modifier.weight(1f)) { onNumber("7") }
                CalcButton(text = "8", modifier = Modifier.weight(1f)) { onNumber("8") }
                CalcButton(text = "9", modifier = Modifier.weight(1f)) { onNumber("9") }
                CalcButton(
                    text = "+",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { onOperator("+") }
            }

            // Row 3: 4, 5, 6, -
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(text = "4", modifier = Modifier.weight(1f)) { onNumber("4") }
                CalcButton(text = "5", modifier = Modifier.weight(1f)) { onNumber("5") }
                CalcButton(text = "6", modifier = Modifier.weight(1f)) { onNumber("6") }
                CalcButton(
                    text = "-",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { onOperator("-") }
            }

            // Row 4: 1, 2, 3, ×
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(text = "1", modifier = Modifier.weight(1f)) { onNumber("1") }
                CalcButton(text = "2", modifier = Modifier.weight(1f)) { onNumber("2") }
                CalcButton(text = "3", modifier = Modifier.weight(1f)) { onNumber("3") }
                CalcButton(
                    text = "×",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { onOperator("×") }
            }

            // Row 5: 0, ., %, ÷
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CalcButton(
                    text = "0",
                    modifier = Modifier.weight(1f),
                    enabled = !isZeroDisabled
                ) { onNumber("0") }
                CalcButton(text = ".", modifier = Modifier.weight(1f)) { onDecimal() }
                CalcButton(
                    text = "%",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) { onPercentage() }
                CalcButton(
                    text = "÷",
                    modifier = Modifier.weight(1f),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) { onOperator("÷") }
            }
        }
    }
}

@Composable
fun AdvancedOperationsPopup(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    if (expanded) {
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = onDismiss
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() }
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedVisibility(
                    visible = expanded,
                    enter = fadeIn(animationSpec = tween(250)) + scaleIn(
                        initialScale = 0.1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    ),
                    exit = fadeOut(animationSpec = tween(150)) + scaleOut(targetScale = 0.1f, animationSpec = tween(150))
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = false) {}
                            .padding(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = MaterialTheme.shapes.extraLarge
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Špeciálne operácie",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SpecialOpButton(text = "x²", label = "Mocnina", modifier = Modifier.weight(1f)) { onSelect("x2") }
                                SpecialOpButton(text = "√x", label = "Odmocnina", modifier = Modifier.weight(1f)) { onSelect("sqrt") }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SpecialOpButton(text = "1/x", label = "Prevrátená", modifier = Modifier.weight(1f)) { onSelect("reciprocal") }
                                SpecialOpButton(text = "x!", label = "Faktoriál", modifier = Modifier.weight(1f)) { onSelect("factorial") }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                SpecialOpButton(text = "±", label = "Znamienko", modifier = Modifier.weight(1f)) { onSelect("negate") }
                                SpecialOpButton(text = "%", label = "Percento", modifier = Modifier.weight(1f)) { onSelect("percent") }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            TextButton(onClick = onDismiss) {
                                Text("Zavrieť", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SpecialOpButton(
    text: String,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(64.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = text, fontSize = 20.sp, style = MaterialTheme.typography.titleMedium)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(72.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = text,
            fontSize = 28.sp
        )
    }
}

private fun isOperator(token: String): Boolean {
    return token == "+" || token == "-" || token == "×" || token == "÷"
}

private fun factorial(n: Int): Long {
    if (n < 0 || n > 20) return 0
    var result = 1L
    for (i in 2..n) {
        result *= i
    }
    return result
}

private fun evaluateExpression(tokens: List<String>): Double {
    if (tokens.isEmpty()) return 0.0
    var cleanTokens = tokens.toMutableList()

    val equalsIndex = cleanTokens.lastIndexOf("=")
    if (equalsIndex != -1) {
        val afterEquals = cleanTokens.getOrNull(equalsIndex + 1)
        if (afterEquals != null && equalsIndex == cleanTokens.size - 2) {
            return afterEquals.toDoubleOrNull() ?: 0.0
        }
        cleanTokens = cleanTokens.subList(equalsIndex + 1, cleanTokens.size).toMutableList()
    }

    while (cleanTokens.isNotEmpty() && isOperator(cleanTokens.last())) {
        cleanTokens.removeAt(cleanTokens.size - 1)
    }
    if (cleanTokens.isEmpty()) return 0.0

    // Strict left-to-right evaluation supporting both binary and unary operators in sequence
    var i = 0
    var result = 0.0
    var pendingOp: String? = null

    while (i < cleanTokens.size) {
        val token = cleanTokens[i]
        val num = token.toDoubleOrNull()

        if (num != null) {
            if (pendingOp == null) {
                result = num
            } else {
                result = when (pendingOp) {
                    "+" -> result + num
                    "-" -> result - num
                    "×" -> result * num
                    "÷" -> if (num != 0.0) result / num else 0.0
                    else -> result
                }
                pendingOp = null
            }
            i++
        } else if (isOperator(token)) {
            pendingOp = token
            i++
        } else {
            // Unary operator (², √, 1/x, !, ±, %)
            result = when (token) {
                "²" -> result * result
                "√" -> if (result >= 0) sqrt(result) else 0.0
                "1/x" -> if (result != 0.0) 1.0 / result else 0.0
                "!" -> factorial(result.toInt()).toDouble()
                "±" -> -result
                "%" -> result / 100.0
                else -> result
            }
            i++
        }
    }

    return result
}

private fun formatResult(value: Double): String {
    return if (value == value.toLong().toDouble()) {
        value.toLong().toString()
    } else {
        value.toString()
    }
}

@Preview(showBackground = true)
@Composable
fun CalculatorPreview() {
    CauculateTheme {
        CalculatorScreen()
    }
}
