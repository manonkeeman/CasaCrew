import { useEffect, useState } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { isPushSupported, isSubscribed, subscribeToWebPush } from '../lib/push';

interface NavItem {
  to: string;
  label: string;
}

function PushOptIn() {
  const [supported] = useState(isPushSupported());
  const [subscribed, setSubscribed] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSubscribing, setIsSubscribing] = useState(false);

  useEffect(() => {
    if (supported) {
      isSubscribed().then(setSubscribed);
    }
  }, [supported]);

  if (!supported || subscribed) return null;

  async function handleClick() {
    setError(null);
    setIsSubscribing(true);
    try {
      await subscribeToWebPush();
      setSubscribed(true);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Inschakelen mislukt.');
    } finally {
      setIsSubscribing(false);
    }
  }

  return (
    <div className="border-b border-slate-200 px-5 py-3">
      <button
        onClick={handleClick}
        disabled={isSubscribing}
        className="w-full rounded-lg bg-emerald-50 px-3 py-2 text-left text-xs font-medium text-emerald-700 hover:bg-emerald-100 disabled:opacity-50"
      >
        {isSubscribing ? 'Bezig...' : 'Pushmeldingen inschakelen'}
      </button>
      {error && <p className="mt-1 text-xs text-red-600">{error}</p>}
    </div>
  );
}

export function DashboardLayout({ title, navItems }: { title: string; navItems: NavItem[] }) {
  const { user, logout } = useAuth();

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 shrink-0 flex-col border-r border-slate-200 bg-white">
        <div className="border-b border-slate-200 px-5 py-5">
          <p className="text-lg font-bold text-emerald-700">CasaCrew</p>
          <p className="text-xs text-slate-500">{title}</p>
        </div>
        <PushOptIn />
        <nav className="flex-1 space-y-1 px-3 py-4">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `block rounded-lg px-3 py-2 text-sm font-medium ${
                  isActive ? 'bg-emerald-50 text-emerald-700' : 'text-slate-600 hover:bg-slate-50'
                }`
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="border-t border-slate-200 px-5 py-4">
          <p className="truncate text-sm font-medium text-slate-700">{user?.username}</p>
          <p className="truncate text-xs text-slate-400">{user?.email}</p>
          <button onClick={logout} className="mt-2 text-xs font-medium text-red-600 hover:underline">
            Uitloggen
          </button>
        </div>
      </aside>
      <main className="flex-1 overflow-y-auto p-8">
        <Outlet />
      </main>
    </div>
  );
}
