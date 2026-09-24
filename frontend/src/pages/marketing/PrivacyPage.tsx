export function PrivacyPage() {
  return (
    <div className="mx-auto max-w-3xl px-6 py-16">
      <h1 className="text-4xl font-extrabold tracking-tight text-stone-900 sm:text-5xl">Privacyverklaring</h1>
      <p className="mt-3 text-sm text-stone-500">Laatst bijgewerkt: 23 september 2026</p>

      <div className="mt-8 space-y-8 text-stone-600">
        <section>
          <h2 className="text-lg font-semibold text-stone-800">1. Wie is verantwoordelijk?</h2>
          <p className="mt-2">
            CasaCrew is software waarmee verhuurders ("beheerders") hun eigen huis of pand
            beheren. Elke beheerder registreert een eigen, volledig afgescheiden omgeving en
            voert daarin zelf gegevens in over kamers, studenten en schoonmaakpersoneel. Voor die
            gegevens is de beheerder zelf de verwerkingsverantwoordelijke (in de zin van de AVG);
            CasaCrew treedt op als verwerker. Voor de gegevens die je invult bij het aanmaken van
            je eigen beheerdersaccount (naam, e-mailadres) is CasaCrew de
            verwerkingsverantwoordelijke.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">2. Welke gegevens verwerkt CasaCrew?</h2>
          <p className="mt-2">Afhankelijk van hoe een huis wordt ingericht, kan dit gaan om:</p>
          <ul className="mt-3 list-disc space-y-1.5 pl-5">
            <li>Naam, e-mailadres, telefoonnummer en wachtwoord (versleuteld opgeslagen, nooit leesbaar)</li>
            <li>Adresgegevens van het pand en, indien ingevuld, van bewoners</li>
            <li>Kamerinformatie, huurbedragen, facturen en betaalstatus</li>
            <li>Geüploade documenten en huurcontracten</li>
            <li>Berichten via klachten, onderhoudsmeldingen en aankondigingen</li>
            <li>Noodcontactgegevens</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">3. Waarom verwerken we deze gegevens?</h2>
          <p className="mt-2">
            Uitsluitend om de dienst zelf mogelijk te maken: inloggen, kamers en huurders beheren,
            facturen opstellen en herinneringen versturen, schoonmaak plannen, en communicatie
            tussen beheerder, bewoners en schoonmaakteam faciliteren. We gebruiken gegevens niet
            voor advertenties en verkopen ze nooit door aan derden.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">4. Met wie delen we gegevens?</h2>
          <p className="mt-2">
            Gegevens worden alleen gedeeld met partijen die nodig zijn om de dienst te laten
            werken ("subverwerkers"):
          </p>
          <ul className="mt-3 list-disc space-y-1.5 pl-5">
            <li><strong>Render</strong>: hosting van de backend en de database</li>
            <li><strong>Netlify</strong>: hosting van deze website en het dashboard</li>
            <li>Een <strong>e-mailprovider</strong>: voor het versturen van herinneringen en meldingen</li>
            <li><strong>Twilio</strong>: alleen als een huis WhatsApp-meldingen heeft ingeschakeld</li>
            <li><strong>Google</strong>: alleen als je inlogt via "Inloggen met Google"</li>
          </ul>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">5. Beveiliging</h2>
          <p className="mt-2">
            Wachtwoorden worden gehasht (BCrypt) en zijn nooit terug te lezen, ook niet door ons.
            Alle verkeer tussen jouw browser en CasaCrew loopt via een beveiligde HTTPS-verbinding.
            Elke beheerdersomgeving is technisch afgescheiden van andere klanten: de gegevens van
            het ene huis zijn nooit zichtbaar voor een ander huis. Gevoelige bestanden zoals
            huurcontracten en documenten zijn alleen toegankelijk voor ingelogde, geautoriseerde
            gebruikers, niet via een openbare link.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">6. Cookies en tracking</h2>
          <p className="mt-2">
            Deze website en het dashboard gebruiken geen trackingcookies en geen
            analytics-/advertentiediensten van derden. Voor het inloggen wordt een sessietoken
            gebruikt dat lokaal in je browser wordt bewaard, niet via een cookie.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">7. Bewaartermijn</h2>
          <p className="mt-2">
            Gegevens blijven bewaard zolang een account of huis actief is. Een beheerder kan
            zelf gebruikers, kamers en documenten verwijderen. Wil je je hele account en de
            bijbehorende gegevens laten verwijderen, neem dan contact met ons op via{' '}
            <a href="mailto:info@casacrew.nl" className="text-emerald-700 hover:underline">
              info@casacrew.nl
            </a>.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">8. Jouw rechten</h2>
          <p className="mt-2">
            Je hebt het recht om je gegevens in te zien, te corrigeren of te laten verwijderen, en
            het recht om bezwaar te maken tegen verwerking. Neem hiervoor contact op via{' '}
            <a href="mailto:info@casacrew.nl" className="text-emerald-700 hover:underline">
              info@casacrew.nl
            </a>
            . Ben je het niet eens met hoe we met je gegevens omgaan, dan kun je een klacht
            indienen bij de Autoriteit Persoonsgegevens.
          </p>
        </section>

        <section>
          <h2 className="text-lg font-semibold text-stone-800">9. Contact</h2>
          <p className="mt-2">
            Vragen over deze privacyverklaring? Mail naar{' '}
            <a href="mailto:info@casacrew.nl" className="text-emerald-700 hover:underline">
              info@casacrew.nl
            </a>{' '}
            of gebruik het{' '}
            <a href="/contact" className="text-emerald-700 hover:underline">
              contactformulier
            </a>
            .
          </p>
        </section>
      </div>
    </div>
  );
}
