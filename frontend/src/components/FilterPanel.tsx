import { motion } from 'framer-motion'
import { Dices, Sparkles, SlidersHorizontal } from 'lucide-react'
import type { DecadeOption, GenreOption } from '../lib/api'

type Mode = 'random' | 'custom'

type FilterPanelProps = {
  mode: Mode
  onModeChange: (mode: Mode) => void
  genres: GenreOption[]
  decades: DecadeOption[]
  randomGenre: GenreOption | null
  randomDecade: DecadeOption | null
  selectedGenreIds: string[]
  selectedDecadeIds: string[]
  onToggleGenre: (id: string) => void
  onToggleDecade: (id: string) => void
  onReroll: () => void
}

export function FilterPanel({
  mode,
  onModeChange,
  genres,
  decades,
  randomGenre,
  randomDecade,
  selectedGenreIds,
  selectedDecadeIds,
  onToggleGenre,
  onToggleDecade,
  onReroll,
}: FilterPanelProps) {
  return (
    <div className="w-full max-w-3xl">
      <div className="flex items-center gap-2 mb-6">
        <ModeTab
          active={mode === 'random'}
          onClick={() => onModeChange('random')}
          icon={<Dices size={16} />}
          label="Surpreenda-me"
        />
        <ModeTab
          active={mode === 'custom'}
          onClick={() => onModeChange('custom')}
          icon={<SlidersHorizontal size={16} />}
          label="Personalizar"
        />
      </div>

      {mode === 'random' ? (
        <motion.div
          key="random"
          initial={{ opacity: 0, y: 6 }}
          animate={{ opacity: 1, y: 0 }}
          className="flex flex-wrap items-center gap-3 font-mono text-sm text-muted"
        >
          <span>hoje o sorteio caiu em</span>
          {randomGenre && <Pill accent>{randomGenre.label}</Pill>}
          <span>+</span>
          {randomDecade && <Pill accent>{randomDecade.label}</Pill>}
          <button
            type="button"
            onClick={onReroll}
            className="ml-1 inline-flex items-center gap-1.5 rounded-full border border-line px-3 py-1.5 text-cream/80 hover:border-accent hover:text-accent transition-colors cursor-pointer"
          >
            <Sparkles size={14} /> rolar de novo
          </button>
        </motion.div>
      ) : (
        <motion.div
          key="custom"
          initial={{ opacity: 0, y: 6 }}
          animate={{ opacity: 1, y: 0 }}
          className="space-y-5"
        >
          <ChipGroup label="Gênero" options={genres} selected={selectedGenreIds} onToggle={onToggleGenre} />
          <ChipGroup label="Década" options={decades} selected={selectedDecadeIds} onToggle={onToggleDecade} />
        </motion.div>
      )}
    </div>
  )
}

function ModeTab({
  active,
  onClick,
  icon,
  label,
}: {
  active: boolean
  onClick: () => void
  icon: React.ReactNode
  label: string
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={`relative inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-medium transition-colors cursor-pointer ${
        active ? 'text-ink' : 'text-muted hover:text-cream'
      }`}
    >
      {active && (
        <motion.span
          layoutId="mode-pill"
          className="absolute inset-0 rounded-full bg-accent"
          transition={{ type: 'spring', stiffness: 400, damping: 30 }}
        />
      )}
      <span className="relative flex items-center gap-2">
        {icon}
        {label}
      </span>
    </button>
  )
}

function Pill({ children, accent }: { children: React.ReactNode; accent?: boolean }) {
  return (
    <span
      className={`rounded-full px-3 py-1.5 ${
        accent ? 'bg-accent/15 text-accent border border-accent/30' : 'bg-surface-2 text-cream'
      }`}
    >
      {children}
    </span>
  )
}

function ChipGroup<T extends { id: string; label: string }>({
  label,
  options,
  selected,
  onToggle,
}: {
  label: string
  options: T[]
  selected: string[]
  onToggle: (id: string) => void
}) {
  return (
    <div>
      <p className="mb-2 font-mono text-xs uppercase tracking-widest text-muted">{label}</p>
      <div className="flex flex-wrap gap-2">
        {options.map((opt) => {
          const active = selected.includes(opt.id)
          return (
            <motion.button
              key={opt.id}
              type="button"
              whileTap={{ scale: 0.94 }}
              onClick={() => onToggle(opt.id)}
              className={`rounded-full border px-3.5 py-1.5 text-sm transition-colors cursor-pointer ${
                active
                  ? 'border-accent bg-accent text-ink font-medium'
                  : 'border-line text-cream/80 hover:border-cream/40'
              }`}
            >
              {opt.label}
            </motion.button>
          )
        })}
      </div>
    </div>
  )
}
