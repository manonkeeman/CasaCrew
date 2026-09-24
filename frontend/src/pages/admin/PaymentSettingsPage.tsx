import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { OrganizationPaymentSettings } from '../../lib/types';
import { Banner, Button, Card, Field, Input } from '../../components/ui';

export function PaymentSettingsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ['payment-settings'],
    queryFn: () => api.get<OrganizationPaymentSettings>('/api/admin/organization/payment-settings'),
  });

  const mutation = useMutation({
    mutationFn: (body: OrganizationPaymentSettings) =>
      api.put<OrganizationPaymentSettings>('/api/admin/organization/payment-settings', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['payment-settings'] });
      // Bepaalt of Facturen/Betalingen in de sidebar zichtbaar zijn (zie
      // useAdminNav in App.tsx) -- zonder deze invalidatie blijven die
      // verborgen totdat de app opnieuw laadt.
      queryClient.invalidateQueries({ queryKey: ['onboarding-status'] });
      setSuccess('Opgeslagen.');
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setSuccess(null);
    const form = new FormData(e.currentTarget);
    mutation.mutate({
      bunqMeUsername: String(form.get('bunqMeUsername') || '') || null,
      iban: String(form.get('iban') || '') || null,
      accountHolderName: String(form.get('accountHolderName') || '') || null,
    });
  }

  if (query.isLoading) return <p className="text-slate-400">Laden...</p>;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Betaalgegevens</h1>
      <p className="mb-4 text-sm text-slate-500">
        Deze gegevens worden gebruikt in huurherinneringen naar studenten (WhatsApp/e-mail).
      </p>
      {error && <Banner kind="error" message={error} />}
      {success && <Banner kind="success" message={success} />}
      <Card>
        <form onSubmit={handleSubmit} className="max-w-md">
          <Field label="IBAN">
            <Input name="iban" defaultValue={query.data?.iban ?? ''} placeholder="NL00 BANK 0123456789" />
          </Field>
          <Field label="Naam rekeninghouder">
            <Input name="accountHolderName" defaultValue={query.data?.accountHolderName ?? ''} />
          </Field>
          <Field label="Bunq.me-gebruikersnaam (optioneel)">
            <Input name="bunqMeUsername" defaultValue={query.data?.bunqMeUsername ?? ''} placeholder="JouwBunqNaam" />
          </Field>
          <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Bezig...' : 'Opslaan'}</Button>
        </form>
      </Card>
    </div>
  );
}
