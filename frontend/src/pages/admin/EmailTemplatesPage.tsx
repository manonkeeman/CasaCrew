import { useState, type FormEvent } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { EmailTemplate } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';

export function EmailTemplatesPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const query = useQuery({
    queryKey: ['email-templates'],
    queryFn: () => api.get<EmailTemplate[]>('/api/admin/email-templates'),
  });

  const mutation = useMutation({
    mutationFn: ({ type, subject, body }: { type: string; subject: string; body: string }) =>
      api.put(`/api/admin/email-templates/${type}`, { subject, body }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['email-templates'] });
      setSuccess('Sjabloon opgeslagen.');
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Opslaan mislukt.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>, type: string) {
    e.preventDefault();
    setSuccess(null);
    const form = new FormData(e.currentTarget);
    mutation.mutate({ type, subject: String(form.get('subject')), body: String(form.get('body')) });
  }

  return (
    <div>
      <h1 className="mb-2 text-2xl font-bold text-slate-800">E-mailsjablonen</h1>
      <p className="mb-6 text-sm text-slate-500">
        Gebruik <code className="rounded bg-slate-100 px-1">{'{{naam}}'}</code>,{' '}
        <code className="rounded bg-slate-100 px-1">{'{{bedrag}}'}</code>,{' '}
        <code className="rounded bg-slate-100 px-1">{'{{maand}}'}</code>,{' '}
        <code className="rounded bg-slate-100 px-1">{'{{betaalLink}}'}</code> en{' '}
        <code className="rounded bg-slate-100 px-1">{'{{vervaldatum}}'}</code> als plaatshouders.
      </p>
      {error && <Banner kind="error" message={error} />}
      {success && <Banner kind="success" message={success} />}
      <div className="space-y-6">
        {(query.data ?? []).map((template) => (
          <Card key={template.id} title={template.type}>
            <form onSubmit={(e) => handleSubmit(e, template.type)}>
              <Field label="Onderwerp">
                <Input name="subject" defaultValue={template.subject} required />
              </Field>
              <Field label="Inhoud">
                <Textarea name="body" defaultValue={template.body} rows={8} required />
              </Field>
              <Button type="submit" disabled={mutation.isPending}>Opslaan</Button>
            </form>
          </Card>
        ))}
      </div>
    </div>
  );
}
