import { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import { Banner, Button, Card } from '../../components/ui';

interface JobDef {
  key: string;
  label: string;
  description: string;
  path: string;
}

const JOBS: JobDef[] = [
  {
    key: 'reminders',
    label: 'Factuurherinneringen',
    description: 'Verstuurt herinneringen voor openstaande facturen.',
    path: '/api/admin/jobs/reminders/trigger',
  },
  {
    key: 'overdue',
    label: 'Vervallen facturen',
    description: 'Verwerkt facturen die de vervaldatum zijn gepasseerd.',
    path: '/api/admin/jobs/overdue/trigger',
  },
  {
    key: 'missed-cleaning',
    label: 'Gemiste schoonmaaktaken',
    description: 'Stuurt meldingen voor schoonmaaktaken die niet zijn afgerond.',
    path: '/api/admin/jobs/cleaning/missed/trigger',
  },
  {
    key: 'rent-reminder',
    label: 'Huurherinnering',
    description: 'Stuurt een huurherinnering naar alle studenten.',
    path: '/api/admin/jobs/rent-reminder/trigger',
  },
  {
    key: 'monthly-invoices',
    label: 'Maandelijkse facturen',
    description: 'Maakt de maandelijkse huurfacturen aan.',
    path: '/api/admin/jobs/monthly-invoices/trigger',
  },
  {
    key: 'payment-reminder-1',
    label: 'Eerste betalingsherinnering',
    description: 'Verstuurt de eerste betalingsherinnering.',
    path: '/api/admin/jobs/payment-reminder-1/trigger',
  },
  {
    key: 'payment-reminder-2',
    label: 'Tweede betalingsherinnering',
    description: 'Verstuurt de tweede betalingsherinnering.',
    path: '/api/admin/jobs/payment-reminder-2/trigger',
  },
];

export function JobsPage() {
  const [message, setMessage] = useState<{ kind: 'error' | 'success'; text: string } | null>(null);

  const triggerMutation = useMutation({
    mutationFn: (path: string) => api.post<{ message: string }>(path),
    onSuccess: (data) => setMessage({ kind: 'success', text: data.message }),
    onError: (err) => setMessage({ kind: 'error', text: err instanceof ApiError ? err.message : 'Taak uitvoeren mislukt.' }),
  });

  return (
    <div>
      <h1 className="mb-2 text-2xl font-bold text-stone-800">Systeemtaken</h1>
      <p className="mb-6 text-sm text-stone-500">
        Deze taken draaien normaal automatisch op een schema. Hier kun je ze handmatig direct uitvoeren, bijvoorbeeld om te testen of om een gemiste run in te halen.
      </p>
      {message && <Banner kind={message.kind} message={message.text} />}

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
        {JOBS.map((job) => (
          <Card key={job.key} title={job.label}>
            <p className="mb-4 text-sm text-stone-500">{job.description}</p>
            <Button
              variant="secondary"
              disabled={triggerMutation.isPending}
              onClick={() => {
                setMessage(null);
                triggerMutation.mutate(job.path);
              }}
            >
              {triggerMutation.isPending ? 'Bezig...' : 'Nu uitvoeren'}
            </Button>
          </Card>
        ))}
      </div>
    </div>
  );
}
