import { useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError, downloadFile } from '../../lib/apiClient';
import type { DocumentItem, UploadResponse } from '../../lib/types';
import { Banner, Button, Card, Field, Table } from '../../components/ui';

const ROLE_ACCESS_LABEL: Record<string, string> = {
  ROLE_ALL: 'Iedereen',
  STUDENT: 'Alleen studenten',
  CLEANER: 'Alleen schoonmakers',
  ADMIN: 'Alleen admins',
};

export function DocumentsPage() {
  const queryClient = useQueryClient();
  const [error, setError] = useState<string | null>(null);
  const [roleAccess, setRoleAccess] = useState('ROLE_ALL');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const documentsQuery = useQuery({
    queryKey: ['documents'],
    queryFn: () => api.get<DocumentItem[]>('/api/documents'),
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) =>
      api.upload<UploadResponse>('/api/documents', file, 'file', { roleAccess }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['documents'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Uploaden mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/documents/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['documents'] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  const documents = documentsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-stone-800">Documenten</h1>
      </div>
      {error && <Banner kind="error" message={error} />}

      <Card title="Document uploaden">
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          onChange={(e) => {
            const file = e.target.files?.[0];
            if (file) uploadMutation.mutate(file);
            e.target.value = '';
          }}
        />
        <div className="grid grid-cols-1 gap-x-4 sm:grid-cols-2">
          <Field label="Zichtbaar voor">
            <select
              value={roleAccess}
              onChange={(e) => setRoleAccess(e.target.value)}
              className="w-full rounded-lg border border-stone-300 px-3 py-2 text-sm"
            >
              {Object.entries(ROLE_ACCESS_LABEL).map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </select>
          </Field>
        </div>
        <Button onClick={() => fileInputRef.current?.click()} disabled={uploadMutation.isPending}>
          {uploadMutation.isPending ? 'Bezig...' : 'Bestand kiezen en uploaden'}
        </Button>
      </Card>

      <div className="mt-6">
        <Card>
          <Table head={['Titel', 'Zichtbaar voor', 'Geüpload door', '']}>
            {documents.map((doc) => (
              <tr key={doc.id}>
                <td className="py-2 pr-4">{doc.title}</td>
                <td className="py-2 pr-4">{ROLE_ACCESS_LABEL[doc.roleAccess] ?? doc.roleAccess}</td>
                <td className="py-2 pr-4">{doc.uploadedBy}</td>
                <td className="py-2 text-right">
                  <button
                    className="mr-3 text-xs font-medium text-emerald-700 hover:underline"
                    onClick={() => downloadFile(`/api/documents/${doc.id}/download`, doc.title)}
                  >
                    Downloaden
                  </button>
                  <button
                    className="text-xs font-medium text-red-600 hover:underline"
                    onClick={() => {
                      if (confirm(`Document "${doc.title}" verwijderen?`)) deleteMutation.mutate(doc.id);
                    }}
                  >
                    Verwijderen
                  </button>
                </td>
              </tr>
            ))}
            {documents.length === 0 && (
              <tr>
                <td colSpan={4} className="py-6 text-center text-stone-400">Nog geen documenten geüpload</td>
              </tr>
            )}
          </Table>
        </Card>
      </div>
    </div>
  );
}
