# Zadanie z aktorów

### Opis

Zaimplementuj system aktorów symulujący działanie kawiarni. System powinien składać się z aktora głównego, typu **Manager**:

```scala
class Manager extends Actor { ... }
```

oraz dynamicznie określanej (w momencie inicjowania działania Managera) liczby aktorów pracowników typu **Barista**:

```scala
class Barista extends Actor { ... }  // Odpowiedzialni za przygotowanie kawy w Maszynie
```

Dodatkowo, w systemie aktorów powinni istnieć aktorzy (po jednym) typu **Kasa** i **Maszyna**:

```scala
class Kasa extends Actor { ... }     // Odpowiedzialna za sumowanie kwot zamówień
class Maszyna extends Actor { ... }  // Odpowiedzialna za zrobienie kawy
```

### Scenariusz

1. **Manager** po uruchomieniu programu jest w stanie przyjąć jedynie komunikat inicjalizacyjny:

    ```scala
    case class InitKawiarnia(liczbaZamowien: Int, liczbaBaristow: Int)
    ```

   W efekcie tego komunikatu powinien on utworzyć aktorów: **Barista** (tylu, ile wynosi `liczbaBaristow`), **Kasa**, oraz **Maszyna**. Każdy z nich otrzymuje komunikat inicjalizacyjny:

    ```scala
    case class InitBarista(/* TODO: wymyśl listę parametrów */)
    case class InitKasa(/* TODO: wymyśl listę parametrów */)
    case class InitMaszyna(/* TODO: wymyśl listę parametrów */)
    ```

2. Po inicjalizacji, **Manager** przechodzi do stanu, w którym może przyjmować "zamówienia" z daną kwotą za pomocą komunikatu:

    ```scala
    case class Zamowienie(kwota: Int)
    ```
    **Manager** nie powinien przyjąć w sumie więcej zamówień niż wskazuje liczba `liczbaZamowien`. Zamówienia wykonane po przekroczeniu tej liczby mają być odrzucane przez wyświetlenie na ekranie odpowiedniego komunikatu (np. `Nie przyjmujemy więcej zamówień!`).

3. Gdy **Manager** otrzyma zamówienie, powiadamia **Kasę** o jego kwocie.

    ```scala
    case class ZarejestrujOplate(kwota: Int)
    ```

    **Kasa** powinna sumować otrzymane kwoty i przechowywać je w tożsamości. ~~Po dodaniu otrzymanej kwoty do sumy, **Kasa** powinna odesłać do **Managera** potwierdzenie płatności za pomocą komunikatu:~~

4. ~~**Manager** po otrzymaniu potwierdzenia płatności,~~ Następnie **Manager** od razu przekazuje zamówienie do pierwszego dostępnego (nie "zajętego") **Baristy** za pomocą komunikatu

    ```scala
    case object PrzygotujKawe
    ```

5. Dany **Barista** po otrzymaniu komunikatu `PrzygotujKawe` musi spróbować przygotować ją w **Maszynie**. Robi to za pomocą komunikatu wysyłanego do **Maszyna**:

    ```scala
    case object ZrobKawe
    ```
> [!IMPORTANT]
> **Barista**, który nie skończył wykonywać zamówienia, nie może dostać kolejnego komunikatu `PrzygotujKawe`. Uznajemy, że jest on w tym czasie "zajęty".
> Jeśli nie ma "wolnych" **Baristów**, **Manager** powinien przechować zamówienie i wysłać je pierwszemu **Bariście**, który skończy pracować nad swoim aktualnym zamówieniem (**Barista** kończy pracę nad swoim zamówieniem za pomocą komunikatu `ZamowienieWykonane`, który przedstawiony jest niżej).

6. **Maszyna** powinna posiadać natomiast swoją *zdolność*, która jest dowolną dodatnią liczbą całkowitą. Jeśli *zdolność* **Maszyny** wynosi `0`, powinna ona odpowiedzieć **Bariście** komunikatem:

    ```scala
    case object BrakZdolnosci
    ```
    **Barista** powinien w takim wypadku najpierw wysłać do **Maszyny** komunikat:
    ```scala
    case object NaprawMaszyne
    ```
    A następnie ponowić komunikat `ZrobKawe`.
    **Maszyna** natomiast po przyjęciu komunikatu `NaprawMaszyne` powinna zresetować swoją *zdolność* na liczbę odpowiadającą *zdolności* maszyny na początku (np.  jeśli na początku maszyna miała zdolność `5`, to "naprawa" powinna przywrócić ją również na poziom `5`).
    
    Jeśli *zdolność* **Maszyny** jest **większa od zera** (przez brak jej użycia lub wykonaną właśnie "naprawę"), powinna odpowiedzieć **Bariście** komunikatem:
    ```scala
    case object Kawa
    ```
> [!NOTE]
> Postaraj się zaimplementować resetowanie poziomu *zdolności* przez "naprawę" tak, aby **Maszyna** sama wiedziała jaką *zdolność* miała na początku i do niej wracała.

7. Kiedy **Barista** otrzyma gotową kawę (komunikat `Kawa`) od **Maszyny**, powinien dać znać **Managerowi**, że wykonał zamówienie

    ```scala
    case object ZamowienieWykonane
    ```

8. Po obsłużeniu ustalonej w komunikacie `InitKawiarnia(…)` liczby zamówień, **Manager** kończy działanie systemu za pomocą `context.system.terminate()`

### Definicje wszystkich komunikatów
```scala
case class InitKawiarnia(liczbaZamowien: Int, liczbaBaristow: Int)
case class InitBarista(/* TODO: wymyśl listę parametrów */)
case class InitKasa(/* TODO: wymyśl listę parametrów */)
case class InitMaszyna(/* TODO: wymyśl listę parametrów */)
case class Zamowienie(kwota: Int)
case class ZarejestrujOplate(kwota: Int)
case object PrzygotujKawe
case object ZrobKawe
case object BrakZdolnosci
case object NaprawMaszyne
case object Kawa
case object ZamowienieWykonane
```
