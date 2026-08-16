import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { OrganizationProfile } from '../../lib/types';
import { Banner, Button, Card, Field, Input } from '../../components/ui';

export function OrganizationProfilePage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ['organization-profile'],
    queryFn: () => api.get<OrganizationProfile>('/api/admin/organization/profile'),
  });

  const mutation = useMutation({
    mutationFn: (body: OrganizationProfile) => api.put<OrganizationProfile>('/api/admin/organization/profile', body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['organization-profile'] });
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
      name: String(form.get('name') || ''),
      address: String(form.get('address') || '') || null,
    });
  }

  if (query.isLoading) return <p className="text-slate-400">Laden...</p>;

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Huisprofiel</h1>
      {error && <Banner kind="error" message={error} />}
      {success && <Banner kind="success" message={success} />}
      <Card>
        <form onSubmit={handleSubmit} className="max-w-md">
          <Field label="Naam">
            <Input name="name" defaultValue={query.data?.name} required />
          </Field>
          <Field label="Adres">
            <Input name="address" defaultValue={query.data?.address ?? ''} placeholder="Straat 1, 1234 AB Plaats" />
          </Field>
          <Button type="submit" disabled={mutation.isPending}>{mutation.isPending ? 'Bezig...' : 'Opslaan'}</Button>
        </form>
      </Card>
    </div>
  );
}
