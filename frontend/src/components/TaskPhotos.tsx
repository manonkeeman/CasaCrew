import { useRef, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, ApiError, assetUrl } from '../lib/apiClient';
import type { TaskPhoto } from '../lib/types';
import { Banner, Button } from './ui';
import { CameraIcon } from './icons';

export function TaskPhotosSection({ taskId, canDelete }: { taskId: number; canDelete: boolean }) {
  const queryClient = useQueryClient();
  const [open, setOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const photosQuery = useQuery({
    queryKey: ['task-photos', taskId],
    queryFn: () => api.get<TaskPhoto[]>(`/api/task-photos/task/${taskId}`),
    enabled: open,
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => api.upload(`/api/task-photos/task/${taskId}`, file, 'photo'),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['task-photos', taskId] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Uploaden mislukt.'),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => api.delete(`/api/task-photos/${id}`),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['task-photos', taskId] }),
    onError: (err) => setError(err instanceof ApiError ? err.message : 'Verwijderen mislukt.'),
  });

  const photos = photosQuery.data ?? [];

  return (
    <div className="mt-2">
      <button
        type="button"
        className="flex items-center gap-1 text-xs font-medium text-emerald-700 hover:underline"
        onClick={() => setOpen((v) => !v)}
      >
        <CameraIcon className="h-3.5 w-3.5" />
        {open ? "Foto's verbergen" : "Foto's"}
      </button>

      {open && (
        <div className="mt-2">
          {error && <Banner kind="error" message={error} />}
          <input
            ref={fileInputRef}
            type="file"
            accept="image/jpeg,image/png,image/webp"
            className="hidden"
            onChange={(e) => {
              const file = e.target.files?.[0];
              if (file) uploadMutation.mutate(file);
              e.target.value = '';
            }}
          />
          <div className="flex flex-wrap gap-2">
            {photos.map((photo) => (
              <div key={photo.id} className="relative">
                <a href={assetUrl(photo.photoPath) ?? '#'} target="_blank" rel="noreferrer">
                  <img
                    src={assetUrl(photo.photoPath) ?? ''}
                    alt="Taakfoto"
                    className="h-16 w-16 rounded-lg border border-stone-200 object-cover"
                  />
                </a>
                {canDelete && (
                  <button
                    type="button"
                    className="absolute -right-1 -top-1 flex h-4 w-4 items-center justify-center rounded-full bg-red-600 text-[10px] text-white"
                    onClick={() => deleteMutation.mutate(photo.id)}
                    title="Verwijderen"
                  >
                    ×
                  </button>
                )}
              </div>
            ))}
            {photos.length === 0 && <p className="text-xs text-stone-400">Nog geen foto's</p>}
          </div>
          <div className="mt-2">
            <Button
              type="button"
              variant="secondary"
              onClick={() => fileInputRef.current?.click()}
              disabled={uploadMutation.isPending}
            >
              {uploadMutation.isPending ? 'Bezig...' : 'Foto toevoegen'}
            </Button>
          </div>
        </div>
      )}
    </div>
  );
}
