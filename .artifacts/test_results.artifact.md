# Výsledky testovania kalkulačky

Tento dokument obsahuje komplexný prehľad otestovaných scenárov a funkčností pre aplikáciu kalkulačky.

| Scenár | Popis testu | Očakávané správanie | Stav |
| :--- | :--- | :--- | :--- |
| **Základné operácie** | Zžitie čísel 0-9 a operátorov `+`, `-`, `×`, `÷`. | Výpočet prebieha sekvenčne zľava doprava, výsledok sa zobrazuje priebežne. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
| **História výpočtu (Páska)** | Postupné zádávanie čísiel a operátorov. | Položky sa ukladajú do riadkov a páska sa automaticky posúva nahor. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
| **Živý výsledok** | Priebežné sledovanie medzisúčtu. | Výsledok sa aktualizuje v reálnom čase na spodku panela. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
| **Ochrana proti deleniu nulou** | Stlačenie operátora `÷`. | Tlačidlo `0` sa automaticky znefunkční (deaktivuje), kým sa nezadá iná číslica. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
| **Krok vzad (Undo `↶`)** | Zlyhanie/vrátenie kroku po `+ 3` alebo samotnom `+`. | Zmaže sa buď len osamotený operátor, alebo celý posledný krok (`+ 3`) bez straty histórie. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
| **Špeciálne operácie (`fx`)** | Kliknutie na tlačidlo `fx` a výber funkcie ($x^2$, $\sqrt{x}$, $1/x$, $x!$, $\pm$, `%`). | Vyskakovacia bublina s plynulou animáciou zväčšenia (zoom-in) aplikuje operáciu na aktuálny výsledok. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
| **Kombinovaný test** | Vstup: `1 + 1` -> `x²` -> `+ 3`. | Výpočet vyhodnotí `(1+1)² = 4`, pripočíta `3` -> `7`, pričom zachová históriu. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
| **Mazanie (`C`, `⌫`)** | Použitie vymazania celého vstupu (`C`) alebo jedného znaku (`⌫`). | Vstup sa úspešne resetuje na `0` alebo sa upraví posledný token. | ![OK](https://img.shields.io/badge/Stav-OK-green) |
