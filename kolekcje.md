
# Zadanie z kolekcji

Zaprojektuj program w Scali, który będzie analizował dane ruchu sieciowego. Program powinien przetwarzać listę połączeń sieciowych, gdzie każde połączenie jest reprezentowane jako krotka:
```scala
(źródłoIP: String, celIP: String, liczbaBajtów: Int)
```

Dane wejściowe są reprezentowane jako lista:

```scala
// Przykładowa lista
val połączenia: List[(String, String, Int)] = List(
  ("192.168.1.1", "10.0.0.2", 500),
  ("10.0.0.2", "192.168.1.1", 300),
  ("192.168.1.1", "10.0.0.3", 800),
  ("10.0.0.3", "10.0.0.4", 1000),
  ("10.0.0.4", "192.168.1.1", 200),
  ("192.168.1.1", "10.0.0.2", 700),
  ("10.0.0.2", "10.0.0.4", 900),
  ("10.0.0.3", "192.168.1.1", 100),
  ("10.0.0.3", "10.0.0.2", 200)
)
```

### Twoje zadania:

1. **Filtracja połączeń:**
   - Przygotuj listę połączeń, w których liczba przesłanych bajtów jest większa niż `500`.
   - Skorzystaj z metody `filter`, aby stworzyć nową listę połączeń spełniających ten warunek.

2. **Transformacja danych:**
   - Skorzystaj z metody `map`, aby przygotować listę, która będzie zawierać tylko adresy IP źródła (pola `źródłoIP`) z podanej listy. (`List("192.168.1.1", "10.0.0.2", "192.168.1.1", "10.0.0.3", ...)`)

3. **Łączenie danych:**
   - Skorzystaj z metod `flatMap` oraz `distinct`, aby stworzyć listę wszystkich unikalnych adresów IP występujących w polach `źródłoIP` oraz `celIP`.
> [!TIP]
> Spróbuj zrobić to samo metodami `flatMap`, `toSet` oraz `toList`!

4. **Sumowanie ruchu:**
   - Skorzystaj z metody `foldLeft`, aby obliczyć sumę wszystkich bajtów przesłanych w połączeniach, w których adres `celIP` to `"192.168.1.1"`.

5. **Analiza danych:**
   - Przygotuj zestawienie (`Map`'ę), które dla każdego adresu IP (z listy źródłowych adresów IP) obliczy, ile bajtów zostało wysłanych z danego adresu. Wykorzystaj do tego metody `groupBy`, `map` oraz `sum`.

6.  **Lista aktywnych IP:**
	- Dla każdej unikalnej pary `(źródłoIP, celIP)` policz sumę wszystkich przesłanych bajtów. Następnie utwórz **listę adresów IP**, które **były źródłem** w **co najmniej dwóch połączeniach**, gdzie **suma bajtów przekroczyła wartość** `1600`. Użyj między innymi metod `groupBy`, `map`, `sum`, `filter`, `keys`, oraz `toList`.
> [!TIP]
> `Map` oraz `HashMap` wspierają metody `filter` oraz `map` bez konieczności konwertowania do listy!

7. **Najbardziej obciążone IP**
	- Utwórz **listę adresów IP**, które były **celem połączeń**, a następnie oblicz **całkowitą liczbę bajtów** przesłanych do **każdego z tych adresów**. Z tej listy **wybierz IP** z **największą ilością przesłanych bajtów**. Skorzystaj z metod `groupBy`, `map`, `maxBy`, oraz `toList`.
