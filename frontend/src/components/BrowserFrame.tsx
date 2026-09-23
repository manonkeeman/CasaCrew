export function BrowserFrame({ src, alt, url }: { src: string; alt: string; url: string }) {
  return (
    <div className="overflow-hidden rounded-2xl border border-stone-200 bg-white shadow-xl shadow-stone-300/40">
      <div className="flex items-center gap-1.5 border-b border-stone-200 bg-stone-50 px-4 py-2.5">
        <span className="h-2.5 w-2.5 rounded-full bg-red-400" />
        <span className="h-2.5 w-2.5 rounded-full bg-amber-400" />
        <span className="h-2.5 w-2.5 rounded-full bg-emerald-400" />
        <span className="ml-3 truncate rounded-md border border-stone-200 bg-white px-3 py-1 text-xs text-stone-400">
          {url}
        </span>
      </div>
      <img src={src} alt={alt} className="w-full" />
    </div>
  );
}
