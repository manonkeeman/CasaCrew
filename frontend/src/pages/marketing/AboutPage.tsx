import { Link } from 'react-router-dom';
import { TeamIllustration } from '../../components/illustrations/TeamIllustration';

export function AboutPage() {
  return (
    <div className="bg-white">
      <div className="mx-auto grid max-w-5xl grid-cols-1 items-center gap-10 px-6 py-24 md:grid-cols-5">
        <div className="md:col-span-3">
          <h1 className="text-5xl font-extrabold tracking-tight text-stone-900">Over CasaCrew</h1>
          <p className="mt-6 text-xl text-stone-500">
            CasaCrew is software voor mensen die kamers verhuren, of het nu om een enkel
            studentenhuis gaat of meerdere panden. In plaats van losse spreadsheets, WhatsApp-
            groepen en briefjes op de koelkast, geeft CasaCrew elke verhuurder één plek voor
            kamers, huurders, schoonmaak, facturen en communicatie.
          </p>
        </div>
        <div className="hidden justify-center md:col-span-2 md:flex">
          <TeamIllustration className="h-64 w-full max-w-sm" />
        </div>
      </div>

      <div className="bg-stone-50 py-24">
        <div className="mx-auto max-w-3xl px-6">
          <h2 className="text-2xl font-bold text-stone-900">Voor wie is dit?</h2>
          <p className="mt-3 text-lg text-stone-500">
            CasaCrew is gebouwd voor verhuurders die kamers beheren met wisselende bewoners: denk aan
            studentenhuizen, kamerverhuur en soortgelijke woonvormen. Elke verhuurder registreert zijn
            eigen, volledig gescheiden omgeving. Jouw kamers, huurders en gegevens zijn nooit
            zichtbaar voor een andere verhuurder op het platform.
          </p>

          <h2 className="mt-12 text-2xl font-bold text-stone-900">Hoe we werken</h2>
          <p className="mt-3 text-lg text-stone-500">
            Na registratie doorloop je een korte setup-wizard: huisprofiel, kamers, huurinstellingen,
            betaalgegevens, huisregels, en het toevoegen van studenten en een schoonmaakaccount. Daarna
            heeft iedereen (beheerder, student en schoonmaakploeg) zijn eigen dashboard met precies
            de informatie en taken die voor hen relevant zijn.
          </p>

          <div className="mt-16 rounded-3xl bg-white p-10 text-center">
            <h2 className="text-2xl font-bold text-stone-900">Nieuwsgierig of CasaCrew bij jouw huis past?</h2>
            <div className="mt-6 flex flex-wrap items-center justify-center gap-4">
              <Link
                to="/register"
                className="rounded-full bg-emerald-600 px-7 py-3 text-sm font-semibold text-white transition-all duration-200 hover:scale-[1.03] hover:bg-emerald-700 active:scale-95"
              >
                Start gratis
              </Link>
              <Link
                to="/contact"
                className="rounded-full border border-stone-200 px-7 py-3 text-sm font-semibold text-stone-700 transition-all duration-200 hover:scale-[1.03] hover:bg-stone-50 active:scale-95"
              >
                Neem contact op
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
