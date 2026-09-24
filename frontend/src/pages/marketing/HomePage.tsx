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
import { GradientBlobs } from '../../components/decor/GradientBlobs';

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
    <div>
      <div className="relative isolate overflow-hidden">
        <GradientBlobs />
        <div className="mx-auto max-w-6xl px-6 pb-16 pt-20 text-center">
          <h1 className="mx-auto max-w-3xl text-4xl font-bold tracking-tight text-stone-900 sm:text-5xl">
            Verhuur je kamers zonder chaos
          </h1>
          <p className="mx-auto mt-5 max-w-2xl text-lg text-stone-600">
            CasaCrew geeft elke verhuurder een eigen dashboard voor kamers, huurders, schoonmaak,
            facturen en communicatie, overzichtelijk op één plek.
          </p>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
            <Link
              to="/register"
              className="rounded-xl bg-emerald-600 px-6 py-3 text-sm font-semibold text-white shadow-sm shadow-emerald-900/10 hover:bg-emerald-700"
            >
              Start gratis
            </Link>
            <Link
              to="/functies"
              className="rounded-xl border border-stone-300 bg-white px-6 py-3 text-sm font-semibold text-stone-700 hover:bg-stone-50"
            >
              Bekijk functies
            </Link>
          </div>

          <div className="mx-auto mt-14 max-w-4xl">
            <BrowserFrame
              src="/images/dashboard-preview.png"
              alt="CasaCrew beheerdersdashboard met studenten, kamers en openstaande facturen"
              url="casacrew.nl/admin/dashboard"
            />
          </div>
        </div>
      </div>

      <section className="border-y border-stone-200 bg-white py-16">
        <div className="mx-auto max-w-6xl px-6">
          <h2 className="text-center text-2xl font-bold text-stone-900">Alles wat je nodig hebt, in één huis</h2>
          <div className="mt-10 grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {FEATURES.map((feature) => (
              <div key={feature.title} className="rounded-2xl border border-stone-200 p-6 shadow-sm shadow-stone-200/50">
                <span className="flex h-10 w-10 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                  <feature.icon className="h-5 w-5" />
                </span>
                <h3 className="mt-4 font-semibold text-stone-800">{feature.title}</h3>
                <p className="mt-1.5 text-sm text-stone-500">{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      <section className="mx-auto max-w-6xl px-6 py-16">
        <div className="grid grid-cols-1 items-center gap-10 md:grid-cols-2">
          <div>
            <h2 className="text-2xl font-bold text-stone-900">
              Studenten en kamers in één overzicht
            </h2>
            <p className="mt-3 text-stone-600">
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

      <section className="border-y border-stone-200 bg-white py-16">
        <div className="mx-auto max-w-6xl px-6">
          <div className="grid grid-cols-1 items-center gap-10 md:grid-cols-2">
            <BrowserFrame
              src="/images/wizard-preview.png"
              alt="Setup-wizard voor het inrichten van een nieuw huis"
              url="casacrew.nl/admin/setup"
            />
            <div>
              <h2 className="text-2xl font-bold text-stone-900">Zo werkt het</h2>
              <div className="mt-6 space-y-6">
                {STEPS.map((item) => (
                  <div key={item.step} className="flex gap-4">
                    <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-full bg-emerald-600 text-sm font-bold text-white">
                      {item.step}
                    </span>
                    <div>
                      <h3 className="font-semibold text-stone-800">{item.title}</h3>
                      <p className="mt-1 text-sm text-stone-500">{item.description}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="bg-emerald-700">
        <div className="mx-auto max-w-4xl px-6 py-16 text-center">
          <h2 className="text-2xl font-bold text-white sm:text-3xl">Klaar om te beginnen?</h2>
          <p className="mt-3 text-emerald-50">
            Richt je huis vandaag nog in, of neem contact op als je eerst vragen hebt.
          </p>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-4">
            <Link
              to="/register"
              className="rounded-xl bg-white px-6 py-3 text-sm font-semibold text-emerald-700 hover:bg-emerald-50"
            >
              Start gratis
            </Link>
            <Link
              to="/contact"
              className="rounded-xl border border-emerald-300 px-6 py-3 text-sm font-semibold text-white hover:bg-emerald-600"
            >
              Neem contact op
            </Link>
          </div>
        </div>
      </section>
    </div>
  );
}
