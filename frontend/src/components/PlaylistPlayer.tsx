import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Play } from 'lucide-react'
import type { TrackDto } from '../lib/api'

type PlaylistPlayerProps = {
  title: string
  tracks: TrackDto[]
}

export function PlaylistPlayer({ title, tracks }: PlaylistPlayerProps) {
  const [activeIndex, setActiveIndex] = useState(0)
  const active = tracks[activeIndex]
  const queue = tracks.filter((_, i) => i !== activeIndex).map((t) => t.videoId)
  const embedSrc = active
    ? `https://www.youtube-nocookie.com/embed/${active.videoId}?autoplay=1&rel=0&modestbranding=1&playlist=${queue.join(',')}`
    : undefined

  return (
    <div className="grid gap-6 lg:grid-cols-[1.3fr_1fr]">
      <div className="overflow-hidden rounded-2xl border border-line bg-surface">
        <AnimatePresence mode="wait">
          {embedSrc && (
            <motion.div
              key={active.videoId}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="aspect-video w-full"
            >
              <iframe
                key={active.videoId}
                src={embedSrc}
                title={active.title}
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowFullScreen
                className="h-full w-full"
              />
            </motion.div>
          )}
        </AnimatePresence>
        <div className="flex items-end gap-1 border-t border-line px-5 py-4">
          {[0, 1, 2, 3, 4].map((i) => (
            <span
              key={i}
              className="eq-bar h-4 w-1 rounded-full bg-accent"
              style={{ animationDelay: `${i * 0.12}s` }}
            />
          ))}
          <p className="ml-3 truncate font-mono text-xs text-muted">{title}</p>
        </div>
      </div>

      <ol className="max-h-[420px] space-y-1 overflow-y-auto pr-1">
        {tracks.map((track, i) => (
          <motion.li
            key={track.videoId}
            initial={{ opacity: 0, x: 12 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ delay: i * 0.03 }}
          >
            <button
              type="button"
              onClick={() => setActiveIndex(i)}
              className={`flex w-full items-center gap-3 rounded-xl border px-3 py-2 text-left transition-colors cursor-pointer ${
                i === activeIndex
                  ? 'border-accent/40 bg-accent/10'
                  : 'border-transparent hover:border-line hover:bg-surface'
              }`}
            >
              <span className="relative shrink-0 overflow-hidden rounded-lg">
                <img src={track.thumbnailUrl} alt="" width={56} height={40} className="h-10 w-14 object-cover" />
                {i === activeIndex && (
                  <span className="absolute inset-0 flex items-center justify-center bg-ink/50">
                    <Play size={14} className="fill-accent text-accent" />
                  </span>
                )}
              </span>
              <span className="min-w-0">
                <span className="block truncate text-sm text-cream">{track.title}</span>
                <span className="block truncate font-mono text-xs text-muted">{track.channelTitle}</span>
              </span>
            </button>
          </motion.li>
        ))}
      </ol>
    </div>
  )
}
