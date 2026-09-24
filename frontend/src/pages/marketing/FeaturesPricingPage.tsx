import { Link } from 'react-router-dom';
import { BanknotesIcon, CheckIcon, MegaphoneIcon, SparklesIcon, UsersIcon } from '../../components/icons';
import { GradientBlobs } from '../../components/decor/GradientBlobs';
import { ChecklistIllustration } from '../../components/illustrations/ChecklistIllustration';

const FEATURE_GROUPS = [
  {
    title: 'Studenten & kamers',
    icon: UsersIcon,
    items: [
      'Kamerbeheer met bewoners en beschikbaarheid',
      'Eigen inlog per student, gekoppeld aan hun kamer',
      'Contractbeheer met einddatum en herinnering',
      'Overzicht van huisgenoten voor studenten onderling',
    ],
  },
  {
    title: 'Schoonmaak',
    icon: SparklesIcon,
    items: [
      'Automatisch roulerend schoonmaakrooster',
      'Eigen account en dashboard voor de schoonmaakploeg',
      'In- en uitchecken per shift',
      'Meldingen van voorraad en incidenten',
    ],
  },
  {
    title: 'Financieel',
    icon: BanknotesIcon,
    items: [
      'Automatische maandelijkse huurfacturatie',
      'Betaalherinneringen via e-mail en WhatsApp',
      'Overzicht van betalingen en openstaande facturen',
      'Uitgaventracking met export',
    ],
  },
  {
    title: 'Communicatie',
    icon: MegaphoneIcon,
    items: [
      'Aankondigingen naar alle studenten',
      'Klachten tussen student en beheerder, in beide richtingen',
      'Onderhoudsmeldingen los van klachten',
      'Gedeelde agenda voor huisafspraken',
    ],
  },
];

export function FeaturesPricingPage() {
  return (
    <div>
      <div className="relative isolate overflow-hidden">
        <GradientBlobs />
        <div className="mx-auto grid max-w-6xl grid-cols-1 items-center gap-8 px-6 py-16 md:grid-cols-5">
          <div className="text-center md:col-span-3 md:text-left">
            <h1 className="text-3xl font-bold text-stone-900 sm:text-4xl">Functies & Prijzen</h1>
            <p className="mx-auto mt-4 max-w-2xl text-stone-600 md:mx-0">
              Alles wat je als verhuurder nodig hebt om je pand te beheren, gebundeld in één dashboard.
            </p>
          </div>
          <div className="hidden justify-center md:col-span-2 md:flex">
            <ChecklistIllustration className="h-64 w-full max-w-xs" />
          </div>
        </div>
      </div>

      <div className="mx-auto max-w-6xl px-6 pb-16">
        <div className="mt-14 grid grid-cols-1 gap-8 md:grid-cols-2">
          {FEATURE_GROUPS.map((group) => (
            <div key={group.title} className="rounded-2xl border border-stone-200 bg-white p-6 shadow-sm shadow-stone-200/50">
              <span className="flex h-12 w-12 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                <group.icon className="h-6 w-6" />
              </span>
              <h2 className="mt-4 font-semibold text-stone-800">{group.title}</h2>
              <ul className="mt-4 space-y-2.5">
                {group.items.map((item) => (
                  <li key={item} className="flex items-start gap-2.5 text-sm text-stone-600">
                    <CheckIcon className="mt-0.5 h-4 w-4 shrink-0 text-emerald-600" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mx-auto mt-16 max-w-md rounded-2xl border border-emerald-200 bg-emerald-50 p-8 text-center">
          <h2 className="text-lg font-semibold text-stone-800">Prijs</h2>
          <p className="mt-2 text-3xl font-bold text-emerald-700">Op aanvraag</p>
          <p className="mt-2 text-sm text-stone-600">
            Elk pand is anders. Neem contact op voor een voorstel dat past bij het aantal kamers en
            bewoners van jouw huis.
          </p>
          <Link
            to="/contact"
            className="mt-6 inline-block rounded-xl bg-emerald-600 px-6 py-3 text-sm font-semibold text-white shadow-sm shadow-emerald-900/10 hover:bg-emerald-700"
          >
            Vraag een demo aan
          </Link>
        </div>
      </div>
    </div>
  );
}
