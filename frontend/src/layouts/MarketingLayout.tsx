import { Link, NavLink, Outlet } from 'react-router-dom';

const NAV_LINKS = [
  { to: '/', label: 'Home' },
  { to: '/functies', label: 'Functies & Prijzen' },
  { to: '/over-ons', label: 'Over ons' },
  { to: '/contact', label: 'Contact' },
];

export function MarketingLayout() {
  return (
    <div className="flex min-h-screen flex-col bg-sand">
      <header className="border-b border-stone-200 bg-white/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-6 py-4">
          <Link to="/" className="text-xl font-bold text-emerald-700">
            CasaCrew
          </Link>
          <nav className="hidden items-center gap-6 md:flex">
            {NAV_LINKS.map((link) => (
              <NavLink
                key={link.to}
                to={link.to}
                end={link.to === '/'}
                className={({ isActive }) =>
                  `text-sm font-medium ${isActive ? 'text-emerald-700' : 'text-stone-600 hover:text-emerald-700'}`
                }
              >
                {link.label}
              </NavLink>
            ))}
          </nav>
          <div className="flex items-center gap-3">
            <Link
              to="/login"
              className="text-sm font-medium text-stone-600 hover:text-emerald-700"
            >
              Inloggen
            </Link>
            <Link
              to="/register"
              className="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white shadow-sm shadow-emerald-900/10 hover:bg-emerald-700"
            >
              Start gratis
            </Link>
          </div>
        </div>
        <nav className="flex items-center gap-4 overflow-x-auto border-t border-stone-100 px-6 py-2 md:hidden">
          {NAV_LINKS.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === '/'}
              className={({ isActive }) =>
                `whitespace-nowrap text-sm font-medium ${isActive ? 'text-emerald-700' : 'text-stone-600'}`
              }
            >
              {link.label}
            </NavLink>
          ))}
        </nav>
      </header>

      <main className="flex-1">
        <Outlet />
      </main>

      <footer className="border-t border-stone-200 bg-white">
        <div className="mx-auto max-w-6xl px-6 py-10">
          <div className="flex flex-col gap-6 md:flex-row md:items-start md:justify-between">
            <div>
              <p className="text-lg font-bold text-emerald-700">CasaCrew</p>
              <p className="mt-1 max-w-xs text-sm text-stone-500">
                Slim beheer voor verhuurpanden: kamers, huurders, schoonmaak en facturatie in één dashboard.
              </p>
            </div>
            <div className="flex flex-wrap gap-x-10 gap-y-4">
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-stone-400">Product</p>
                <ul className="mt-2 space-y-1.5 text-sm text-stone-600">
                  <li><Link to="/functies" className="hover:text-emerald-700">Functies & Prijzen</Link></li>
                  <li><Link to="/register" className="hover:text-emerald-700">Start gratis</Link></li>
                  <li><Link to="/login" className="hover:text-emerald-700">Inloggen</Link></li>
                </ul>
              </div>
              <div>
                <p className="text-xs font-semibold uppercase tracking-wide text-stone-400">Bedrijf</p>
                <ul className="mt-2 space-y-1.5 text-sm text-stone-600">
                  <li><Link to="/over-ons" className="hover:text-emerald-700">Over ons</Link></li>
                  <li><Link to="/contact" className="hover:text-emerald-700">Contact</Link></li>
                  <li><Link to="/privacy" className="hover:text-emerald-700">Privacy</Link></li>
                </ul>
              </div>
            </div>
          </div>
          <p className="mt-8 text-xs text-stone-400">
            &copy; {new Date().getFullYear()} CasaCrew. Alle rechten voorbehouden. ·{' '}
            <Link to="/privacy" className="hover:text-emerald-700">Privacyverklaring</Link>
          </p>
        </div>
      </footer>
    </div>
  );
}
