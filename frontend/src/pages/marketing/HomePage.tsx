import { Link } from 'react-router-dom';
import {
  BanknotesIcon,
  BookOpenIcon,
  BuildingIcon,
  SparklesIcon,
  UsersIcon,
  WrenchIcon,
} from '../../components/icons';
import { BrowserFrame } from '../../components/BrowserFrame';

const FEATURES = [
  {
    icon: BuildingIcon,
    title: 'Kamerbeheer',
    description: 'Houd in één oogopslag bij welke kamers vrij zijn en wie waar woont.',
  },
  {
    icon: UsersIcon,
    title: 'Studenten & toegang',
    description: 'Nodig huurders uit met hun eigen inlog, gekoppeld aan hun kamer.',
  },
  {
    icon: SparklesIcon,
    title: 'Schoonmaakrooster',
    description: 'Automatisch rooster en een apart account voor de schoonmaakploeg.',
  },
  {
    icon: BanknotesIcon,
    title: 'Facturatie',
    description: 'Maandelijkse huur wordt automatisch gefactureerd en herinnerd.',
  },
  {
    icon: WrenchIcon,
    title: 'Onderhoud & klachten',
    description: 'Meldingen van studenten komen direct en overzichtelijk bij je binnen.',
  },
  {
    icon: BookOpenIcon,
    title: 'Huisregels & documenten',
    description: 'Eén centrale plek voor huisregels, contracten en andere documenten.',
  },
];

const STEPS = [
  {
    step: '1',
    title: 'Registreer je huis',
    description: 'Maak in een paar minuten een eigen account aan voor jouw pand.',
  },
  {
    step: '2',
    title: 'Doorloop de setup-wizard',
    description: 'Voeg kamers, huurbedrag, betaalgegevens en huisregels toe.',
  },
  {
    step: '3',
    title: 'Nodig studenten en schoonmaak uit',
    description: 'Geef iedereen die erbij hoort direct zijn eigen toegang.',
  },
];

export function HomePage() {
  return (
    <div className="bg-white">
      <div className="mx-auto max-w-5xl px-6 pb-20 pt-24 text-center sm:pt-32">
        <h1 className="mx-auto max-w-3xl text-6xl font-extrabold tracking-tight text-stone-900 sm:text-7xl">
          Verhuur je kamers zonder chaos
        </h1>
        <p className="mx-auto mt-6 max-w-xl text-xl text-stone-500">
          CasaCrew geeft elke verhuurder een eigen dashboard voor kamers, huurders, schoonmaak,
          facturen en communicatie, overzichtelijk op één plek.
        </p>
        <div className="mt-9 flex flex-wrap items-center justify-center gap-4">
          <Link
            to="/register"
            className="rounded-full bg-emerald-600 px-7 py-3 text-sm font-semibold text-white transition-all duration-200 hover:scale-[1.03] hover:bg-emerald-700 active:scale-95"
          >
            Start gratis
          </Link>
          <Link
            to="/functies"
            className="rounded-full border border-stone-200 px-7 py-3 text-sm font-semibold text-stone-700 transition-all duration-200 hover:scale-[1.03] hover:bg-stone-50 active:scale-95"
          >
            Bekijk functies
          </Link>
        </div>

        <div className="mx-auto mt-20 max-w-4xl">
          <BrowserFrame
            src="/images/dashboard-preview.png"
            alt="CasaCrew beheerdersdashboard met studenten, kamers en openstaande facturen"
            url="casacrew.nl/admin/dashboard"
          />
        </div>
      </div>

      <section className="bg-stone-50 py-24">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="text-center text-4xl font-extrabold tracking-tight text-stone-900">
            Alles wat je nodig hebt, in één huis
          </h2>
          <div className="mt-14 grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map((feature) => (
              <div key={feature.title} className="rounded-3xl bg-white p-8">
                <feature.icon className="h-7 w-7 text-emerald-600" />
                <h3 className="mt-5 text-lg font-semibold text-stone-900">{feature.title}</h3>
                <p className="mt-2 text-stone-500">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-6 py-24">
        <div className="grid grid-cols-1 items-center gap-16 md:grid-cols-2">
          <div>
            <h2 className="text-3xl font-extrabold tracking-tight text-stone-900 sm:text-4xl">
              Studenten en kamers in één overzicht
            </h2>
            <p className="mt-4 text-lg text-stone-500">
              Zie in één oogopslag wie waar woont, welke huur nog openstaat en waar contracten
              bijna aflopen, zonder losse spreadsheets bij te houden.
            </p>
          </div>
          <BrowserFrame
            src="/images/students-preview.png"
            alt="Overzicht van studenten met kamer, huurbedrag en contract"
            url="casacrew.nl/admin/students"
          />
        </div>
      </section>

      <section className="bg-stone-50 py-24">
        <div className="mx-auto max-w-6xl px-6">
          <div className="grid grid-cols-1 items-center gap-16 md:grid-cols-2">
            <BrowserFrame
              src="/images/wizard-preview.png"
              alt="Setup-wizard voor het inrichten van een nieuw huis"
              url="casacrew.nl/admin/setup"
              className="md:order-first"
            />
            <div>
              <h2 className="text-3xl font-extrabold tracking-tight text-stone-900 sm:text-4xl">Zo werkt het</h2>
              <div className="mt-8 space-y-8">
                {STEPS.map((item) => (
                  <div key={item.step} className="flex gap-5">
                    <span className="shrink-0 text-2xl font-extrabold text-emerald-600">{item.step}</span>
                    <div>
                      <h3 className="text-lg font-semibold text-stone-900">{item.title}</h3>
                      <p className="mt-1 text-stone-500">{item.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="bg-stone-950">
        <div className="mx-auto max-w-4xl px-6 py-24 text-center">
          <h2 className="text-4xl font-extrabold tracking-tight text-white">Klaar om te beginnen?</h2>
          <p className="mt-4 text-lg text-stone-400">
            Richt je huis vandaag nog in, of neem contact op als je eerst vragen hebt.
          </p>
          <div className="mt-9 flex flex-wrap items-center justify-center gap-4">
            <Link
              to="/register"
              className="rounded-full bg-white px-7 py-3 text-sm font-semibold text-stone-950 transition-all duration-200 hover:scale-[1.03] hover:bg-stone-100 active:scale-95"
            >
              Start gratis
            </Link>
            <Link
              to="/contact"
              className="rounded-full border border-white/20 px-7 py-3 text-sm font-semibold text-white transition-all duration-200 hover:scale-[1.03] hover:bg-white/10 active:scale-95"
            >
              Neem contact op
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
