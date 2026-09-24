import { Link } from 'react-router-dom';
import { GradientBlobs } from '../../components/decor/GradientBlobs';
import { TeamIllustration } from '../../components/illustrations/TeamIllustration';

export function AboutPage() {
  return (
    <div>
      <div className="relative isolate overflow-hidden">
        <GradientBlobs />
        <div className="mx-auto grid max-w-5xl grid-cols-1 items-center gap-10 px-6 py-16 md:grid-cols-5">
          <div className="md:col-span-3">
            <h1 className="text-3xl font-bold text-stone-900 sm:text-4xl">Over CasaCrew</h1>
            <p className="mt-6 text-lg text-stone-600">
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
      </div>

      <div className="mx-auto max-w-3xl px-6 pb-16">
      <h2 className="mt-10 text-xl font-semibold text-stone-800">Voor wie is dit?</h2>
      <p className="mt-3 text-stone-600">
        CasaCrew is gebouwd voor verhuurders die kamers beheren met wisselende bewoners: denk aan
        studentenhuizen, kamerverhuur en soortgelijke woonvormen. Elke verhuurder registreert zijn
        eigen, volledig gescheiden omgeving. Jouw kamers, huurders en gegevens zijn nooit
        zichtbaar voor een andere verhuurder op het platform.
      </p>

      <h2 className="mt-10 text-xl font-semibold text-stone-800">Hoe we werken</h2>
      <p className="mt-3 text-stone-600">
        Na registratie doorloop je een korte setup-wizard: huisprofiel, kamers, huurinstellingen,
        betaalgegevens, huisregels, en het toevoegen van studenten en een schoonmaakaccount. Daarna
        heeft iedereen (beheerder, student en schoonmaakploeg) zijn eigen dashboard met precies
        de informatie en taken die voor hen relevant zijn.
      </p>

      <div className="mt-12 rounded-2xl border border-stone-200 bg-white p-8 text-center shadow-sm shadow-stone-200/50">
        <h2 className="text-lg font-semibold text-stone-800">Nieuwsgierig of CasaCrew bij jouw huis past?</h2>
        <div className="mt-5 flex flex-wrap items-center justify-center gap-4">
          <Link
            to="/register"
            className="rounded-xl bg-emerald-600 px-6 py-3 text-sm font-semibold text-white shadow-sm shadow-emerald-900/10 hover:bg-emerald-700"
          >
            Start gratis
          </Link>
          <Link
            to="/contact"
            className="rounded-xl border border-stone-300 px-6 py-3 text-sm font-semibold text-stone-700 hover:bg-stone-50"
          >
            Neem contact op
          </Link>
        </div>
      </div>
      </div>
    </div>
  );
}
