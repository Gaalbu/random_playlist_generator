import { useEffect, useMemo, useState } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { LogIn, Loader2, CheckCircle2, ExternalLink, Music2 } from 'lucide-react'
import { VinylDisc } from './components/VinylDisc'
import { FilterPanel } from './components/FilterPanel'
import { PlaylistPlayer } from './components/PlaylistPlayer'
import { useAuthStatus } from './hooks/useAuthStatus'
import { api, type DecadeOption, type GenreOption, type PlaylistResultDto } from './lib/api'

type Mode = 'random' | 'custom'

const TRACK_COUNT_OPTIONS = [15, 25, 40] as const

function App() {
  const [genres, setGenres] = useState<GenreOption[]>([])
  const [decades, setDecades] = useState<DecadeOption[]>([])
  const [randomGenre, setRandomGenre] = useState<GenreOption | null>(null)
  const [randomDecade, setRandomDecade] = useState<DecadeOption | null>(null)
  const [mode, setMode] = useState<Mode>('random')
  const [selectedGenreIds, setSelectedGenreIds] = useState<string[]>([])
  const [selectedDecadeIds, setSelectedDecadeIds] = useState<string[]>([])
  const [trackCount, setTrackCount] = useState<number | null>(null)

  const [playlist, setPlaylist] = useState<PlaylistResultDto | null>(null)
  const [generating, setGenerating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [saving, setSaving] = useState(false)
  const [saveResult, setSaveResult] = useState<{ youtubeMusicUrl: string } | null>(null)
  const authStatus = useAuthStatus()

  const justConnected = useMemo(() => new URLSearchParams(window.location.search).get('connected') === '1', [])

  useEffect(() => {
    Promise.all([api.genres(), api.decades(), api.randomFilters()])
      .then(([g, d, rf]) => {
        setGenres(g)
        setDecades(d)
        setRandomGenre(rf.genre)
        setRandomDecade(rf.decade)
      })
      .catch(() => setError('Não foi possível carregar o catálogo. O backend está rodando?'))
  }, [])

  function reroll() {
    api.randomFilters().then((rf) => {
      setRandomGenre(rf.genre)
      setRandomDecade(rf.decade)
    })
  }

  function toggleGenre(id: string) {
    setSelectedGenreIds((prev) => (prev.includes(id) ? prev.filter((g) => g !== id) : [...prev, id]))
  }

  function toggleDecade(id: string) {
    setSelectedDecadeIds((prev) => (prev.includes(id) ? prev.filter((d) => d !== id) : [...prev, id]))
  }

  async function generate() {
    setGenerating(true)
    setError(null)
    setSaveResult(null)
    try {
      const result = await api.generate({
        genreIds: mode === 'random' ? (randomGenre ? [randomGenre.id] : null) : selectedGenreIds.length ? selectedGenreIds : null,
        decadeIds: mode === 'random' ? (randomDecade ? [randomDecade.id] : null) : selectedDecadeIds.length ? selectedDecadeIds : null,
        trackCount,
      })
      setPlaylist(result)
    } catch {
      setError('A busca no YouTube falhou. Tenta de novo em alguns segundos.')
    } finally {
      setGenerating(false)
    }
  }

  async function save() {
    if (!playlist) return
    if (!authStatus?.oauthEnabled) return
    if (!authStatus.authenticated) {
      window.location.href = api.loginUrl(authStatus.loginUrl)
      return
    }
    setSaving(true)
    try {
      const result = await api.save({ title: playlist.title, videoIds: playlist.tracks.map((t) => t.videoId) })
      setSaveResult(result)
    } catch {
      setError('Não deu pra salvar no YT Music agora. Tenta de novo.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="relative min-h-svh">
      <div className="grain" />

      <header className="mx-auto flex max-w-6xl items-center justify-between px-6 py-6">
        <div className="flex items-center gap-2 font-mono text-xs uppercase tracking-widest text-muted">
          <Music2 size={16} className="text-accent" />
          playlist.generator
        </div>
        {authStatus?.oauthEnabled && <AuthPill authenticated={authStatus.authenticated} />}
      </header>

      <main className="mx-auto flex max-w-6xl flex-col items-center gap-14 px-6 pb-24 pt-8">
        <section className="relative flex w-full flex-col items-center text-center">
          <VinylDisc
            size={420}
            spinning={generating}
            className="pointer-events-none absolute -top-24 opacity-[0.08]"
          />
          {justConnected && (
            <p className="mb-4 rounded-full border border-accent-2/30 bg-accent-2/10 px-4 py-1.5 font-mono text-xs text-accent-2">
              Login com Google concluído — gere sua playlist e salve no YT Music.
            </p>
          )}
          <h1 className="relative max-w-3xl font-display text-6xl leading-[0.95] tracking-tight sm:text-7xl">
            Aperte <span className="italic text-accent">play</span> na
            sorte.
          </h1>
          <p className="relative mt-5 max-w-lg text-balance text-muted">
            Um clique gera uma playlist com filtros aleatórios que mudam a cada visita — ou escolha
            gênero e década do seu jeito. Escute na hora, salve no YT Music depois.
          </p>
        </section>

        <FilterPanel
          mode={mode}
          onModeChange={setMode}
          genres={genres}
          decades={decades}
          randomGenre={randomGenre}
          randomDecade={randomDecade}
          selectedGenreIds={selectedGenreIds}
          selectedDecadeIds={selectedDecadeIds}
          onToggleGenre={toggleGenre}
          onToggleDecade={toggleDecade}
          onReroll={reroll}
        />

        <div className="flex flex-col items-center gap-3">
          <div className="flex items-center gap-2 font-mono text-xs text-muted">
            faixas:
            {TRACK_COUNT_OPTIONS.map((n) => (
              <button
                key={n}
                type="button"
                onClick={() => setTrackCount(n)}
                className={`rounded-full border px-2.5 py-1 transition-colors cursor-pointer ${
                  trackCount === n ? 'border-accent text-accent' : 'border-line hover:border-cream/40'
                }`}
              >
                {n}
              </button>
            ))}
            <button
              type="button"
              onClick={() => setTrackCount(null)}
              className={`rounded-full border px-2.5 py-1 transition-colors cursor-pointer ${
                trackCount === null ? 'border-accent text-accent' : 'border-line hover:border-cream/40'
              }`}
            >
              surpresa
            </button>
          </div>

          <motion.button
            type="button"
            onClick={generate}
            disabled={generating}
            whileTap={{ scale: 0.96 }}
            whileHover={{ scale: 1.03 }}
            className="group relative inline-flex items-center gap-3 rounded-full bg-accent px-8 py-4 text-lg font-medium text-ink shadow-[0_0_0_1px_rgba(255,90,54,0.4)] transition-shadow hover:shadow-[0_0_40px_rgba(255,90,54,0.35)] disabled:opacity-70 cursor-pointer"
          >
            {generating ? (
              <>
                <Loader2 size={20} className="animate-spin" /> Girando o disco…
              </>
            ) : (
              <>Gerar playlist</>
            )}
          </motion.button>

          {error && <p className="font-mono text-xs text-accent">{error}</p>}
        </div>

        <AnimatePresence>
          {playlist && (
            <motion.section
              key={playlist.title}
              initial={{ opacity: 0, y: 24 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              transition={{ type: 'spring', stiffness: 200, damping: 26 }}
              className="w-full"
            >
              <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="font-mono text-xs uppercase tracking-widest text-muted">agora tocando</p>
                  <h2 className="font-display text-2xl italic">{playlist.title}</h2>
                </div>
                <SaveButton
                  saving={saving}
                  saveResult={saveResult}
                  oauthEnabled={authStatus?.oauthEnabled}
                  authenticated={authStatus?.authenticated}
                  onSave={save}
                />
              </div>
              <PlaylistPlayer title={playlist.title} tracks={playlist.tracks} />
            </motion.section>
          )}
        </AnimatePresence>
      </main>

      <footer className="mx-auto max-w-6xl px-6 pb-10 text-center font-mono text-xs text-muted">
        projeto acadêmico · dados de música via YouTube
      </footer>
    </div>
  )
}

function AuthPill({ authenticated }: { authenticated?: boolean }) {
  return (
    <span className="inline-flex items-center gap-1.5 rounded-full border border-line px-3 py-1.5 font-mono text-xs text-muted">
      <span className={`h-1.5 w-1.5 rounded-full ${authenticated ? 'bg-accent-2' : 'bg-muted'}`} />
      {authenticated ? 'conectado ao google' : 'não conectado'}
    </span>
  )
}

function SaveButton({
  saving,
  saveResult,
  oauthEnabled,
  authenticated,
  onSave,
}: {
  saving: boolean
  saveResult: { youtubeMusicUrl: string } | null
  oauthEnabled?: boolean
  authenticated?: boolean
  onSave: () => void
}) {
  if (!oauthEnabled) {
    return (
      <span
        title="Login com Google não configurado no backend (GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET)"
        className="inline-flex items-center gap-2 rounded-full border border-line px-4 py-2 text-sm text-muted opacity-60"
      >
        <LogIn size={16} /> Salvar no YT Music indisponível
      </span>
    )
  }
  if (saveResult) {
    return (
      <a
        href={saveResult.youtubeMusicUrl}
        target="_blank"
        rel="noreferrer"
        className="inline-flex items-center gap-2 rounded-full border border-accent-2/40 bg-accent-2/10 px-4 py-2 text-sm text-accent-2"
      >
        <CheckCircle2 size={16} /> Salva no YT Music <ExternalLink size={14} />
      </a>
    )
  }
  return (
    <button
      type="button"
      onClick={onSave}
      disabled={saving}
      className="inline-flex items-center gap-2 rounded-full border border-line px-4 py-2 text-sm text-cream hover:border-accent hover:text-accent transition-colors disabled:opacity-60 cursor-pointer"
    >
      {saving ? <Loader2 size={16} className="animate-spin" /> : <LogIn size={16} />}
      {authenticated ? 'Salvar no YT Music' : 'Entrar com Google e salvar'}
    </button>
  )
}

export default App
