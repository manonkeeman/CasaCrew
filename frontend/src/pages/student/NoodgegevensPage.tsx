import { useQuery } from '@tanstack/react-query';
import { api } from '../../lib/apiClient';
import { telHref } from '../../lib/phone';
import type { EmergencyContact } from '../../lib/types';
import { Card } from '../../components/ui';

export function NoodgegevensPage() {
  const query = useQuery({
    queryKey: ['emergency-contacts'],
    queryFn: () => api.get<EmergencyContact[]>('/api/emergency-contacts'),
  });

  const contacts = (query.data ?? []).filter((c) => c.phoneNumber);

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Noodgegevens</h1>
      <div className="space-y-4">
        {contacts.map((contact) => (
          <Card key={contact.id}>
            <div className="flex items-center justify-between">
              <div>
                <h3 className="font-semibold text-slate-800">{contact.label}</h3>
                <p className="mt-1 text-sm text-slate-600">{contact.phoneNumber}</p>
              </div>
              <a
                href={telHref(contact.phoneNumber!)}
                className="shrink-0 rounded-lg bg-emerald-50 px-3 py-2 text-sm font-medium text-emerald-700 hover:bg-emerald-100"
              >
                Bel
              </a>
            </div>
          </Card>
        ))}
        {contacts.length === 0 && <p className="text-sm text-slate-400">Nog geen noodgegevens beschikbaar.</p>}
      </div>
    </div>
  );
}
