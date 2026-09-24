import { useEffect, useState, type ComponentType, type SVGProps } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { isPushSupported, isSubscribed, subscribeToWebPush } from '../lib/push';
import { assetUrl } from '../lib/apiClient';
import { Avatar } from '../components/Avatar';
import { LogoMark } from '../components/LogoMark';

interface NavItem {
  to: string;
  label: string;
  icon?: ComponentType<SVGProps<SVGSVGElement>>;
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
    <div className="border-b border-emerald-800 px-5 py-3">
      <button
        onClick={handleClick}
        disabled={isSubscribing}
        className="w-full rounded-lg bg-white/10 px-3 py-2 text-left text-xs font-medium text-emerald-50 hover:bg-white/15 disabled:opacity-50"
      >
        {isSubscribing ? 'Bezig...' : 'Pushmeldingen inschakelen'}
      </button>
      {error && <p className="mt-1 text-xs text-red-300">{error}</p>}
    </div>
  );
}

export function DashboardLayout({ title, navItems }: { title: string; navItems: NavItem[] }) {
  const { user, logout } = useAuth();

  return (
    <div className="flex min-h-screen">
      <aside className="flex w-60 shrink-0 flex-col bg-emerald-900">
        <div className="border-b border-emerald-800 px-5 py-5">
          <p className="flex items-center gap-2 text-lg font-bold text-white">
            <LogoMark className="h-7 w-7" />
            CasaCrew
          </p>
          <p className="text-xs text-emerald-300">{title}</p>
        </div>
        <PushOptIn />
        <nav className="flex-1 space-y-1 px-3 py-4">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  `flex items-center gap-2.5 rounded-lg px-3 py-2 text-sm font-medium ${
                    isActive ? 'bg-white/10 text-white' : 'text-emerald-100 hover:bg-white/5'
                  }`
                }
              >
                {Icon && <Icon className="h-4.5 w-4.5 shrink-0" />}
                {item.label}
              </NavLink>
            );
          })}
        </nav>
        <div className="flex items-center gap-3 border-t border-emerald-800 px-5 py-4">
          <Avatar name={user?.username ?? '?'} photoUrl={assetUrl(user?.profileImagePath)} size="sm" />
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium text-white">{user?.username}</p>
            <p className="truncate text-xs text-emerald-300">{user?.email}</p>
            <button onClick={logout} className="mt-0.5 text-xs font-medium text-red-300 hover:text-red-200 hover:underline">
              Uitloggen
            </button>
          </div>
        </div>
      </aside>
      <main className="flex-1 overflow-y-auto p-8">
        <Outlet />
      </main>
    </div>
  );
}
