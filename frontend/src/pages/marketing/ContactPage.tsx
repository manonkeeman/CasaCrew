import { useState, type FormEvent } from 'react';
import { useMutation } from '@tanstack/react-query';
import { api, ApiError } from '../../lib/apiClient';
import type { ContactRequest } from '../../lib/types';
import { Banner, Button, Field, Input, Textarea } from '../../components/ui';

export function ContactPage() {
  const [success, setSuccess] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const mutation = useMutation({
    mutationFn: (body: ContactRequest) => api.post<void>('/api/contact', body),
    onSuccess: () => {
      setSuccess(true);
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Versturen mislukt, probeer het later opnieuw.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setError(null);

    const form = new FormData(e.currentTarget);

    // Honeypot: onzichtbaar voor mensen, maar bots vullen vaak elk veld in.
    if (String(form.get('website') || '').trim() !== '') {
      setSuccess(true);
      return;
    }

    mutation.mutate({
      name: String(form.get('name') || ''),
      email: String(form.get('email') || ''),
      message: String(form.get('message') || ''),
    });
  }

  return (
    <div className="mx-auto max-w-4xl px-6 py-16">
      <div className="text-center">
        <h1 className="text-3xl font-bold text-stone-900 sm:text-4xl">Contact</h1>
        <p className="mx-auto mt-4 max-w-xl text-stone-600">
          Vragen over CasaCrew of benieuwd of het bij jouw huis past? Stuur een bericht, we reageren
          zo snel mogelijk.
        </p>
      </div>

      <div className="mt-12 grid grid-cols-1 gap-10 md:grid-cols-5">
        <div className="md:col-span-3">
          <div className="rounded-2xl border border-stone-200 bg-white p-8 shadow-sm shadow-stone-200/50">
            {error && <Banner kind="error" message={error} />}
            {success ? (
              <Banner kind="success" message="Bedankt voor je bericht! We nemen zo snel mogelijk contact op." />
            ) : (
              <form onSubmit={handleSubmit}>
                <Field label="Naam">
                  <Input name="name" required minLength={2} maxLength={100} />
                </Field>
                <Field label="E-mailadres">
                  <Input name="email" type="email" required maxLength={150} />
                </Field>
                <Field label="Bericht">
                  <Textarea name="message" rows={5} required maxLength={2000} />
                </Field>
                <input
                  type="text"
                  name="website"
                  tabIndex={-1}
                  autoComplete="off"
                  className="absolute left-[-9999px] h-0 w-0 opacity-0"
                  aria-hidden="true"
                />
                <Button type="submit" disabled={mutation.isPending} className="w-full">
                  {mutation.isPending ? 'Bezig...' : 'Versturen'}
                </Button>
              </form>
            )}
          </div>
        </div>

        <div className="md:col-span-2">
          <h2 className="font-semibold text-stone-800">Direct contact</h2>
          <p className="mt-2 text-sm text-stone-600">
            <a href="mailto:info@casacrew.nl" className="text-emerald-700 hover:underline">
              info@casacrew.nl
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}
