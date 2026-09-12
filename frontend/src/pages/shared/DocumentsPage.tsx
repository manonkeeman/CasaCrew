import { useQuery } from '@tanstack/react-query';
import { api, downloadFile } from '../../lib/apiClient';
import type { DocumentItem } from '../../lib/types';
import { Card, Table } from '../../components/ui';

export function DocumentsPage() {
  const documentsQuery = useQuery({
    queryKey: ['documents'],
    queryFn: () => api.get<DocumentItem[]>('/api/documents'),
  });

  const documents = documentsQuery.data ?? [];

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-2xl font-bold text-stone-800">Documenten</h1>
      </div>

      <Card>
        <Table head={['Titel', '']}>
          {documents.map((doc) => (
            <tr key={doc.id}>
              <td className="py-2 pr-4">{doc.title}</td>
              <td className="py-2 text-right">
                <button
                  className="text-xs font-medium text-emerald-700 hover:underline"
                  onClick={() => downloadFile(`/api/documents/${doc.id}/download`, doc.title)}
                >
                  Downloaden
                </button>
              </td>
            </tr>
          ))}
          {documents.length === 0 && (
            <tr>
              <td colSpan={2} className="py-6 text-center text-stone-400">Geen documenten beschikbaar</td>
            </tr>
          )}
        </Table>
      </Card>
    </div>
  );
}
