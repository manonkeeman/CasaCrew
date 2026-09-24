export function GradientBlobs() {
  return (
    <div aria-hidden="true" className="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
      <div className="absolute -left-24 -top-24 h-72 w-72 rounded-full bg-emerald-300/50 blur-2xl" />
      <div className="absolute -right-24 top-16 h-96 w-96 rounded-full bg-emerald-400/35 blur-2xl" />
      <div className="absolute left-1/3 top-40 h-56 w-56 rounded-full bg-[#e8a479]/30 blur-2xl" />
    </div>
  );
}
