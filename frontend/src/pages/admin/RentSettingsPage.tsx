import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { OrganizationRentSettings } from '../../lib/types';
import { Banner, Button, Card, Field, Input } from '../../components/ui';

export function RentSettingsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ['rent-settings'],
    queryFn: () => api.get<OrganizationRentSettings>('/api/admin/organization/rent-settings'),
  });

  const mutation = useMutation({
    mutationFn: (body: OrganizationRentSettings) =>
      api.put<OrganizationRentSettings>('/api/admin/organization/rent-settings', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['rent-settings'] });
      setSuccess('Opgeslagen.');
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setSuccess(null);
    const form = new FormData(e.currentTarget);
    const amount = form.get('defaultRentAmount');
    const invoiceDay = form.get('rentInvoiceDayOfMonth');
    const dueDay = form.get('rentDueDayOfMonth');
    mutation.mutate({
      defaultRentAmount: amount ? Number(amount) : null,
      rentInvoiceDayOfMonth: invoiceDay ? Number(invoiceDay) : null,
      rentDueDayOfMonth: dueDay ? Number(dueDay) : null,
    });
  }

  if (query.isLoading) return <p className="text-slate-400">Laden...</p>;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Huurregels</h1>
      <p className="mb-4 text-sm text-slate-500">
        Standaardbedrag geldt voor studenten zonder eigen huurbedrag. Facturen worden maandelijks automatisch
        aangemaakt op de gekozen dag.
      </p>
      {error && <Banner kind="error" message={error} />}
      {success && <Banner kind="success" message={success} />}
      <Card>
        <form onSubmit={handleSubmit} className="max-w-md">
          <Field label="Standaard huurbedrag (€)">
            <Input
              name="defaultRentAmount"
              type="number"
              step="0.01"
              min="0.01"
              defaultValue={query.data?.defaultRentAmount ?? ''}
            />
          </Field>
          <Field label="Dag van de maand waarop facturen worden aangemaakt (1-28)">
            <Input
              name="rentInvoiceDayOfMonth"
              type="number"
              min="1"
              max="28"
              defaultValue={query.data?.rentInvoiceDayOfMonth ?? 1}
            />
          </Field>
          <Field label="Dag van de maand waarop de huur vervalt (1-28)">
            <Input
              name="rentDueDayOfMonth"
              type="number"
              min="1"
              max="28"
              defaultValue={query.data?.rentDueDayOfMonth ?? 8}
            />
          </Field>
          <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Bezig...' : 'Opslaan'}</Button>
        </form>
      </Card>
    </div>
  );
}
