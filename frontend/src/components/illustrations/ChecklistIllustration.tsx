import type { SVGProps } from 'react';

const INK = '#24483b';
const GREEN_DARK = '#2f5d4c';
const GREEN_MID = '#47805f';
const GREEN_LIGHT = '#99bfa6';
const GREEN_PALE = '#e0ebe4';
const SAND = '#faf7f2';
const CLAY = '#c96a4d';
const CLAY_LIGHT = '#e8a479';
const SKIN_B = '#ad6a43';

export function ChecklistIllustration(props: SVGProps<SVGSVGElement>) {
  return (
    <svg viewBox="0 0 420 320" fill="none" {...props}>
      <circle cx="100" cy="250" r="90" fill={GREEN_LIGHT} opacity="0.2" />
      <circle cx="320" cy="70" r="75" fill={CLAY_LIGHT} opacity="0.2" />

      <line x1="20" y1="290" x2="400" y2="290" stroke={INK} strokeWidth="3" strokeLinecap="round" opacity="0.3" />

      {/* Zwevend muntje */}
      <circle cx="140" cy="90" r="22" fill={CLAY_LIGHT} stroke={INK} strokeWidth="2.5" />
      <text x="140" y="99" fontSize="22" fontWeight="700" fill={INK} textAnchor="middle" fontFamily="system-ui, sans-serif">
        €
      </text>

      {/* Sparkle */}
      <path
        d="M370 82 L376 96 L390 100 L376 104 L370 118 L364 104 L350 100 L364 96 Z"
        fill={GREEN_MID}
      />

      {/* Kalendertje */}
      <rect x="46" y="176" width="34" height="30" rx="4" fill={SAND} stroke={INK} strokeWidth="2.5" />
      <rect x="46" y="176" width="34" height="10" fill={CLAY} />
      <circle cx="55" cy="176" r="2.5" fill={INK} />
      <circle cx="71" cy="176" r="2.5" fill={INK} />

      {/* Klembord */}
      <rect x="200" y="70" width="150" height="190" rx="14" fill={SAND} stroke={INK} strokeWidth="3.5" />
      <rect x="258" y="58" width="34" height="20" rx="7" fill={GREEN_DARK} stroke={INK} strokeWidth="2.5" />

      {[
        { y: 98, w: 68 },
        { y: 142, w: 60 },
        { y: 186, w: 68 },
        { y: 230, w: 40 },
      ].map((row) => (
        <g key={row.y}>
          <rect x="220" y={row.y} width="22" height="22" rx="5" fill={GREEN_MID} stroke={INK} strokeWidth="2" />
          <path
            d={`M225 ${row.y + 11} L231 ${row.y + 17} L242 ${row.y + 5}`}
            stroke={SAND}
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
            fill="none"
          />
          <rect x="252" y={row.y + 7} width={row.w} height="8" rx="4" fill={GREEN_PALE} />
        </g>
      ))}

      {/* Persoon */}
      <rect x="98" y="278" width="24" height="10" rx="4" fill={INK} />
      <rect x="126" y="278" width="24" height="10" rx="4" fill={INK} />
      <rect x="104" y="228" width="14" height="52" rx="6" fill={INK} opacity="0.85" />
      <rect x="130" y="228" width="14" height="52" rx="6" fill={INK} opacity="0.85" />
      <rect x="92" y="166" width="64" height="70" rx="22" fill={GREEN_DARK} />
      <path d="M96 188 L80 210" stroke={SKIN_B} strokeWidth="14" strokeLinecap="round" />
      <circle cx="78" cy="212" r="9" fill={SKIN_B} />
      <path d="M150 190 L186 170" stroke={SKIN_B} strokeWidth="14" strokeLinecap="round" />
      <circle cx="188" cy="168" r="9" fill={SKIN_B} />
      <circle cx="124" cy="146" r="20" fill={SKIN_B} />
      <path d="M102 148 A22 22 0 0 1 146 148 Z" fill={CLAY} />
    </svg>
  );
}
