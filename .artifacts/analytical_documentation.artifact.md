# Analytická dokumentácia aplikácie Kalkulačka (Cauculate)

## 1. Úvod a účel projektu
Aplikácia **Cauculate** je moderná kalkulačka pre operačný systém Android vyvinutá v jazyku Kotlin s využitím deklaratívneho frameworku **Jetpack Compose** a dizajnovej knižnice **Material 3**.

Cieľom projektu je poskytnúť používateľom intutívne, vizuálne atraktívne a vysoko funkčné rozhranie, ktoré kombinuje jednoduchosť klasickej vreckovej kalkulačky s pokročilými funkciami, inteligentným zobrazením histórie výpočtov a živým náhľadom výsledku v reálnom čase.

---

## 2. Architektúra a štruktúra aplikácie

Aplikácia je postavená na architektúre **Single Activity** spoločne s prístupom *Unidirectional Data Flow (UDF)* v Jetpack Compose.

- **`MainActivity.kt`**: Hlavný vstupný bod aplikácie nastavujúci Compose obsah v téme `CauculateTheme`.
- **`CalculatorScreen`**: HlavnáComposable obrazovka spravujúca stav kalkulačky, históriu a používateľské rozhranie.
- **`AdvancedOperationsPopup`**: Animované vyskakovacie okno (bublina) pre špeciálne operátory.
- **`CalcButton` & `SpecialOpButton`**: Znovu použiteľné komponenty tlačidiel s podporou zakázaného stavu (`disabled`) a vlastných tém.

---

## 3. Funkcionálna špecifikácia

### 3.1 Páska histórie výpočtov (Step-by-Step Tape)
- Zobrazuje každý zadaný operand a operátor v samostatnom riadku.
- Riadky sú orientované vpravo a zoradené tak, že staršie položky postupujú nahor a najnovšia zadaná hodnota/operátor je vždy dole.
- Používa komponent `LazyColumn` s nastavením `Arrangement.Bottom` a automatickým posúvaním na koniec (`animateScrollToItem`) pri pridaní nového kroku.

### 3.2 Trvalý živý výsledok (Live Result)
- Priebežný celkový výsledok sa nepretržite vyhodnocuje a zobrazuje v spodnej časti panelu histórie pod deliacou čiarou.
- Aktuálny výsledok reaguje na každú zmenu výrazu v reálnom čase.

### 3.3 Sekvenčné reťazové počítanie (Left-to-Right Chain Calculation)
- Operácie sa vyhodnocujú v poradí zľava doprava (stlačenie akéhokoľvek operátora okamžite vyhodnotí doterajší medzisúčet a použije ho ako základ pre ďalšiu operáciu).
- Špeciálne unárne operátory ($x^2$, $\sqrt{x}$, atď.) pracujú s **celkovým dosiahnutým medzisúčtom** výrazu. Príklad: `1 + 1` -> `x²` -> `+ 3` vyhodnotí $(1+1)^2 + 3 = 4 + 3 = 7$.

### 3.4 Inteligentný krok späť (Undo `↶`)
- Tlačidlo `↶` umožňuje krokovať späť v histórii bez jej vymazania.
- **Pravidlo 1**: Ak bol zadaný iba operátor (napr. `+`), stlačenie `↶` zmaže iba samotný operátor `+` a ponechá hodnotu pred ním.
- **Pravidlo 2**: Ak bola zadaná dvojica operátor + číslo (napr. `+ 3`), stlačenie `↶` zmaže celý tento krok naraz (odstráni `3` aj `+`).
- Tlačidlo je aktívne hneď pri zadávaní hodnôt bez nutnosti stláčať `=`.

### 3.5 Ochrana proti deleniu nulou
- Ak je aktívny operátor delenia (`÷`), tlačidlo čísla **`0`** sa automaticky znefunkční (`enabled = false`).
- Tlačidlo zostáva vizuálne prítomné na klávesnici v tlmenom stave. Po stlačení inej číslice sa opäť aktivuje.

### 3.6 Animovaná bublina špeciálnych operácií (`fx`)
- Tlačidlo `fx` otvorí animované vyskakovacie okno zobrazené v strede obrazovky.
- Využíva `AnimatedVisibility` s kombináciou `scaleIn` (zväčšenie z malého bodu s pružinovým efektom `Spring.DampingRatioMediumBouncy`) a `fadeIn`.
- **Obsiahnuté operátory**:
  - $x^2$ (Mocnina)
  - $\sqrt{x}$ (Odmocnina)
  - $1/x$ (Prevrátená hodnota)
  - $x!$ (Faktoriál)
  - $\pm$ (Zmena znamienka)
  - $\%$ (Percento)

---

## 4. Rozvrhnutie klávesnice (Keypad Layout)

| Riadok | Tlačidlo 1 | Tlačidlo 2 | Tlačidlo 3 | Tlačidlo 4 | Tlačidlo 5 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Riadok 1 (Toolbar)** | `C` (Reset) | `⌫` (Backspace) | `↶` (Undo) | `fx` (Špeciálne) | `=` (Rovná sa) |
| **Riadok 2** | `7` | `8` | `9` | `+` | – |
| **Riadok 3** | `4` | `5` | `6` | `-` | – |
| **Riadok 4** | `1` | `2` | `3` | `×` | – |
| **Riadok 5** | `0` (vypnuté pri `÷`) | `.` | `%` | `÷` | – |

*(Poradie operátorov v pravom stĺpci zhora nadol: `+`, `-`, `×`, `÷`).*

---

## 5. Algoritmus vyhodnocovania výrazov (`evaluateExpression`)

```kotlin
private fun evaluateExpression(tokens: List<String>): Double {
    // 1. Spracovanie znaku "=" a najnovšieho výsledku
    // 2. Vyhodnotenie zľava doprava:
    //    - Prechod cez tokeny: čísla, binárne operátory (+, -, ×, ÷), unárne operátory (², √, 1/x, !, ±, %)
    // 3. Vrátenie výsledku ako Double
}
```

---

## 6. Matica testovacích scenárov

| Scenár | Testovací vstup | Očakávaný výsledok | Reálny stav |
| :--- | :--- | :--- | :--- |
| **Reťazový výpočet** | `5 + 3 × 2` | `16` | PASSED |
| **Zmena znamienka** | `5` -> `±` -> `±` | `5` (návrat na pôvodné znamienko) | PASSED |
| **Zložitejšia mocnina** | `1 + 1` -> `x²` -> `+ 3` | `(1+1)² + 3 = 7` | PASSED |
| **Delenie nulou** | `5` -> `÷` | Tlačidlo `0` je neaktívne | PASSED |
| **Mazanie krok späť** | `1 + 1` -> `+ 3` -> `↶` | Vráti sa na `1 + 1`, výsledok `2` | PASSED |
| **Animovaná bublina** | Klik na `fx` | Plynulé zväčšenie okna s operátormi | PASSED |

---

## 7. Záver
Aplikácia **Cauculate** spĺňa všetky definované požiadavky na modernú, stabilnú a užívateľsky prívetivú kalkulačku s rozšírenou podporou špeciálnych operácií a histórie výpočtov.
