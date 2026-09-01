import { useQuery } from '@tanstack/react-query';
import { api, assetUrl } from '../../lib/apiClient';
import type { Housemate } from '../../lib/types';
import { Card } from '../../components/ui';
import { Avatar } from '../../components/Avatar';

const AVAILABILITY_LABEL: Record<string, string> = {
  TENTAMENPERIODE: 'Tentamenperiode',
  DRUK: 'Druk',
  OPEN_VOOR_CHILLEN: 'Open voor chillen',
};

const AVAILABILITY_STYLE: Record<string, string> = {
  TENTAMENPERIODE: 'bg-red-50 text-red-700',
  DRUK: 'bg-amber-50 text-amber-700',
  OPEN_VOOR_CHILLEN: 'bg-emerald-50 text-emerald-700',
};

const SOCIAL_LABEL: Record<string, string> = {
  OPEN_FOR_CONTACT: 'Open voor contact',
  LEUK: 'Gezellig erbij',
  AF_EN_TOE: 'Af en toe',
  LIEVER_OP_MEZELF: 'Liever op mezelf',
};

export function HousematesPage() {
  const query = useQuery({
    queryKey: ['housemates'],
    queryFn: () => api.get<Housemate[]>('/api/users/housemates'),
  });

  const housemates = query.data ?? [];

  return (
    <div>
      <h1 className="mb-2 text-2xl font-bold text-stone-800">Huisgenoten</h1>
      <p className="mb-6 text-sm text-stone-500">Wie woont er nog meer in huis.</p>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {housemates.map((h) => (
          <Card key={h.id}>
            <div className="flex items-center gap-3">
              <Avatar name={h.fullName ?? h.username} photoUrl={assetUrl(h.profileImagePath)} size="lg" />
              <div className="min-w-0">
                <h3 className="truncate font-semibold text-stone-800">{h.fullName ?? h.username}</h3>
                <p className="text-sm text-stone-500">{h.roomName ?? 'Geen kamer'}</p>
              </div>
            </div>
            <div className="mt-3 space-y-1 text-sm text-stone-600">
              {h.studyOrWork && <p>📚 {h.studyOrWork}</p>}
              {h.favoriteMeal && <p>🍽️ {h.favoriteMeal}</p>}
              {h.socialPreference && <p>{SOCIAL_LABEL[h.socialPreference] ?? h.socialPreference}</p>}
            </div>
            {h.availabilityStatus && (
              <span
                className={`mt-3 inline-block rounded-full px-2 py-0.5 text-xs font-medium ${
                  AVAILABILITY_STYLE[h.availabilityStatus] ?? 'bg-stone-100 text-stone-600'
                }`}
              >
                {AVAILABILITY_LABEL[h.availabilityStatus] ?? h.availabilityStatus}
              </span>
            )}
          </Card>
        ))}
        {housemates.length === 0 && <p className="text-sm text-stone-400">Nog geen andere huisgenoten.</p>}
      </div>
    </div>
  );
}
