package com.dobdmitry.risuem.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

/** Бумага, на которой всё лежит: тёплая, не белая — глазам спокойнее. */
val Paper = Color(0xFFFFF6E7)
val PaperDeep = Color(0xFFFFE8C6)
val Ink = Color(0xFF3B3027)
val Accent = Color(0xFFE8552F)

/**
 * Двенадцать красок палитры.
 *
 * Тёплые и насыщенные, без кислоты: каждая узнаётся ребёнком и нормально
 * смотрится рядом с любой другой. Порядок — как в радуге, чтобы искать глазами.
 * Хотите другие цвета — меняйте только этот список, он один на всё приложение.
 */
val PaintColors: List<Color> = listOf(
    Color(0xFFE8503A), // красный
    Color(0xFFF07A2E), // оранжевый
    Color(0xFFF5C024), // жёлтый
    Color(0xFF9CC14A), // салатовый
    Color(0xFF4B9E5F), // зелёный
    Color(0xFF3FA9A2), // бирюзовый
    Color(0xFF4C9BD6), // голубой
    Color(0xFF3E63B0), // синий
    Color(0xFF8B5CB8), // фиолетовый
    Color(0xFFE877A6), // розовый
    Color(0xFF8B5E3C), // коричневый
    Color(0xFF3B3027), // чёрный
)

/**
 * Шрифт один на всё приложение: рубленый, очень жирный, без курсива.
 * Мельче 28 sp не бывает ничего — читает ребёнок четырёх лет.
 */
private val bigType = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 56.sp,
        textAlign = TextAlign.Center,
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 40.sp,
        textAlign = TextAlign.Center,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 32.sp,
        textAlign = TextAlign.Center,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Black,
        fontSize = 28.sp,
        textAlign = TextAlign.Center,
    ),
)

private val scheme = lightColorScheme(
    primary = Accent,
    onPrimary = Color.White,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperDeep,
    onSurfaceVariant = Ink,
)

@Composable
fun RisuemTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = scheme, typography = bigType, content = content)
}
