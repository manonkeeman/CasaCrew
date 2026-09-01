const PALETTE = ['bg-emerald-500', 'bg-amber-500', 'bg-rose-400', 'bg-sky-500', 'bg-violet-500', 'bg-orange-500'];

function colorFor(seed: string) {
  let hash = 0;
  for (let i = 0; i < seed.length; i++) hash = seed.charCodeAt(i) + ((hash << 5) - hash);
  return PALETTE[Math.abs(hash) % PALETTE.length];
}

function initials(name: string) {
  const parts = name.trim().split(/\s+/);
  const first = parts[0]?.[0] ?? '';
  const last = parts.length > 1 ? parts[parts.length - 1][0] : '';
  return (first + last).toUpperCase() || '?';
}

export function Avatar({
  name,
  photoUrl,
  size = 'md',
}: {
  name: string;
  photoUrl?: string | null;
  size?: 'sm' | 'md' | 'lg';
}) {
  const dimensions = { sm: 'h-8 w-8 text-xs', md: 'h-10 w-10 text-sm', lg: 'h-16 w-16 text-xl' }[size];

  if (photoUrl) {
    return <img src={photoUrl} alt={name} className={`${dimensions} shrink-0 rounded-full object-cover`} />;
  }

  return (
    <div
      className={`${dimensions} ${colorFor(name)} flex shrink-0 items-center justify-center rounded-full font-semibold text-white`}
    >
      {initials(name)}
    </div>
  );
}
