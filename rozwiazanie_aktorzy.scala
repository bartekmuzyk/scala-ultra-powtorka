import org.apache.pekko
import pekko.actor.{ActorSystem, Actor, ActorLogging, ActorRef, Props}

case class InitKawiarnia(liczbaZamowien: Int, liczbaBaristow: Int)
case class InitBarista(manager: ActorRef, maszyna: ActorRef)
case class InitKasa()
case class InitMaszyna(zdolnosc: Int)
case class Zamowienie(kwota: Int)
case class ZarejestrujOplate(kwota: Int)
case object PotwierdzeniePlatnosci
case object PrzygotujKawe
case object ZrobKawe
case object BrakZdolnosci
case object NaprawMaszyne
case object Kawa
case object ZamowienieWykonane

class Manager extends Actor with ActorLogging {
  def receive: Receive = {
    case InitKawiarnia(liczbaZamowien, liczbaBaristow) => {
      val kasa = context.system.actorOf(Props[Kasa](), "kasa")
      kasa ! InitKasa

      val maszyna = context.system.actorOf(Props[Maszyna](), "maszyna")
      maszyna ! InitMaszyna(2)

      val barisci = (1 to liczbaBaristow).map(num => {
        val barista = context.system.actorOf(Props[Barista](), s"barista$num")
        barista ! InitBarista(self, maszyna)

        barista
      }).toList

      context.become(gotowyNaZamowienia(barisci, liczbaZamowien, liczbaZamowien, 0, kasa))
    }
  }

  def gotowyNaZamowienia(dostepniBarisci: List[ActorRef], ileJeszczeZamowienPrzyjac: Int, naIleZamowienCzekam: Int,
                         nadmiaroweZamowienia: Int, kasa: ActorRef): Receive = {
    case Zamowienie(kwota) => {
      log.info(s"Zamowienie($kwota)")

      if (ileJeszczeZamowienPrzyjac == 0) {
        log.info("Nie przyjmujemy więcej zamówień!")
      } else {
        kasa ! ZarejestrujOplate(kwota)

        if (dostepniBarisci.nonEmpty) {
          val pierwszyLepszy = dostepniBarisci.head

          log.info(s"PrzygotujKawe -> ${pierwszyLepszy.path}")
          pierwszyLepszy ! PrzygotujKawe

          context.become(gotowyNaZamowienia(
            dostepniBarisci.tail, ileJeszczeZamowienPrzyjac - 1, naIleZamowienCzekam, nadmiaroweZamowienia, kasa
          ))
        } else {
          log.info(s"Nadmiarowe: ${nadmiaroweZamowienia + 1}")
          context.become(gotowyNaZamowienia(
            dostepniBarisci, ileJeszczeZamowienPrzyjac - 1, naIleZamowienCzekam, nadmiaroweZamowienia + 1, kasa
          ))
        }
      }
    }
    case ZamowienieWykonane => {
      log.info(s"ZamowienieWykonane <- ${sender().path} | czekam na ${naIleZamowienCzekam - 1} zamówień")

      if (naIleZamowienCzekam == 1) {
        // Ten warunek jest spełniony jeśli dostaliśmy właśnie ostatnie zamówienie, na którego wykonanie czekaliśmy
        context.system.terminate()
      } else {
        val wolnyBarista = sender()

        if (nadmiaroweZamowienia > 0) {
          log.info(s"PrzygotujKawe (nadmiar) -> ${wolnyBarista.path}")
          wolnyBarista ! PrzygotujKawe

          context.become(gotowyNaZamowienia(
            dostepniBarisci,
            ileJeszczeZamowienPrzyjac,
            naIleZamowienCzekam - 1,
            nadmiaroweZamowienia - 1,
            kasa
          ))
        } else {
          context.become(gotowyNaZamowienia(
            wolnyBarista :: dostepniBarisci,
            ileJeszczeZamowienPrzyjac,
            naIleZamowienCzekam - 1,
            nadmiaroweZamowienia,
            kasa
          ))
        }
      }
    }
  }
}

class Barista extends Actor with ActorLogging {
  def receive: Receive = {
    case InitBarista(manager, maszyna) => {
      context.become(gotowyDoPrzygotowywaniaKawy(manager, maszyna))
    }
  }

  def gotowyDoPrzygotowywaniaKawy(manager: ActorRef, maszyna: ActorRef): Receive = {
    case PrzygotujKawe => {
//      log.info("Próbuję przygotować kawę")
      maszyna ! ZrobKawe
    }
    case BrakZdolnosci => {
//      log.info("Naprawiam maszynę i próbuję jeszcze raz")
      maszyna ! NaprawMaszyne
      maszyna ! ZrobKawe
    }
    case Kawa => {
      log.info("Kawa zrobiona!")
      manager ! ZamowienieWykonane
    }
  }
}

class Kasa extends Actor with ActorLogging {
  def receive: Receive = {
    case InitKasa => {
      context.become(gotowyDoLiczenia(0))
    }
  }

  def gotowyDoLiczenia(suma: Int): Receive = {
    case ZarejestrujOplate(kwota) => {
      context.become(gotowyDoLiczenia(suma + kwota))
    }
  }
}

class Maszyna extends Actor with ActorLogging {
  def receive: Receive = {
    case InitMaszyna(zdolnosc) => {
      context.become(gotowyDoRobieniaKawy(zdolnosc, zdolnosc))
    }
  }

  def gotowyDoRobieniaKawy(zdolnosc: Int, zdolnoscStartowa: Int): Receive = {
    case ZrobKawe => {
      if (zdolnosc == 0) {
        sender() ! BrakZdolnosci
      } else {
        sender() ! Kawa
        context.become(gotowyDoRobieniaKawy(zdolnosc - 1, zdolnoscStartowa))
      }
    }
    case NaprawMaszyne => {
      context.become(gotowyDoRobieniaKawy(zdolnoscStartowa, zdolnoscStartowa))
    }
  }
}

@main
def moje(): Unit = {
  val system = ActorSystem("kawiarnia")
  val manager = system.actorOf(Props[Manager](), "manager")
  manager ! InitKawiarnia(6, 4)
  manager ! Zamowienie(1)
  manager ! Zamowienie(2)
  manager ! Zamowienie(3)
  manager ! Zamowienie(4)

  // nadmiarowe (trzeba poczekać aż się zwolnią bariści)
  manager ! Zamowienie(5)
  manager ! Zamowienie(6)

  // nadmiarowe (manager nie przyjmuje już tylu zamówień)
  manager ! Zamowienie(7)
  manager ! Zamowienie(8)
  manager ! Zamowienie(9)
}
