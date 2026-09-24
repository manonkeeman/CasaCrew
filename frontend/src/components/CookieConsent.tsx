import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

const STORAGE_KEY = 'casacrew_cookie_consent';

export type CookieConsentValue = 'accepted' | 'declined';

export function getCookieConsent(): CookieConsentValue | null {
  try {
    const value = localStorage.getItem(STORAGE_KEY);
    return value === 'accepted' || value === 'declined' ? value : null;
  } catch {
    return null;
  }
}

export function CookieConsent() {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    setVisible(getCookieConsent() === null);
  }, []);

  function respond(value: CookieConsentValue) {
    try {
      localStorage.setItem(STORAGE_KEY, value);
    } catch {
      // Lokale opslag niet beschikbaar (privémodus e.d.) -- toon de banner
      // dan gewoon opnieuw bij een volgend bezoek, geen harde fout nodig.
    }
    setVisible(false);
  }

  if (!visible) return null;

  return (
    <div className="fixed inset-x-0 bottom-0 z-50 border-t border-stone-200 bg-white px-4 py-4 shadow-[0_-4px_12px_rgba(0,0,0,0.06)] sm:px-6">
      <div className="mx-auto flex max-w-5xl flex-col items-start gap-3 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-sm text-stone-600">
          CasaCrew gebruikt geen trackingcookies of advertentiediensten. Voor inloggen gebruiken we
          een sessietoken, geen cookie. Meer weten? Lees onze{' '}
          <Link to="/privacy" className="text-emerald-700 hover:underline">
            privacyverklaring
          </Link>
          .
        </p>
        <div className="flex shrink-0 gap-2">
          <button
            onClick={() => respond('declined')}
            className="rounded-xl border border-stone-300 px-4 py-2 text-sm font-medium text-stone-700 hover:bg-stone-50"
          >
            Weigeren
          </button>
          <button
            onClick={() => respond('accepted')}
            className="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-medium text-white hover:bg-emerald-700"
          >
            Accepteren
          </button>
        </div>
      </div>
    </div>
  );
}
