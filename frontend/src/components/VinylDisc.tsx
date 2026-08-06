import { motion } from 'framer-motion'

type VinylDiscProps = {
  size?: number
  spinning?: boolean
  className?: string
}

export function VinylDisc({ size = 320, spinning = true, className = '' }: VinylDiscProps) {
  return (
    <motion.svg
      viewBox="0 0 200 200"
      width={size}
      height={size}
      className={className}
      animate={spinning ? { rotate: 360 } : { rotate: 0 }}
      transition={spinning ? { duration: 6, repeat: Infinity, ease: 'linear' } : { duration: 0.4 }}
    >
      <circle cx="100" cy="100" r="98" fill="#0c0a10" stroke="rgba(246,241,230,0.14)" strokeWidth="1" />
      {[92, 82, 72, 62, 52].map((r) => (
        <circle key={r} cx="100" cy="100" r={r} fill="none" stroke="rgba(246,241,230,0.07)" strokeWidth="0.6" />
      ))}
      <circle cx="100" cy="100" r="40" fill="#ff5a36" />
      <circle cx="100" cy="100" r="40" fill="none" stroke="#0c0a10" strokeWidth="1" />
      <circle cx="100" cy="100" r="4" fill="#0c0a10" />
      <text
        x="100"
        y="94"
        textAnchor="middle"
        fontFamily="var(--font-mono)"
        fontSize="7"
        fill="#0c0a10"
        letterSpacing="1"
      >
        PLAYLIST
      </text>
      <text
        x="100"
        y="112"
        textAnchor="middle"
        fontFamily="var(--font-mono)"
        fontSize="6"
        fill="#0c0a10"
        letterSpacing="1"
      >
        GENERATOR
      </text>
    </motion.svg>
  )
}
