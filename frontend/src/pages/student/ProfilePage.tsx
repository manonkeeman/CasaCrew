import { useRef, useState, type FormEvent } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { api, ApiError, assetUrl } from '../../lib/apiClient';
import { useAuth } from '../../context/AuthContext';
import type { UserProfileUpdate, UserResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Input, Textarea } from '../../components/ui';

export function ProfilePage() {
  const { user, refreshUser } = useAuth();
  const queryClient = useQueryClient();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const updateMutation = useMutation({
    mutationFn: (body: UserProfileUpdate) => api.put<UserResponse>('/api/users/me/profile', body),
    onSuccess: async () => {
      await refreshUser();
      setSuccess('Profiel bijgewerkt.');
      setError(null);
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Bijwerken mislukt.'),
  });

  const photoMutation = useMutation({
    mutationFn: (file: File) => api.upload<UserResponse>('/api/users/me/profile-photo', file),
    onSuccess: async () => {
      await refreshUser();
      queryClient.invalidateQueries();
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Uploaden mislukt.'),
  });

  const deletePhotoMutation = useMutation({
    mutationFn: () => api.delete<UserResponse>('/api/users/me/profile-photo'),
    onSuccess: async () => {
      await refreshUser();
    },
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setSuccess(null);
    const form = new FormData(e.currentTarget);
    updateMutation.mutate({
      username: String(form.get('username') || ''),
      phoneNumber: String(form.get('phoneNumber') || ''),
      emergencyPhoneNumber: String(form.get('emergencyPhoneNumber') || ''),
      studyOrWork: String(form.get('studyOrWork') || ''),
      favoriteMeal: String(form.get('favoriteMeal') || ''),
      parentsAddress: String(form.get('parentsAddress') || ''),
    });
  }

  if (!user) return null;
  const photoUrl = assetUrl(user.profileImagePath);

  return (
    <div>
      <h1 className="mb-6 text-2xl font-bold text-slate-800">Mijn profiel</h1>
      {error && <Banner kind="error" message={error} />}
      {success && <Banner kind="success" message={success} />}

      <Card title="Profielfoto">
        <div className="flex items-center gap-4">
          {photoUrl ? (
            <img src={photoUrl} alt="Profielfoto" className="h-20 w-20 rounded-full object-cover" />
          ) : (
            <div className="flex h-20 w-20 items-center justify-center rounded-full bg-slate-100 text-slate-400">
              geen foto
            </div>
          )}
          <div className="space-x-2">
            <input
              ref={fileInputRef}
              type="file"
              accept="image/*"
              className="hidden"
              onChange={(e) => {
                const file = e.target.files?.[0];
                if (file) photoMutation.mutate(file);
              }}
            />
            <Button type="button" variant="secondary" onClick={() => fileInputRef.current?.click()}>
              Foto uploaden
            </Button>
            {photoUrl && (
              <Button type="button" variant="danger" onClick={() => deletePhotoMutation.mutate()}>
                Verwijderen
              </Button>
            )}
          </div>
        </div>
      </Card>

      <div className="mt-6">
        <Card title="Gegevens">
          <form onSubmit={handleSubmit} className="grid grid-cols-2 gap-x-4">
            <Field label="Naam">
              <Input name="username" defaultValue={user.username} required />
            </Field>
            <Field label="Telefoonnummer">
              <Input name="phoneNumber" defaultValue={user.phoneNumber ?? ''} />
            </Field>
            <Field label="Noodnummer">
              <Input name="emergencyPhoneNumber" defaultValue={user.emergencyPhoneNumber ?? ''} />
            </Field>
            <Field label="Studie/werk">
              <Input name="studyOrWork" defaultValue={user.studyOrWork ?? ''} />
            </Field>
            <Field label="Lievelingseten">
              <Input name="favoriteMeal" defaultValue={user.favoriteMeal ?? ''} />
            </Field>
            <div className="col-span-2">
              <Field label="Adres ouders">
                <Textarea name="parentsAddress" defaultValue={user.parentsAddress ?? ''} rows={2} />
              </Field>
            </div>
            <div className="col-span-2">
              <Button type="submit" disabled={updateMutation.isPending}>
                {updateMutation.isPending ? 'Bezig...' : 'Opslaan'}
              </Button>
            </div>
          </form>
        </Card>
      </div>
    </div>
  );
}
